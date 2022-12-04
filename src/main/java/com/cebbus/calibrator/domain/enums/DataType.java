package com.cebbus.calibrator.domain.enums;

import java.time.LocalDate;

public enum DataType {
    STRING,
    INTEGER,
    DOUBLE,
    DATE,
    BOOLEAN;

    public static Class<?> getJavaType(DataType dataType) {
        if (dataType.equals(DATE)) {
            return LocalDate.class;
        } else if (dataType.equals(STRING)) {
            return String.class;
        } else if (dataType.equals(DOUBLE)) {
            return Double.class;
        } else if (dataType.equals(INTEGER)) {
            return Integer.class;
        } else if (dataType.equals(BOOLEAN)) {
            return Boolean.class;
        }

        throw new IllegalArgumentException("Type not found. Type: " + dataType);
    }
}
