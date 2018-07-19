package com.hjzgg.apigateway.commons.maven;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * @author junzheng.hu  Date: 4/25/16 Time: 16:37
 */

public class MavenArtifact implements Comparable<MavenArtifact> {

    private static final Pattern DEPENDENCY_PATTERN = Pattern
            .compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?(:([^: ]+))?");
    static final char SEPARATOR_COORDINATE = ':';

    private static final int IS_POS_1 = 1;
    private static final int ID_POS_2 = 2;
    private static final int ID_POS_3 = 4;
    private static final int ID_POS_4 = 6;
    private static final int ID_POS_5 = 8;

    private final String version;
    private final String groupId;
    private final String artifactId;
    private final PackagingType packaging;

    /**
     * 默认创建 {@link PackagingType#JAR} 类型的Artifact
     */
    public MavenArtifact(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, PackagingType.JAR);
    }

    public MavenArtifact(String groupId, String artifactId, String version, PackagingType packaging) {
        Assert.hasText(version, "version can't not be empty!");
        Assert.hasText(groupId, "groupId can't not be empty!");
        Assert.hasText(artifactId, "artifactId can't not be empty!");
        Assert.notNull(packaging, "packaging can't be null!");
        this.version = version;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.packaging = packaging;
    }

    public MavenArtifact(String gav) {
        if (gav == null || gav.length() == 0) {
            throw new IllegalArgumentException("canonical form is required");
        }
        final Matcher m = DEPENDENCY_PATTERN.matcher(gav);
        if (!m.matches()) {
            throw new IllegalArgumentException("Bad artifact coordinates"
                    + ", expected format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:(<version>|'?'), got: "
                    + gav);
        }

        groupId = m.group(IS_POS_1);
        artifactId = m.group(ID_POS_2);

        final String position3 = m.group(ID_POS_3);
        final String position4 = m.group(ID_POS_4);
        final String position5 = m.group(ID_POS_5);

        // some logic with numbers of provided groups
        final int noOfColons = numberOfOccurrences(gav, MavenArtifact.SEPARATOR_COORDINATE);

        // Parsing is segment-dependent
        switch (noOfColons) {
            case 2:
                version = position3;
                packaging = PackagingType.JAR;
                break;
            case 3:
                packaging = isEmpty(position3) ? PackagingType.JAR
                        : new PackagingType(position3);
                version = isEmpty(position4) ? "LATEST" : position4;
                break;
            default:
                packaging = isEmpty(position3) ? PackagingType.JAR
                        : new PackagingType(position3, position4);
                version = isEmpty(position5) ? "LATEST" : position5;
        }

    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public final PackagingType getType() {
        return this.packaging;
    }

    public final String getVersion() {
        return this.version;
    }

    public final String toCanonicalForm() {
        return groupId + SEPARATOR_COORDINATE +
                artifactId + SEPARATOR_COORDINATE + packaging.toCanonicalForm() + SEPARATOR_COORDINATE + version;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "MavenArtifact{" +
                "version='" + version + '\'' +
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", packaging=" + packaging +
                '}';
    }

    private static int numberOfOccurrences(final CharSequence haystack, char needle) {
        int counter = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * 不比较version
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MavenArtifact artifact = (MavenArtifact) o;
        return Objects.equals(groupId, artifact.groupId) &&
                Objects.equals(artifactId, artifact.artifactId) &&
                Objects.equals(packaging, artifact.packaging);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, packaging);
    }

    /**
     * 如果两个Artifact不相等 {@link this#equals(Object)}，返回-1。否则比较两者的version
     */
    @Override
    public int compareTo(MavenArtifact o) {
        if (!this.equals(o)) {
            return -1;
        }
        ComparableVersion v1 = new ComparableVersion(version);
        ComparableVersion v2 = new ComparableVersion(o.version);
        return v1.compareTo(v2);
    }
}
