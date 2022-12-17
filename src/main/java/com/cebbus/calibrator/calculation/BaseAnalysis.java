package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.common.ClassOperations;
import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.DecisionTree;
import com.cebbus.calibrator.domain.DecisionTreeItem;
import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.domain.StructureField;
import com.cebbus.calibrator.repository.StructureRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseAnalysis implements Analysis {

    final StructureRepository structureRepository;
    final CustomClassOperations customClassOperations;

    BaseAnalysis(
            StructureRepository structureRepository,
            CustomClassOperations customClassOperations) {
        this.structureRepository = structureRepository;
        this.customClassOperations = customClassOperations;
    }

    abstract void createTree(Structure structure, List<Map<String, Object>> dataRowList, DecisionTreeItem root);

    @Override
    public DecisionTreeItem createDecisionTree(DecisionTreeReq request) {
        long structureId = request.getStructureId().longValue();
        Structure structure = getStructure(structureId);
        List<Object> dataList = loadStructureData(structureId);
        List<Object> trainingDataList = filterStructureData(structure, dataList, true);
        List<Map<String, Object>> convertedDataList = convertStructureData(structure, trainingDataList);

        DecisionTreeItem root = new DecisionTreeItem();
        root.setChildren(new ArrayList<>());

        createTree(structure, convertedDataList, root);

        return root;
    }

    @Override
    public Map<Object, Object> testDecisionTree(DecisionTreeReq request, DecisionTree tree) {
        Map<Object, Object> classValMap = new HashMap<>();

        long structureId = request.getStructureId().longValue();
        Structure structure = getStructure(structureId);
        List<Object> dataList = loadStructureData(structureId);
        List<Object> testDataList = filterStructureData(structure, dataList, false);
        List<Map<String, Object>> convertedDataList = convertStructureData(structure, testDataList);

        List<DecisionTreeItem> itemList = tree.getRoot().getChildren();

        for (Map<String, Object> testData : convertedDataList) {
            AtomicInteger counter = new AtomicInteger();

            Object classValue = findClass(testData, itemList, counter);
            classValMap.put(testData.get("id"), classValue);

            log.debug("count: " + counter.get() + " - class: " + classValue + " - id: " + testData.get("id"));
        }

        return classValMap;
    }

    Structure getStructure(Long structureId) {
        return structureRepository
                .findById(structureId)
                .orElseThrow();
    }

    String findClassAttribute(Structure structure) {
        return structure.getFields().stream()
                .filter(StructureField::isClassifier)
                .findFirst()
                .orElseThrow()
                .getFieldName();
    }

    Set<Object> createValueSet(List<Map<String, Object>> dataList, String attribute) {
        return dataList.stream()
                .map(d -> d.get(attribute))
                .collect(Collectors.toSet());
    }

    <T> List<T> loadStructureData(Long structureId) {
        Structure structure = getStructure(structureId);
        Class<T> type = getType(structure.getClassName());
        return structureRepository.list(type);
    }

    <T> List<T> filterStructureData(Structure structure, List<T> dataList, boolean training) {
        Optional<StructureField> differentiator = structure.getFields().stream()
                .filter(StructureField::isDifferentiator)
                .findFirst();

        if (differentiator.isEmpty()) {
            return dataList;
        } else {
            String fieldName = differentiator.get().getFieldName();
            return dataList.stream().filter(d -> {
                Object value = ClassOperations.getField(d, fieldName);
                if (training) {
                    return Boolean.TRUE.equals(value);
                } else {
                    return !Boolean.TRUE.equals(value);
                }
            }).collect(Collectors.toList());
        }
    }

    <T> List<Map<String, Object>> convertStructureData(Structure structure, List<T> dataList) {
        List<Map<String, Object>> rowList = new ArrayList<>();
        Set<StructureField> fields = structure.getFields();
        for (T data : dataList) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", ClassOperations.getField(data, "id"));

            for (StructureField field : fields) {
                String fieldName = field.getFieldName();
                row.put(fieldName, ClassOperations.getField(data, fieldName));
            }

            rowList.add(row);
        }

        return rowList;
    }

    boolean isExcluded(StructureField field) {
        return field.isDifferentiator() || field.isClassifier() || field.isExcluded();
    }

    double logBase2(double t) {
        return Math.log(t) / Math.log(2d);
    }

    private <T> Class<T> getType(String customClassName) {
        return customClassOperations.resolveCustomClass(customClassName);
    }

    private String findClass(Map<String, Object> testData, List<DecisionTreeItem> itemList, AtomicInteger counter) {
        for (DecisionTreeItem item : itemList) {
            String name = item.getFieldName();
            String value = item.getFieldValue();
            String testValue = testData.get(name).toString();

            if (name == null || name.equals(value)) { //field node
                return findClass(testData, item.getChildren(), counter);
            } else {
                counter.incrementAndGet();

                if (isEqualOrContains(value, testValue)) {
                    if (item.getClassification() != null) {
                        return item.getClassification();
                    } else {
                        return findClass(testData, item.getChildren(), counter);
                    }
                }
            }
        }

        return null;
    }

    private boolean isEqualOrContains(String value, String testValue) {
        if (value.startsWith("[")) {
            return Arrays.stream(value.replace("[", "").replace("]", "").split(","))
                    .map(String::trim)
                    .anyMatch(s -> s.equals(testValue));
        } else {
            return value.equals(testValue);
        }
    }
}
