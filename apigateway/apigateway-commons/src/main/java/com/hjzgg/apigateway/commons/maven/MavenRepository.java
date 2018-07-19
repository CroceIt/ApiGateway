package com.hjzgg.apigateway.commons.maven;

import org.eclipse.aether.resolution.ArtifactResolutionException;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;


public interface MavenRepository {

    default List<File> resolveClassPath(List<MavenArtifact> artifacts, boolean offline) {
        return resolveClassPath(offline, artifacts.toArray(new MavenArtifact[]{}));
    }

    /**
     * 解析所有的依赖（包括间接依赖），并下载至本地仓库。然后返回所有依赖在本地仓库的文件列表
     *
     * @see MavenRepositoryProperties#getLocalDirectory()
     */
    List<File> resolveClassPath(boolean offline, MavenArtifact... artifacts);

    /**
     * 按照类名搜索Artifact，
     * 返回所有匹配的Artifact，如果两个Artifact相同，按照version排序，最新的排在前面。
     *
     * @param className 类名
     * @return 排序后的Artifact
     */
    List<MavenArtifact> searchByClassName(String className, Predicate<PackagingType> typePredicate);

    List<MavenArtifact> searchByGAV(GAVSearchRequest GAVSearchRequest, Predicate<PackagingType> typePredicate);

    /**
     * 解析具体Artifact
     * @param artifact
     * @return
     * @throws ArtifactResolutionException
     */
    File resolveArtifactFile(MavenArtifact artifact) throws ArtifactResolutionException;
}