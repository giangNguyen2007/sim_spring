package core.context;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


//This scanner loads .class entries from the classpath for a base package.
//        It supports:
//
//        running from IDE/classes directory (protocol: file)
//
//        running from a jar (protocol: jar)
public class ClassPathScanner {

    private final ClassLoader classLoader;

    public ClassPathScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    // returns all classes found in the given base package and its sub-packages
    public Set<Class<?>> scan(String basePackage) {
        try {
            String path = basePackage.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);

            Set<Class<?>> classes = new LinkedHashSet<>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String protocol = url.getProtocol();

                if ("file".equals(protocol)) {
                    classes.addAll(scanFromDirectory(basePackage, url));
                } else if ("jar".equals(protocol)) {
                    classes.addAll(scanFromJar(basePackage, url));
                } else {
                    // Keep v1 minimal: ignore other protocols.
                }
            }
            return classes;
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan package: " + basePackage, e);
        }
    }

    // ============ HELPERS ==============

    private Set<Class<?>> scanFromDirectory(String basePackage, URL url) {
        String dirPath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);
        File root = new File(dirPath);
        if (!root.exists() || !root.isDirectory()) return Set.of();

        Set<Class<?>> classes = new LinkedHashSet<>();
        scanDirRecursive(basePackage, root, classes);
        return classes;
    }

    private void scanDirRecursive(String currentPackage, File dir, Set<Class<?>> out) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                scanDirRecursive(currentPackage + "." + f.getName(), f, out);
            } else if (f.getName().endsWith(".class")) {
                String simple = f.getName().substring(0, f.getName().length() - ".class".length());
                String fqcn = currentPackage + "." + simple;
                tryLoadClass(fqcn).ifPresent(out::add);
            }
        }
    }

    private Set<Class<?>> scanFromJar(String basePackage, URL url) {
        try {
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            try (JarFile jarFile = conn.getJarFile()) {
                String path = basePackage.replace('.', '/');

                Set<Class<?>> classes = new LinkedHashSet<>();
                Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (!name.startsWith(path)) continue;
                    if (!name.endsWith(".class")) continue;
                    if (entry.isDirectory()) continue;

                    String fqcn = name
                            .substring(0, name.length() - ".class".length())
                            .replace('/', '.');

                    tryLoadClass(fqcn).ifPresent(classes::add);
                }

                return classes;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan jar for package: " + basePackage, e);
        }
    }

    private Optional<Class<?>> tryLoadClass(String fqcn) {
        try {
            return Optional.of(Class.forName(fqcn, false, classLoader));
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
            // NoClassDefFoundError can happen if dependencies are missing at runtime.
            return Optional.empty();
        }
    }
}
