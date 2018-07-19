package com.hjzgg.apigateway.commons.maven;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.Exceptions;
import reactor.ipc.netty.http.client.HttpClient;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public class DefaultMavenRepository implements MavenRepository {
    private static final Logger logger = LoggerFactory.getLogger(DefaultMavenRepository.class);

    private final HttpClient httpClient;
    private final DependencyResolver dependencyResolver;

    public DefaultMavenRepository(RepositorySystem repositorySystem, String localDirectory, String host, int port) {
        httpClient = HttpClient.create(host, port);
        dependencyResolver = new DependencyResolver(repositorySystem, "http://" + host + ":" + port + "/nexus/content/groups/public/", localDirectory);
    }


    @Override
    public List<File> resolveClassPath(boolean offline, MavenArtifact... artifacts) {
        List<File> files = new ArrayList<>(Stream.of(artifacts).map(MavenArtifact::toCanonicalForm).flatMap(gav -> dependencyResolver.resolve(gav, offline).stream()).collect(toSet()));
        logger.debug("MavenArtifact files [{}]",files);
        return files;
    }

    @Override
    public List<MavenArtifact> searchByClassName(String className, Predicate<PackagingType> typePredicate) {
        return doLuceneSearch(Stream.of(Tuples.of("cn", className)), typePredicate);
    }

    @Override
    public List<MavenArtifact> searchByGAV(GAVSearchRequest GAVSearchRequest, Predicate<PackagingType> typePredicate) {
        Stream<Tuple2<String, String>> just = Stream.of(
                Tuples.of("g", GAVSearchRequest.getGroupId()),
                Tuples.of("a", GAVSearchRequest.getArtifactId()),
                Tuples.of("p", GAVSearchRequest.getExtension()),
                Tuples.of("c", GAVSearchRequest.getClassifier()),
                Tuples.of("v", GAVSearchRequest.getVersion()));
        return doLuceneSearch(just, typePredicate);
    }

    @Override
    public File resolveArtifactFile(MavenArtifact artifact) throws ArtifactResolutionException {
        return dependencyResolver.resolveSingleArtifact(artifact.toCanonicalForm());
    }

    private List<MavenArtifact> doLuceneSearch(Stream<Tuple2<String, String>> kvTuples, Predicate<PackagingType> typePredicate) {
        String queryString = kvTuples.filter(tuple2 -> !StringUtils.isEmpty(tuple2.getT2()))
                .map(this::createQueryParameter).collect(joining("&"));
        Assert.hasText(queryString, "query string is empty!");

        String url = "/nexus/service/local/lucene/search?" + queryString;
        String responseText = httpClient.get(url, request -> request.header("Accept", "application/json"))
                .flatMap(response -> response.receive().asString().limitRate(1)).reduce(String::concat).block();
        JsonObject json = Json.parse(responseText).asObject();
        int totalCount = json.getInt("totalCount", 0);

        if (totalCount <= 0) {
            logger.debug("{} 的搜索结果为{}.", queryString, totalCount);
            return Collections.emptyList();
        }

        JsonValue data = json.get("data");
        if (null == data || data.asArray().isEmpty()) {
            logger.warn("{} 的搜索结果不包含data字段.", queryString);
            return Collections.emptyList();
        }

        return data.asArray().values().stream()
                .map(JsonValue::asObject).flatMap(this::getArtifactStream).filter(artifact -> typePredicate.test(artifact.getType())).collect(Collectors.toList());
    }

    private Stream<? extends MavenArtifact> getArtifactStream(JsonObject object) {
        String artifactId = object.getString("artifactId", "");
        String groupId = object.getString("groupId", "");
        String version = object.getString("version", "");
        return object.get("artifactHits").asArray().values().stream()
                .map(JsonValue::asObject).flatMap(hits -> hits.get("artifactLinks").asArray().values().stream())
                .map(JsonValue::asObject).map(link -> {
                    String extension = link.getString("extension", "");
                    String classifier = link.getString("classifier", "");
                    PackagingType packagingType = new PackagingType(extension, classifier);
                    return new MavenArtifact(groupId, artifactId, version, packagingType);
                });
    }

    private String createQueryParameter(Tuple2<String, String> tuple2) {
        try {
            Assert.hasText(tuple2.getT1(), "key不能为空");
            Assert.hasText(tuple2.getT2(), "value不能为空");
            return tuple2.getT1() + "=" + URLEncoder.encode(tuple2.getT2(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.bubble(e);
        }
    }

    public static MavenRepository createDefaultRemoteInstance() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                throw new RuntimeException(String.format("{} : {}", type, impl), exception);
            }
        });

        RepositorySystem repositorySystem = locator.getService(RepositorySystem.class);

        return new DefaultMavenRepository(repositorySystem, new File(SystemUtils.getUserHome(), ".m2/repository").toString(), "maven.dev.elenet.me", 80);
    }
}
