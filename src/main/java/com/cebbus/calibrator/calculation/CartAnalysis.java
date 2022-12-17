package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.domain.DecisionTreeItem;
import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.domain.StructureField;
import com.cebbus.calibrator.repository.StructureRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public abstract class CartAnalysis extends BaseAnalysis {

    CartAnalysis(
            StructureRepository structureRepository,
            CustomClassOperations customClassOperations) {
        super(structureRepository, customClassOperations);
    }

    abstract void calculateGoodness(
            List<Map<String, Object>> dataRowList,
            Set<Object> classValueSet,
            String classAttribute,
            CandidateDivision division);

    @Override
    void createTree(Structure structure, List<Map<String, Object>> dataRowList, DecisionTreeItem parent) {
        CandidateDivision division = candidateDivision(structure, dataRowList);
        if (division == null) {
            return;
        }

        String fieldName = division.getFieldName();

        DecisionTreeItem item = new DecisionTreeItem();
        item.setFieldName(fieldName);
        item.setFieldValue(fieldName);
        item.setChildren(new ArrayList<>());
        item.setParent(parent);
        parent.getChildren().add(item);

        createLeg(structure, item, division, true);
        createLeg(structure, item, division, false);
    }

    CandidateDivision candidateDivision(
            Structure structure,
            List<Map<String, Object>> dataRowList) {

        String classAttribute = findClassAttribute(structure);
        Set<Object> classValueSet = createValueSet(dataRowList, classAttribute);

        CandidateDivision division = null;

        for (StructureField field : structure.getFields()) {
            String fieldName = field.getFieldName();
            if (isExcluded(field) || dataRowList.isEmpty() || !dataRowList.get(0).containsKey(fieldName)) {
                continue;
            }

            Set<Object> valueSet = createValueSet(dataRowList, fieldName);
            for (Object value : valueSet) {
                Set<Object> right = new HashSet<>(valueSet);
                right.remove(value);

                CandidateDivision candidateDivision = new CandidateDivision(fieldName, value, right);
                calculateGoodness(dataRowList, classValueSet, classAttribute, candidateDivision);
                double goodness = candidateDivision.getGoodness();

                log.info(fieldName + " - " + candidateDivision.getLeft() + " : " + goodness);
                if (division == null || goodness > division.getGoodness()) {
                    division = candidateDivision;
                }
            }
        }

        log.info("---------------");

        return division;
    }

    void createLeg(
            Structure structure,
            DecisionTreeItem parent,
            CandidateDivision division,
            boolean left) {

        String fieldName = division.getFieldName();
        String fieldValue = left ? division.getLeft().toString() : division.getRight().toString();

        DecisionTreeItem item = new DecisionTreeItem();
        item.setFieldName(fieldName);
        item.setFieldValue(fieldValue);
        item.setChildren(new ArrayList<>());
        item.setParent(parent);

        parent.getChildren().add(item);

        List<Map<String, Object>> dataList = left ? division.getLeftDataList() : division.getRightDataList();

        String classAttribute = findClassAttribute(structure);
        Set<Object> classSet = createValueSet(dataList, classAttribute);

        if (classSet.size() == 1) {
            item.setClassification(classSet.iterator().next().toString());
        } else {
            if (left || fieldValue.split(",").length == 1) {
                dataList.forEach(r -> r.remove(fieldName));
            }

            createTree(structure, dataList, item);
        }
    }

    @Data
    @RequiredArgsConstructor
    static final class CandidateDivision {
        private final String fieldName;
        private final Object left;
        private final Set<Object> right;

        private double goodness;
        private List<Map<String, Object>> leftDataList;
        private List<Map<String, Object>> rightDataList;
    }
}
