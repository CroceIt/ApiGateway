package com.hjzgg.apigateway.commons.maven;

import org.apache.commons.io.FileUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.LocalArtifactRequest;
import org.eclipse.aether.repository.LocalArtifactResult;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DependencyResolver {
    private static final Logger logger = LoggerFactory.getLogger(DependencyResolver.class);

    private final RemoteRepository central;

    private RepositorySystem repositorySystem;

    private LocalRepository localRepository;

    public DependencyResolver(RepositorySystem repositorySystem, String remoteRepository, String localRepository) {
        central = new RemoteRepository.Builder("central", "default", remoteRepository).build();
        this.repositorySystem = repositorySystem;
        this.localRepository = new LocalRepository(localRepository);
    }

    private RepositorySystemSession newSession(boolean offline) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setOffline(offline);
        session.setConfigProperty(ConflictResolver.CONFIG_PROP_VERBOSE, true);
        session.setConfigProperty(DependencyManagerUtils.CONFIG_PROP_VERBOSE, true);
        session.setTransferListener(new LoggingTransferListener());
        session.setRepositoryListener(new LoggingRepositoryListener());
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepository));
        return session;
    }

    //删除本地仓库中对应的artifact
    private void deleteExistArtifact(RepositorySystemSession session, Artifact artifact) {
        LocalArtifactRequest request = new LocalArtifactRequest();
        request.setArtifact(artifact);
        LocalArtifactResult result = session.getLocalRepositoryManager().find(session, request);
        if (result.getFile() != null) {
            FileUtils.deleteQuietly(result.getFile().getParentFile());
        }
    }

    public List<File> resolve(String gav, String scope, DependencyFilter filter, boolean offline) {
        RepositorySystemSession session = newSession(offline);
        Artifact artifact = new DefaultArtifact(gav);
        //deleteExistArtifact(session, artifact);
        Dependency dependency = new Dependency(artifact, scope);
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        collectRequest.addRepository(central);

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setCollectRequest(collectRequest);
        dependencyRequest.setFilter(filter);

        try {
            DependencyNode rootNode = repositorySystem.resolveDependencies(session, dependencyRequest).getRoot();
            try {
                rootNode.setChildren(resolveDependencies(artifact, session));
            } catch (Exception ex) {
                //TODO handle exceptions
            }
            PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
            rootNode.accept(nlg);
            return nlg.getFiles();
        } catch (DependencyResolutionException e) {
            logger.error(String.format("Failed to resolve artifact %s, scope: %s.", gav, scope), e);
            return Collections.emptyList();
        }
    }

    private List<DependencyNode> resolveDependencies(Artifact artifact, RepositorySystemSession session) throws ArtifactDescriptorException, DependencyResolutionException {
        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact(artifact);
        descriptorRequest.addRepository(central);
        ArtifactDescriptorResult descriptorResult = repositorySystem.readArtifactDescriptor(session, descriptorRequest);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRootArtifact(artifact);
        collectRequest.setDependencies(descriptorResult.getDependencies());
        collectRequest.setManagedDependencies(descriptorResult.getManagedDependencies());
        collectRequest.setRepositories(descriptorRequest.getRepositories());

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setCollectRequest(collectRequest);
        DependencyNode rootNode = repositorySystem.resolveDependencies(session, dependencyRequest).getRoot();
        return rootNode.getChildren();
    }

    public File resolveSingleArtifact(String gav) throws ArtifactResolutionException {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(new DefaultArtifact(gav));
        List<RemoteRepository> repositories = new ArrayList<>();
        repositories.add(central);
        request.setRepositories(repositories);
        ArtifactResult result = repositorySystem.resolveArtifact(newSession(false), request);
        return result.getArtifact().getFile();
    }

    public List<File> resolve(String gav, boolean offline) {
        DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
        return resolve(gav, JavaScopes.COMPILE, classpathFilter, offline);
    }

    public List<File> resolveMultiArtifacts(List<String> gavs) {
        List<RemoteRepository> repositories = new ArrayList<>();
        repositories.add(central);

        List<ArtifactRequest> artifactRequests = gavs.stream().map(gav -> {
            ArtifactRequest request = new ArtifactRequest();
            request.setArtifact(new DefaultArtifact(gav));
            request.setRepositories(repositories);
            return request;
        }).collect(Collectors.toList());
        try {
            List<ArtifactResult> results = repositorySystem.resolveArtifacts(newSession(false), artifactRequests);
            return results.stream().map(artifactResult -> artifactResult.getArtifact().getFile()).collect(Collectors.toList());
        } catch (ArtifactResolutionException e) {
            logger.error("artifacts={}, resolve error!", gavs);
            return Collections.EMPTY_LIST;
        }
    }
}
