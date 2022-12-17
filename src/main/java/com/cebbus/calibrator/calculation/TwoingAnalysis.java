package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.repository.StructureRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TwoingAnalysis extends CartAnalysis {

    TwoingAnalysis(
            StructureRepository structureRepository,
            CustomClassOperations customClassOperations) {
        super(structureRepository, customClassOperations);
    }

    @Override
    void calculateGoodness(
            List<Map<String, Object>> dataRowList,
            Set<Object> classValueSet,
            String classAttribute,
            CandidateDivision division) {

        Object left = division.getLeft();
        Set<Object> right = division.getRight();
        String fieldName = division.getFieldName();

        List<Map<String, Object>> leftDataList = dataRowList.stream()
                .filter(r -> left.equals(r.get(fieldName)))
                .collect(Collectors.toList());

        List<Map<String, Object>> rightDataList = dataRowList.stream()
                .filter(r -> right.contains(r.get(fieldName)))
                .collect(Collectors.toList());

        double pc = 0d;
        double pLeft = (double) leftDataList.size() / (double) dataRowList.size();
        double pRight = (double) rightDataList.size() / (double) dataRowList.size();

        for (Object classValue : classValueSet) {
            double tcLeft = leftDataList.stream()
                    .filter(r -> classValue.equals(r.get(classAttribute)))
                    .count();

            double tcRight = rightDataList.stream()
                    .filter(r -> classValue.equals(r.get(classAttribute)))
                    .count();

            double pcLeft = tcLeft / leftDataList.size();
            double pcRight = tcRight / rightDataList.size();

            pc += Math.abs(pcLeft - pcRight);
        }

        double goodness = 2 * pLeft * pRight * pc;
        if (Double.isNaN(goodness) || Double.isInfinite(goodness)) {
            division.setGoodness(0d);
        } else {
            division.setGoodness(goodness);
        }

        division.setLeftDataList(leftDataList);
        division.setRightDataList(rightDataList);
    }
}
