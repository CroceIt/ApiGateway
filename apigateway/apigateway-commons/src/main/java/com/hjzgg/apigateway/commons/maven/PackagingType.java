package com.hjzgg.apigateway.commons.maven;

import java.util.Objects;

import static org.springframework.util.StringUtils.isEmpty;
import static org.springframework.util.StringUtils.trimWhitespace;

/**
 * @author junzheng.hu  Date: 4/25/16 Time: 16:35
 */
public class PackagingType {
    public static final PackagingType JAR = new PackagingType("jar", "");
    private final String extension;
    private final String classifier;

    public PackagingType(final String extension) {
        this(extension, "");
    }

    public PackagingType(final String extension, final String classifier) {
        this.extension = trimWhitespace(extension);
        this.classifier = trimWhitespace(classifier);
    }

    public boolean hasClassifier() {
        return !isEmpty(classifier);
    }

    /**
     * Returns extension for packaging. Might be the same as id;
     *
     * @return Extension for packaging.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Returns classifier for packaging. Might be empty string.
     *
     * @return Classifier for packaging.
     */
    public String getClassifier() {
        return classifier;
    }


    public String toCanonicalForm() {
        return hasClassifier() ? extension + MavenArtifact.SEPARATOR_COORDINATE + classifier : extension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackagingType that = (PackagingType) o;
        return Objects.equals(extension, that.extension) &&
                Objects.equals(classifier, that.classifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extension, classifier);
    }

    @Override
    public String toString() {
        return "PackagingType{" +
                "extension='" + extension + '\'' +
                ", classifier='" + classifier + '\'' +
                '}';
    }
}
