package com.hjzgg.apigateway.main.configuration;

import com.hjzgg.apigateway.beans.constants.Constants;
import com.hjzgg.apigateway.commons.maven.DefaultMavenRepository;
import com.hjzgg.apigateway.commons.maven.MavenArtifact;
import com.hjzgg.apigateway.commons.maven.MavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApigatewayConfigurationLocator implements EnvironmentPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ApigatewayConfigurationLocator.class);

    private MavenRepository mavenRepository = DefaultMavenRepository.createDefaultRemoteInstance();


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        /**
         * 初始化日志系统
         * @see org.springframework.boot.logging.LoggingApplicationListener
         * */
        String logConfig = environment.resolvePlaceholders("${logging.config:}");
        LogFile logFile = LogFile.get(environment);
        LoggingSystem loggingSystem = LoggingSystem.get(application.getClassLoader());
        loggingSystem.initialize(new LoggingInitializationContext(environment), logConfig, logFile);

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
        if (CollectionUtils.isEmpty(apiJarsGav)) {
            throw new IllegalArgumentException(String.format("JAR 资源不合, parameter =% s, value = 0", Constants.API_JARS_GAV));
        }
        List<MavenArtifact> mavenArtifacts = new ArrayList<>();
        apiJarsGav.forEach(apiJarGav -> mavenArtifacts.add(new MavenArtifact(apiJarGav)));
        List<File> files = mavenRepository.resolveArtifactsFiles(mavenArtifacts);
        if (CollectionUtils.isEmpty(files)) {
            throw new IllegalArgumentException("The size of downloaded resource files is 0");
        }
        files = files.stream()
                .filter(file -> {
                    String filePath = file.getAbsolutePath();
                    return filePath.contains("com/hjzgg") || filePath.contains("com\\hjzgg");
                }).collect(Collectors.toList());
        files.forEach(file -> logger.info("download file: {}", file.getAbsolutePath()));
        return files;
    }
}
