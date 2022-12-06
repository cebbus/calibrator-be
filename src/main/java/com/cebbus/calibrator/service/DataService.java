package com.cebbus.calibrator.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface DataService {

    <T> List<T> list(Class<T> clazz);

    <T> Page<T> getPage(Specification<T> build, PageRequest pageRequest, Class<T> type);

    <T> T save(T instance);

    <T> T update(T instance);

    <T> void delete(T instance);
}
