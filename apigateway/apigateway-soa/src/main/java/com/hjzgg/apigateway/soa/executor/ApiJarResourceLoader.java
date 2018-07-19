package com.hjzgg.apigateway.soa.executor;

import com.alibaba.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author junzheng.hu  Date: 6/22/16 Time: 18:02
 */
public class ApiJarResourceLoader {
    private static final Logger logger = LoggerFactory.getLogger(ApiJarResourceLoader.class);
    private final ClassLoader resourceClassLoader;
    private final CachingMetadataReaderFactory metadataReaderFactory;

    public ApiJarResourceLoader(ClassLoader resourceClassLoader) {
        this.resourceClassLoader = resourceClassLoader;
        metadataReaderFactory = new CachingMetadataReaderFactory(resourceClassLoader);
    }

    public List<Class<?>> loadResources(File file) throws IOException {
        logger.debug("Finding in {} for resources.", file.getAbsoluteFile());
        try (JarFile jarFile = new JarFile(file)) {
            Set<Class<?>> resourceClasses = new HashSet<>();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.isDirectory()) {
                    continue;
                }
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    ClassMetadata classMetadata = getClassMetadata(jarFile, jarEntry);
                    try {
                        Class<?> resourceClass = resourceClassLoader.loadClass(classMetadata.getClassName());
                        if (!resourceClass.isInterface() || resourceClass.isAnnotation() || !Modifier.isPublic(resourceClass.getModifiers())) {
                            logger.debug("Skipping non-public or non-interface {}", classMetadata.getClassName());
                            continue;
                        }
                        try {
                            resourceClass.getAnnotation(Service.class).version();
                        } catch (Exception e) {
                            if (org.apache.commons.lang3.StringUtils.isBlank(resourceClass.getAnnotation(Service.class).version())) {
                                logger.error(String.format("%s %s %s", ClassUtils.getQualifiedName(resourceClass), ClassUtils.getQualifiedName(Service.class), "version value is blank."));
                            } else {
                                logger.error("{} {} {}", ClassUtils.getQualifiedName(resourceClass), ClassUtils.getQualifiedName(Service.class), e.getMessage());
                            }
                            continue;
                        }
                        resourceClasses.add(resourceClass);
                    } catch (ClassNotFoundException e) {
                        throw new CannotLoadBeanClassException(jarFile.getName(), "Unknown", classMetadata.getClassName(), e);
                    }
                }
            }
            return resourceClasses.stream().collect(Collectors.toList());
        }
    }

    private ClassMetadata getClassMetadata(JarFile jarFile, JarEntry entry) throws IOException {
        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(new InputStreamResource(inputStream));
            return metadataReader.getClassMetadata();
        }

    }
}

