package com.cebbus.calibrator.repository;

import com.cebbus.calibrator.common.CustomClassOperations;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Type;
import javax.sql.DataSource;
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
    @Autowired
    private DataSource dataSource;

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

    @Override
    public <T> List<T> list(Class<T> clazz) {
        try (
                SessionFactory sessionFactory = getSessionFactory(clazz);
                Session session = sessionFactory.getCurrentSession()
        ) {
            session.beginTransaction();

            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> query = builder.createQuery(clazz);
            Root<T> root = query.from(clazz);
            query.select(root);

            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public <T> Page<T> getPage(Specification<T> specification, PageRequest pageRequest, Class<T> clazz) {
        try (
                SessionFactory sessionFactory = getSessionFactory(clazz);
                Session session = sessionFactory.getCurrentSession()
        ) {
            session.beginTransaction();

            Long dataCount = countData(session, specification, clazz);
            List<T> dataList = listData(session, specification, pageRequest, clazz);

            return new PageImpl<>(dataList, pageRequest, dataCount);
        }
    }

    @Override
    public <T> T saveOrUpdate(T instance) {
        try (
                SessionFactory sessionFactory = getSessionFactory(instance.getClass());
                Session session = sessionFactory.getCurrentSession()
        ) {
            session.beginTransaction();
            session.saveOrUpdate(instance);
            session.getTransaction().commit();

            return instance;
        }
    }

    @Override
    public <T> void remove(T instance) {
        try (
                SessionFactory sessionFactory = getSessionFactory(instance.getClass());
                Session session = sessionFactory.getCurrentSession()
        ) {
            session.beginTransaction();
            session.delete(instance);
            session.getTransaction().commit();
        }
    }

    private <T> Long countData(Session session, Specification<T> specification, Class<T> clazz) {
        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<Long> query = builder.createQuery(Long.class);

        Root<T> root = query.from(clazz);
        query.select(builder.count(root));
        query.where(specification.toPredicate(root, query, builder));

        return session.createQuery(query)
                .getSingleResult();
    }

    private <T> List<T> listData(
            Session session,
            Specification<T> specification,
            PageRequest pageRequest,
            Class<T> clazz) {
        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<T> query = builder.createQuery(clazz);

        Root<T> root = query.from(clazz);
        query.select(root);
        query.where(specification.toPredicate(root, query, builder));
        query.orderBy(createOrderList(pageRequest.getSort(), root));

        int pageNumber = pageRequest.getPageNumber();
        int pageSize = pageRequest.getPageSize();

        return session.createQuery(query)
                .setFirstResult(pageNumber * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    private <D> List<Order> createOrderList(Sort orders, Root<D> from) {
        return orders.stream()
                .map(o -> new OrderImpl(from.get(o.getProperty()), o.isAscending()))
                .collect(Collectors.toList());
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

    private <T> SessionFactory getSessionFactory(Class<T> clazz) {
        EntityManagerFactory factory = entityManager.getEntityManagerFactory();
        Map<String, Object> propertiesMap = factory.getProperties();
        Properties properties = new Properties();
        properties.putAll(propertiesMap);
        properties.put("hibernate.current_session_context_class", "thread");
        properties.put("java.class.path", operations.getJavaClassPath());
        properties.put("java.library.path", operations.getCustomClassPath());

        return new LocalSessionFactoryBuilder(dataSource, clazz.getClassLoader())
                .addAnnotatedClass(clazz)
                .addProperties(properties)
                .buildSessionFactory();
    }
}
