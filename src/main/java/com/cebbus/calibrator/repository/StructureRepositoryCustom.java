package com.cebbus.calibrator.repository;

public interface StructureRepositoryCustom {
    void createCustomTable(Class<?> clazz);

    void dropCustomTable(Class<?> clazz);

    boolean isPresent(String tableName);
}
