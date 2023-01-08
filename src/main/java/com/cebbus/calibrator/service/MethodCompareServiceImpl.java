package com.cebbus.calibrator.service;

import com.cebbus.calibrator.calculation.Analysis;
import com.cebbus.calibrator.calculation.result.TestResult;
import com.cebbus.calibrator.common.ClassOperations;
import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.*;
import com.cebbus.calibrator.domain.enums.MethodType;
import com.cebbus.calibrator.repository.MethodCompareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MethodCompareServiceImpl implements MethodCompareService {

    private final ApplicationContext context;
    private final MethodCompareRepository repository;

    private final StructureService structureService;

    @Override
    public MethodCompare save(MethodCompare compare) {
        return repository.save(compare);
    }

    @Override
    public MethodCompare update(MethodCompare compare) {
        return repository.save(compare);
    }

    @Override
    public MethodCompare get(Long id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Page<MethodCompare> getPage(Specification<MethodCompare> specification, PageRequest pageRequest) {
        return repository.findAll(specification, pageRequest);
    }

    @Override
    public <T> void start(DecisionTreeReq request) {
        List<MethodCompare> compareList = new ArrayList<>();

        long structureId = request.getStructureId().longValue();
        Structure structure = structureService.get(structureId);

        List<T> testDataList = structureService.loadData(structure, false);
        List<T> trainingDataList = structureService.loadData(structure, true);

        MethodType[] methodTypes = MethodType.values();
        for (MethodType method : methodTypes) {
            Analysis analysisBean = getAnalysisBean(method);

            MethodCompare compare = new MethodCompare();
            compare.setStructure(structure);
            compare.setMethod(method);
            compare.setTestSize(testDataList.size());
            compare.setTrainingSize(trainingDataList.size());

            compare.setTrainingStart(LocalDateTime.now());
            DecisionTreeItem root = analysisBean.createDecisionTree(structure, trainingDataList);

            DecisionTree tree = new DecisionTree();
            tree.setRoot(root);
            compare.setTrainingEnd(LocalDateTime.now());

            compare.setTestStart(LocalDateTime.now());
            TestResult result = analysisBean.testDecisionTree(structure, testDataList, tree);
            compare.setTestEnd(LocalDateTime.now());

            compare.setNodeWalk(result.getAvgNodeWalk());
            compare.setUnclassifiedDataSize(calculateUnclassifiedData(result));
            compare.setWrongClassifiedDataSize(calculateWrongClassifiedData(structure, result, testDataList));

            compareList.add(compare);
        }

        List<MethodCompare> oldList = repository.findAllByStructure(structure);
        if (!oldList.isEmpty()) {
            repository.deleteAll(oldList);
        }

        repository.saveAll(compareList);
    }

    private Analysis getAnalysisBean(MethodType method) {
        Class<? extends Analysis> analysisClass = method.getAnalysisClass();
        return context.getBean(analysisClass);
    }

    private int calculateUnclassifiedData(TestResult result) {
        return (int) result.getClassValMap().entrySet().stream()
                .filter(e -> e.getValue() == null)
                .count();
    }

    private <T> int calculateWrongClassifiedData(Structure structure, TestResult result, List<T> testDataList) {
        Map<Object, Object> classValMap = result.getClassValMap();

        String classifier = structure.getFields().stream()
                .filter(StructureField::isClassifier)
                .findFirst()
                .orElseThrow()
                .getFieldName();

        int falseRes = 0;
        for (T testData : testDataList) {
            Object id = ClassOperations.getField(testData, "id");
            Object expectedVal = ClassOperations.getField(testData, classifier);
            Object actualVal = classValMap.get(id);

            if (actualVal != null && !actualVal.equals(expectedVal)) {
                falseRes++;
            }
        }

        return falseRes;
    }
}
