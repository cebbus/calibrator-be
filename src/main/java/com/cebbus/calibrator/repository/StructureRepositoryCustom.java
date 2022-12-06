package com.cebbus.calibrator.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface StructureRepositoryCustom {
    void createCustomTable(Class<?> clazz);

    void dropCustomTable(Class<?> clazz);

    boolean isPresent(String tableName);

    <T> List<T> list(Class<T> clazz);

    <T> Page<T> getPage(Specification<T> specification, PageRequest pageRequest, Class<T> clazz);

    <T> T saveOrUpdate(T instance);

    <T> void remove(T instance);
}
