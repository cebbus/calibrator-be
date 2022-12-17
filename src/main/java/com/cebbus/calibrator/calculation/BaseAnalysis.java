package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.common.ClassOperations;
import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.DecisionTreeItem;
import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.domain.StructureField;
import com.cebbus.calibrator.repository.StructureRepository;

import java.util.*;
import java.util.stream.Collectors;

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
}
