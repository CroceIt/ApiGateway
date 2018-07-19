package com.hjzgg.apigateway.commons.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;

public class ApigatewayClassUtils {
    private static java.lang.reflect.Method addURL;
    private static final Logger log = LoggerFactory.getLogger(ApigatewayClassUtils.class);

    static {
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction) () -> {
                addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                return null;
            });
        } catch (PrivilegedActionException e) {
            throw new Error("Can't init classloader!", e);
        }
    }
    public static void addClassPath(String location, ClassLoader classLoader) {
        try {
            Iterator<File> iterator = FileUtils.iterateFiles(new File(location),
                                                             new String[]{"jar"},
                                                             true);
            while (iterator.hasNext()) {
                File jarFile = iterator.next();
                log.debug("Adding {}  to classpath.", jarFile.getAbsolutePath());
                addURL.invoke(classLoader, jarFile.toURI().toURL());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to add " + location + " to classpath!", e);
        }
    }

    public static void addClassPath2(String location, ClassLoader classLoader) {
        try {
            File jarFile = FileUtils.getFile(location);
            if (jarFile.exists() && FilenameUtils.isExtension(jarFile.getAbsolutePath(), "jar")) {
                log.debug("Adding {}  to classpath.", jarFile.getAbsolutePath());
                addURL.invoke(classLoader, jarFile.toURI().toURL());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to add " + location + " to classpath!", e);
        }
    }
}
