package com.cebbus.calibrator.service;

import java.util.List;

public interface Service<T> {
    T save(T object);

    T update(T object);

    T get(Long id);

    List<T> list();

    void delete(Long id);
}
