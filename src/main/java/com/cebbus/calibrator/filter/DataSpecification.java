package com.cebbus.calibrator.filter;

import com.cebbus.calibrator.common.LocalDateOperations;
import com.cebbus.calibrator.domain.Base;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DataSpecification<T> implements Specification<T> {

    private SearchCriteria criteria;

    public DataSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder builder) {
        String operation = criteria.getOperation();
        String key = criteria.getKey();
        Object value = criteria.getValue();

        if (key == null) {
            List<SearchCriteria> criteriaList = (List<SearchCriteria>) value;
            Predicate[] predicates = new Predicate[criteriaList.size()];

            for (int i = 0; i < criteriaList.size(); i++) {
                DataSpecification<T> specification = new DataSpecification<>(criteriaList.get(i));
                predicates[i] = specification.toPredicate(root, query, builder);
            }

            switch (operation) {
                case "or":
                    return builder.or(predicates);
                case "and":
                    return builder.and(predicates);
                default:
                    return null;
            }
        }

        Class<?> type = getType(root, key);

        //FIXME migrate to relational operator enum
        switch (operation) {
            case ">":
            case "gt":
                if (type == LocalDate.class) {
                    return builder.greaterThan(getPath(root, key), LocalDateOperations.objectToLocalDate(value));
                } else {
                    return builder.gt(getPath(root, key), (Number) value);
                }
            case ">=":
                if (type == LocalDate.class) {
                    return builder.greaterThanOrEqualTo(getPath(root, key), LocalDateOperations.objectToLocalDate(value));
                } else {
                    return builder.ge(getPath(root, key), (Number) value);
                }
            case "<":
            case "lt":
                if (type == LocalDate.class) {
                    return builder.lessThan(getPath(root, key), LocalDateOperations.objectToLocalDate(value));
                } else {
                    return builder.lt(getPath(root, key), (Number) value);
                }
            case "<=":
                if (type == LocalDate.class) {
                    return builder.lessThanOrEqualTo(getPath(root, key), LocalDateOperations.objectToLocalDate(value));
                } else {
                    return builder.le(getPath(root, key), (Number) value);
                }
            case "eq":
            case "==":
            case "=":
                return createEqExpression(root, builder, key, value, type);
            case "ne":
            case "!=":
            case "<>":
                return createEqExpression(root, builder, key, value, type).not();
            case "like":
            case "contains":
                return builder.like(getPath(root, key), "%" + value + "%");
            case "not contains":
                return builder.like(getPath(root, key), "%" + value + "%").not();
            case "begins":
                return builder.like(getPath(root, key), value + "%");
            case "not begins":
                return builder.like(getPath(root, key), value + "%").not();
            case "ends":
                return builder.like(getPath(root, key), "%" + value);
            case "not ends":
                return builder.like(getPath(root, key), "%" + value).not();
            case "in":
                if (!value.toString().equals("[]")) {
                    return createInExpression(root, key, valueToList(value));
                }
                break;
            case "not in":
                if (!value.toString().equals("[]")) {
                    return createInExpression(root, key, valueToList(value)).not();
                }
                break;
            case "between":
                List<String> split = valueToList(value);

                if (type == LocalDate.class) {
                    return builder.between(getPath(root, key),
                            LocalDateOperations.objectToLocalDate(split.get(0)),
                            LocalDateOperations.objectToLocalDate(split.get(1)));
                } else {
                    return builder.between(getPath(root, key),
                            Double.parseDouble(split.get(0)),
                            Double.parseDouble(split.get(1)));
                }
            case "isNotEmpty":
                return builder.isNotEmpty(getPath(root, key));
            case "isNotNull":
                return builder.isNotNull(getPath(root, key));
            case "isNull":
                return builder.isNull(getPath(root, key));
            default:
                return builder.equal(getPath(root, key), value);
        }

        return null;
    }

    private Predicate createEqExpression(
            Root<T> root,
            CriteriaBuilder builder,
            String key,
            Object value,
            Class<?> type) {

        if (value == null) {
            return builder.isNull(getPath(root, key));
        }

        if (type == LocalDate.class && !LocalDate.class.isAssignableFrom(value.getClass())) {
            return builder.equal(getPath(root, key), LocalDateOperations.objectToLocalDate(value));
        } else {
            if (type.isEnum()) {

                for (Object constant : type.getEnumConstants()) {
                    if (constant.toString().equals(value)) {
                        return builder.equal(getPath(root, key), constant);
                    }
                }

            }

            return builder.equal(getPath(root, key), value);
        }
    }

    private List<String> valueToList(Object value) {
        return Arrays.asList(value.toString().replaceAll("\\[|\\]", "").split(", "));
    }

    public Predicate createInExpression(Root<T> root, String key, List<String> valueList) {
        Path<Object> path = getPath(root, key);
        Class<?> type = path.getJavaType();

        boolean isValuesLong;
        try {
            Long.parseLong(valueList.get(0));
            isValuesLong = true;
        } catch (Exception e) {
            isValuesLong = false;
        }

        if (type.isEnum()) {
            List<Object> enumList = new ArrayList<>();
            Object[] constants = type.getEnumConstants();

            for (String value : valueList) {
                for (Object constant : constants) {
                    if (constant.toString().equals(value)) {
                        enumList.add(constant);
                        break;
                    }
                }
            }

            return path.in(enumList);
        } else if (type == LocalDate.class) {
            return path.in(valueList.stream().map(LocalDateOperations::objectToLocalDate).collect(Collectors.toList()));
        } else if (Base.class.isAssignableFrom(type) && isValuesLong) {
            return path.get("id").in(valueList);
        } else {
            return path.in(valueList);
        }
    }

    private <P, R> Path<P> getPath(Root<R> root, String key) {
        Path<P> path;

        if (key.contains(".")) {
            String[] keys = key.split("\\.");
            path = joinOrGetPath(root, keys[0]);

            for (int i = 1; i < keys.length; i++) {
                path = path.get(keys[i]);
            }
        } else {
            path = joinOrGetPath(root, key);
        }

        return path;
    }

    private <P, R> Path<P> joinOrGetPath(Root<R> root, String key) {
        return Collection.class.isAssignableFrom(root.get(key).getJavaType()) ? root.join(key) : root.get(key);
    }

    private Class<?> getType(Root<?> root, String key) {
        return getPath(root, key).getJavaType();
    }
}
