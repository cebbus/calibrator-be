package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.common.ClassOperations;
import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.domain.StructureField;
import com.cebbus.calibrator.domain.enums.DataType;
import com.cebbus.calibrator.repository.StructureRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CFourPointFiveAnalysis extends EntropyAnalysis {

    public CFourPointFiveAnalysis(
            StructureRepository structureRepository,
            CustomClassOperations customClassOperations) {
        super(structureRepository, customClassOperations);
    }

    @Override
    <T> List<Map<String, Object>> convertStructureData(Structure structure, List<T> dataList) {
        List<DataType> numericTypeList = DataType.numericTypeList();

        boolean isNumericFieldExists = structure.getFields().stream()
                .anyMatch(f -> !f.isExcluded() && numericTypeList.contains(f.getType()));

        if (!isNumericFieldExists) {
            return super.convertStructureData(structure, dataList);
        } else {
            Set<StructureField> fields = structure.getFields();
            Map<String, Double> meanMap = createMeanMap(fields, dataList);

            List<Map<String, Object>> rowList = new ArrayList<>();

            for (T data : dataList) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", ClassOperations.getField(data, "id"));

                for (StructureField field : fields) {
                    String fieldName = field.getFieldName();
                    Object fieldValue = ClassOperations.getField(data, fieldName);

                    if (meanMap.containsKey(fieldName)) {
                        double value = ((Number) fieldValue).doubleValue();
                        double meanValue = meanMap.get(fieldName);

                        row.put(fieldName, (value > meanValue) ? "gt" : "lte");
                    } else {
                        row.put(fieldName, fieldValue);
                    }
                }

                rowList.add(row);
            }

            return rowList;
        }
    }

    private <T> Map<String, Double> createMeanMap(Set<StructureField> fields, List<T> dataList) {
        Map<String, Double> meanMap = new HashMap<>();

        List<StructureField> numericFieldList = fields.stream()
                .filter(f -> !f.isExcluded() && DataType.numericTypeList().contains(f.getType()))
                .collect(Collectors.toList());

        for (StructureField field : numericFieldList) {
            String fieldName = field.getFieldName();

            List<Number> numericDataList = dataList.stream()
                    .map(d -> ClassOperations.getField(d, fieldName))
                    .map(v -> (Number) v)
                    .sorted().collect(Collectors.toList());

            double mean;
            if (dataList.size() % 2 == 0) {
                int index = (dataList.size() / 2) - 1;
                double first = numericDataList.get(index).doubleValue();
                double second = numericDataList.get(index + 1).doubleValue();
                mean = (first + second) / 2;
            } else {
                int index = ((dataList.size() + 1) / 2) - 1;
                mean = numericDataList.get(index).doubleValue();
            }

            meanMap.put(fieldName, mean);
        }

        return meanMap;
    }
}
