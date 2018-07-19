package com.hjzgg.apigateway.commons.maven;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;


@ConfigurationProperties("apigateway.maven.repo")
public class MavenRepositoryProperties {
    private String host = "maven.dev.elenet.me";
    private int port = 80;
    private String localDirectory = new File(SystemUtils.getUserHome(), ".m2/repository").toString();

    public String getHost() {
        return host;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public MavenRepositoryProperties setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
        return this;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
