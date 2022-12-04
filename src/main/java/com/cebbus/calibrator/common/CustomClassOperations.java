package com.cebbus.calibrator.common;

import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.domain.StructureField;
import com.cebbus.calibrator.domain.enums.DataType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CustomClassOperations {

    private static final String PACKAGE = "com.cebbus.calibrator";

    private final Map<Long, URLClassLoader> loaderMap = new ConcurrentHashMap<>();

    private Timer timer = new Timer();

    @Value("${calibrator.home}/custom-classes")
    private String customClassPath;

    public boolean isPresent(String name) {
        String className = PACKAGE + "." + name;

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return ClassUtils.isPresent(className, loader);
    }

    public <T> Class<T> resolveCustomClass(String className) {
        URLClassLoader loader = getLoaderInstance();
        Class<T> clazz = resolveCustomClass(loader, className);
        closeLoader(loader);

        return clazz;
    }

    public <T> Class<T> resolveCustomClass(Long key, String className) {
        URLClassLoader loader = getLoaderInstance(key);
        return resolveCustomClass(loader, className);
    }

    public <T> T newInstance(Long key, String className) {
        try {
            Class<T> clazz = resolveCustomClass(key, className);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void dropCustomClass(Class<?> clazz) {
        File domainDir = getDomainFolder();
        String simpleName = clazz.getSimpleName();

        FileOperations.deleteFile(new File(domainDir, simpleName + ".java"));
        FileOperations.deleteFile(new File(domainDir, simpleName + ".class"));
    }

    public Class<?> createCustomClass(Structure structure) {
        String name = structure.getClassName();
        StringBuilder clazz = new StringBuilder();

        addImports(clazz);
        addAnnotations(clazz, structure);

        clazz.append("public class ").append(name).append(" {\n\n");
        addAttributes(clazz, structure);
        clazz.append("}");

        File javaFile = writeJavaFile(clazz, name);
        compileJavaFile(javaFile);

        return resolveCustomClass(name);
    }

    public synchronized void createLoader(Long key) {
        if (!loaderMap.containsKey(key)) {
            getLoaderInstance(key);
        }

        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                removeLoader(key);
            }
        }, 1000L * 60L);
    }

    public synchronized void removeLoader(Long key) {
        if (loaderMap.containsKey(key)) {
            closeLoader(loaderMap.remove(key));
        }
    }

    public String getJavaClassPath() {
        return getJavaClassPathList().stream().map(File::getPath).collect(Collectors.joining(";"));
    }

    private List<File> getJavaClassPathList() {
        File lombokApi = new File(customClassPath, "lombok-1.18.20.jar");
        File hibernateApi = new File(customClassPath, "hibernate-core-5.4.1.Final.jar");
        File persistenceApi = new File(customClassPath, "javax.persistence-api-2.2.jar");

        return List.of(lombokApi, hibernateApi, persistenceApi);
    }

    public String getCustomClassPath() {
        return new File(customClassPath).getPath();
    }

    private File writeJavaFile(StringBuilder clazz, String className) {
        File domainDir = getDomainFolder();
        if (!domainDir.exists()) {
            domainDir.mkdirs();
        }

        File javaFile = new File(domainDir, className + ".java");
        FileOperations.writeFile(clazz.toString(), javaFile);

        return javaFile;
    }

    private void addImports(StringBuilder clazz) {
        clazz.append("package ").append(PACKAGE).append(";\n\n")
                .append("import lombok.Data;\n")
                .append("import lombok.EqualsAndHashCode;\n")
                .append("import lombok.ToString;\n")
                .append("import java.time.LocalDate;\n")
                .append("import javax.persistence.*;\n\n");
    }

    private void addAnnotations(StringBuilder clazz, Structure structure) {
        clazz.append("@Data\n")
                .append("@EqualsAndHashCode\n")
                .append("@ToString\n")
                .append("@Entity\n")
                .append("@Table(")
                .append("name = \"").append(structure.getTableName()).append("\")\n");
    }

    private void addAttributes(StringBuilder clazz, Structure structure) {

        clazz.append("\t@Id\n")
                .append("\t@GeneratedValue(strategy = GenerationType.IDENTITY)\n")
                .append("\tprivate Long id;\n\n");

        for (StructureField field : structure.getFields()) {
            clazz.append("\t@Column(name = \"")
                    .append(field.getColumnName())
                    .append("\")\n");

            clazz.append("\tprivate ")
                    .append(DataType.getJavaType(field.getType()).getSimpleName()).append(" ")
                    .append(field.getFieldName()).append(";\n\n");
        }
    }

    private <T> Class<T> resolveCustomClass(URLClassLoader loader, String className) {
        String fullName = PACKAGE + "." + className;
        return (Class<T>) ClassUtils.resolveClassName(fullName, loader);
    }

    private synchronized URLClassLoader getLoaderInstance(Long key) {
        return loaderMap.computeIfAbsent(key, id -> getLoaderInstance());
    }

    private synchronized URLClassLoader getLoaderInstance() {
        File root = new File(customClassPath);

        List<File> pathList = new ArrayList<>();
        pathList.add(root);
        pathList.addAll(getJavaClassPathList());

        try {
            URL[] urls = new URL[pathList.size()];
            for (int i = 0; i < pathList.size(); i++) {
                urls[i] = pathList.get(i).toURI().toURL();
            }

            return URLClassLoader.newInstance(urls, this.getClass().getClassLoader());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private File getDomainFolder() {
        File comDir = new File(customClassPath, "com");
        File finecusDir = new File(comDir, "cebbus");
        return new File(finecusDir, "calibrator");
    }

    private void closeLoader(URLClassLoader loader) {
        try {
            loader.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void compileJavaFile(File javaFile) {
        String[] args = {"-cp", getJavaClassPath(), javaFile.getPath()};

        OutputStream errStream = new ByteArrayOutputStream();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, errStream, args);

        if (!errStream.toString().isEmpty()) {
            log.error(errStream.toString());
        }
    }
}
