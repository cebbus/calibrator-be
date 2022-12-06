package com.cebbus.calibrator.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@Slf4j
public class ClassOperations {

    private ClassOperations() {
    }

    public static Field findField(Class<?> clazz, String name) {
        Field field = ReflectionUtils.findField(clazz, name);

        return Optional.ofNullable(field).orElseThrow(() -> {
            throw new IllegalArgumentException("Field could not be found! Field: " + name + " Class: " + clazz);
        });
    }

    public static Object getField(Object object, String name) {
        return getField(object, findField(object.getClass(), name));
    }

    public static Object getField(Object object, Field field) {
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, object);
    }

    public static void setField(Object object, String name, Object value) {
        setField(object, findField(object.getClass(), name), value);
    }

    public static void setField(Object object, Field field, Object value) {
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, object, value);
    }

    public static <T> T createInstance(Class<T> clazz) {
        return createInstance(clazz, null, null);
    }

    public static <T> T createInstance(Class<T> clazz, Class<?>[] parameterTypes, Object[] initArgs) {
        try {
            if (parameterTypes == null) {
                return clazz.getDeclaredConstructor().newInstance();
            } else {
                return clazz.getDeclaredConstructor(parameterTypes).newInstance(initArgs);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
