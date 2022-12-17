package com.cebbus.calibrator.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public interface StructureRepositoryCustom {
    void createCustomTable(Class<?> clazz);

    void dropCustomTable(Class<?> clazz);

    boolean isPresent(String tableName);

    <T> List<T> list(Class<T> clazz);

    <T> Page<T> getPage(Specification<T> specification, PageRequest pageRequest, Class<T> clazz);

    <T> T saveOrUpdate(T instance);

    <T> void remove(T instance);

    <T> void updateClass(Map<Object, Object> valueMap, String classField, Class<T> clazz);
}
