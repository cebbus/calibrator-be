package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.domain.DecisionTreeItem;
import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.domain.StructureField;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class CartAnalysis extends BaseAnalysis {

    abstract double calculateGoodness(double[] pArr, List<double[]> pArrList);

    @Override
    void createTree(Structure structure, List<Map<String, Object>> dataRowList, DecisionTreeItem parent) {
        CandidateDivision division = candidateDivision(structure, dataRowList, parent);
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
            List<Map<String, Object>> dataRowList,
            DecisionTreeItem parent) {

        String classAttribute = findClassAttribute(structure);
        Set<Object> classValueSet = createValueSet(dataRowList, classAttribute);

        CandidateDivision division = null;

        for (StructureField field : structure.getFields()) {
            String fieldName = field.getFieldName();
            if (isExcluded(field) || dataRowList.isEmpty() || fieldName.equals(parent.getFieldName())) {
                continue;
            }

            double dataSize = dataRowList.size();
            Set<Object> valueSet = createValueSet(dataRowList, fieldName);
            for (Object value : valueSet) {
                Set<Object> right = new HashSet<>(valueSet);
                right.remove(value);

                CandidateDivision candidateDivision = new CandidateDivision(fieldName, value, right);
                candidateDivision.setLeftDataList(filterDataList(dataRowList, candidateDivision, true));
                candidateDivision.setRightDataList(filterDataList(dataRowList, candidateDivision, false));

                List<Map<String, Object>> leftDataList = candidateDivision.getLeftDataList();
                double leftDataSize = leftDataList.size();

                List<Map<String, Object>> rightDataList = candidateDivision.getRightDataList();
                double rightDataSize = rightDataList.size();

                double[] pArr = {leftDataSize / dataSize, rightDataSize / dataSize};

                List<double[]> pArrList = new ArrayList<>();
                for (Object classValue : classValueSet) {
                    double tcLeft = countAttributeValue(leftDataList, classValue, classAttribute);
                    double tcRight = countAttributeValue(rightDataList, classValue, classAttribute);

                    pArrList.add(new double[]{tcLeft / leftDataSize, tcRight / rightDataSize});
                }

                double goodness = calculateGoodness(pArr, pArrList);
                candidateDivision.setGoodness(goodness);

                log.debug(fieldName + " - " + candidateDivision.getLeft() + " : " + goodness);
                if (division == null || goodness > division.getGoodness()) {
                    division = candidateDivision;
                }
            }
        }

        log.debug("---------------");

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
            createTree(structure, dataList, item);
        }
    }

    private List<Map<String, Object>> filterDataList(
            List<Map<String, Object>> dataRowList,
            CandidateDivision division,
            boolean left) {

        String fieldName = division.getFieldName();

        if (left) {
            Object leftValue = division.getLeft();

            return dataRowList.stream()
                    .filter(r -> leftValue.equals(r.get(fieldName)))
                    .collect(Collectors.toList());
        } else {
            Set<Object> rightValue = division.getRight();
            return dataRowList.stream()
                    .filter(r -> rightValue.contains(r.get(fieldName)))
                    .collect(Collectors.toList());
        }
    }

    private double countAttributeValue(List<Map<String, Object>> dataList, Object value, String attribute) {
        return dataList.stream().filter(r -> value.equals(r.get(attribute))).count();
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
