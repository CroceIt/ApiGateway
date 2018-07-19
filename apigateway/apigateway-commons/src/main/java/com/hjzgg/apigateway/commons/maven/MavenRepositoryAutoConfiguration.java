package com.hjzgg.apigateway.commons.maven;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(MavenRepositoryProperties.class)
public class MavenRepositoryAutoConfiguration {

    @Bean
    public RepositorySystem repositorySystem() {
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

        return locator.getService(RepositorySystem.class);
    }

    @Bean
    public MavenRepository mavenRepository(MavenRepositoryProperties properties) {
        String host = properties.getHost();
        int port = properties.getPort();
        String localDirectory = properties.getLocalDirectory();
        return new DefaultMavenRepository(repositorySystem(), localDirectory, host, port);
    }

}
