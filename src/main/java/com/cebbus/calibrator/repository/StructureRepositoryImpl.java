package com.cebbus.calibrator.repository;

import com.cebbus.calibrator.common.CustomClassOperations;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.metamodel.Type;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class StructureRepositoryImpl implements StructureRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CustomClassOperations operations;

    @Override
    public void createCustomTable(Class<?> clazz) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            SchemaExport schemaExport = new SchemaExport();
            schemaExport.create(EnumSet.of(TargetType.STDOUT, TargetType.DATABASE), createSources(clazz));
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }

    @Override
    public void dropCustomTable(Class<?> clazz) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            SchemaExport schemaExport = new SchemaExport();
            schemaExport.drop(EnumSet.of(TargetType.STDOUT, TargetType.DATABASE), createSources(clazz));
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }

    @Override
    public boolean isPresent(String tableName) {
        List<String> tableNameList = entityManager.getMetamodel().getEntities().stream()
                .map(Type::getJavaType)
                .filter(e -> e != null && e.isAnnotationPresent(Table.class))
                .map(e -> e.getAnnotation(Table.class).name())
                .collect(Collectors.toList());

        return tableNameList.contains(tableName);
    }

    private Metadata createSources(Class<?> clazz) {
        Thread.currentThread().setContextClassLoader(clazz.getClassLoader());

        EntityManagerFactory factory = entityManager.getEntityManagerFactory();
        Map<String, Object> propertiesMap = factory.getProperties();

        Properties properties = new Properties();
        properties.putAll(propertiesMap);
        properties.put("java.class.path", operations.getJavaClassPath());
        properties.put("java.library.path", operations.getCustomClassPath());

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(properties).build();
        MetadataSources sources = new MetadataSources(registry);
        sources.addAnnotatedClass(clazz);

        return sources.buildMetadata();
    }
}
