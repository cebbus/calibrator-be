package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.common.ClassOperations;
import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.domain.StructureField;
import com.cebbus.calibrator.repository.StructureRepository;

import java.util.List;
import java.util.Set;
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

    Set<Object> createValueSet(List<Object> dataList, String attribute) {
        return dataList.stream()
                .map(d -> ClassOperations.getField(d, attribute))
                .collect(Collectors.toSet());
    }

    <T> List<T> loadStructureData(Long structureId) {
        Structure structure = getStructure(structureId);
        Class<T> type = getType(structure.getClassName());
        return structureRepository.list(type);
    }

    double logBase2(double t) {
        return Math.log(t) / Math.log(2d);
    }

    private <T> Class<T> getType(String customClassName) {
        return customClassOperations.resolveCustomClass(customClassName);
    }
}
