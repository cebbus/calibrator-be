package com.cebbus.calibrator.service;

public interface Service<T> {
    T save(T object);

    T update(T object);

    T get(Long id);

    void delete(Long id);
}
