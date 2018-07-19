package com.hjzgg.apigateway.main.configuration;

import com.hjzgg.apigateway.commons.Constants;
import com.hjzgg.apigateway.commons.maven.DefaultMavenRepository;
import com.hjzgg.apigateway.commons.maven.MavenArtifact;
import com.hjzgg.apigateway.commons.maven.MavenRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApigatewayConfigurationLocator implements EnvironmentPostProcessor {

    private MavenRepository mavenRepository = DefaultMavenRepository.createDefaultRemoteInstance();


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        List<File> files = download(environment);
        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> propertySourceMap = new HashMap<>();
        propertySourceMap.put(Constants.API_JARS_FILE_PATH, files.stream().map(file -> file.getAbsolutePath()).collect(Collectors.toList()));
        MapPropertySource mapPropertySource = new MapPropertySource(Constants.API_JARS_FILE, propertySourceMap);
        propertySources.addLast(mapPropertySource);
    }

    private List<File> download(ConfigurableEnvironment environment) {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment);
        List<String> apiJarsGav = propertyResolver.getRequiredProperty(Constants.API_JARS_GAV, List.class);
        List<MavenArtifact> mavenArtifacts = new ArrayList<>();
        apiJarsGav.forEach(apiJarGav -> mavenArtifacts.add(new MavenArtifact(apiJarGav)));
        List<File> files = mavenRepository.resolveClassPath(mavenArtifacts, false);
        files = files.stream().filter(file -> file.getAbsolutePath().contains("com/hjzgg")).collect(Collectors.toList());
        return files;
    }
}
