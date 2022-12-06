package com.cebbus.calibrator.domain.enums;

import java.time.LocalDate;
import java.util.function.Function;

public enum DataType {
    STRING(String::new),
    INTEGER(Integer::parseInt),
    DOUBLE(Double::parseDouble),
    DATE(LocalDate::parse),
    BOOLEAN(Boolean::parseBoolean);

    private final Function<String, Object> parser;

    DataType(Function<String, Object> parser) {
        this.parser = parser;
    }

    public Function<String, Object> getParser() {
        return parser;
    }

    public static DataType valueOf(Class<?> type) {
        if (type.equals(LocalDate.class)) {
            return DATE;
        } else if (type.equals(String.class)) {
            return STRING;
        } else if (type.equals(Double.class)) {
            return DOUBLE;
        } else if (type.equals(Integer.class)) {
            return INTEGER;
        } else if (type.equals(Boolean.class)) {
            return BOOLEAN;
        }

        throw new IllegalArgumentException("Type not found. Type: " + type);
    }

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
