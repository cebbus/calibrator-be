package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.common.ClassOperations;
import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.DecisionTreeItem;
import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.domain.StructureField;
import com.cebbus.calibrator.repository.StructureRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class IdTreeAnalysis extends BaseAnalysis {

    public IdTreeAnalysis(
            StructureRepository structureRepository,
            CustomClassOperations customClassOperations) {
        super(structureRepository, customClassOperations);
    }

    @Override
    public DecisionTreeItem createDecisionTree(DecisionTreeReq request) {
        long structureId = request.getStructureId().longValue();
        Structure structure = getStructure(structureId);
        List<Object> dataList = loadStructureData(structureId);
        List<Object> trainingDataList = filterStructureData(structure, dataList, true);

        DecisionTreeItem root = new DecisionTreeItem();
        root.setChildren(new ArrayList<>());

        createTree(structure, trainingDataList, root);

        return root;
    }

    private void createTree(Structure structure, List<Object> dataList, DecisionTreeItem parent) {
        String classAttribute = findClassAttribute(structure);
        Map<String, Double> gainMap = calculateGainMap(structure, dataList);

        String maxGainField = gainMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow().getKey();

        DecisionTreeItem item = new DecisionTreeItem();
        item.setFieldName(maxGainField);
        item.setFieldValue(maxGainField);
        item.setChildren(new ArrayList<>());
        item.setParent(parent);
        parent.getChildren().add(item);

        Set<Object> valueSet = createValueSet(dataList, maxGainField);
        Map<Object, List<Object>> valueMap = createAttributeMap(dataList, maxGainField);

        for (Object value : valueSet) {
            List<Object> subDataList = valueMap.get(value);

            DecisionTreeItem subItem = new DecisionTreeItem();
            subItem.setFieldName(maxGainField);
            subItem.setFieldValue(value.toString());
            subItem.setParent(item);
            subItem.setChildren(new ArrayList<>());
            item.getChildren().add(subItem);

            Set<Object> classSet = createValueSet(subDataList, classAttribute);
            if (classSet.size() == 1) {
                subItem.setClassification(classSet.iterator().next().toString());
            } else {
                createTree(structure, subDataList, subItem);
            }
        }
    }

    private Map<String, Double> calculateGainMap(Structure structure, List<Object> dataList) {
        String classAttribute = findClassAttribute(structure);
        Set<Object> classValueSet = createValueSet(dataList, classAttribute);
        double classEntropy = calculateEntropy(dataList, classValueSet, classAttribute);

        Map<String, Double> gainMap = new HashMap<>();
        for (StructureField field : structure.getFields()) {
            String fieldName = field.getFieldName();
            if (field.isDifferentiator()
                    || field.isClassifier()
                    || field.isExcluded()) {
                continue;
            }

            double entropy = 0;
            Map<Object, List<Object>> attributeMap = createAttributeMap(dataList, fieldName);
            for (List<Object> list : attributeMap.values()) {
                double p = (double) list.size() / (double) dataList.size();
                entropy += p * calculateEntropy(list, classValueSet, classAttribute);
            }

            gainMap.put(fieldName, classEntropy - entropy);
        }

        return gainMap;
    }

    private double calculateEntropy(List<Object> dataList, Set<Object> classValueSet, String classAttribute) {
        double entropy = 0d;
        Map<Object, List<Object>> classValueMap = createAttributeMap(dataList, classAttribute);

        for (Object value : classValueSet) {
            if (classValueMap.containsKey(value)) {
                double p = (double) classValueMap.get(value).size() / (double) dataList.size();
                entropy += -1d * (p * logBase2(p));
            }
        }

        return entropy;
    }

    private Map<Object, List<Object>> createAttributeMap(List<Object> dataList, String fieldName) {
        Map<Object, List<Object>> map = new HashMap<>();
        for (Object data : dataList) {
            Object attr = ClassOperations.getField(data, fieldName);
            map.computeIfAbsent(attr, o -> new ArrayList<>()).add(data);
        }

        return map;
    }

}
