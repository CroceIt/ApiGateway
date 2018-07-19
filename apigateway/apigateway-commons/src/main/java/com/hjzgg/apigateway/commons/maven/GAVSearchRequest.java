package com.hjzgg.apigateway.commons.maven;

public class GAVSearchRequest {
    private String groupId;
    private String artifactId;
    private String extension;
    private String classifier;
    private String version;

    @Override
    public String toString() {
        return "GAVSearchRequest{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", extension='" + extension + '\'' +
                ", classifier='" + classifier + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    public static GAVSearchRequest create() {
        return new GAVSearchRequest();
    }

    public GAVSearchRequest setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public GAVSearchRequest setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public GAVSearchRequest setExtension(String extension) {
        this.extension = extension;
        return this;
    }

    public GAVSearchRequest setClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    public GAVSearchRequest setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getExtension() {
        return extension;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getVersion() {
        return version;
    }
}
