package com.cebbus.calibrator.service;

import com.cebbus.calibrator.calculation.Analysis;
import com.cebbus.calibrator.calculation.result.TestResult;
import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.DecisionTree;
import com.cebbus.calibrator.domain.DecisionTreeItem;
import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.domain.StructureField;
import com.cebbus.calibrator.filter.SpecificationBuilder;
import com.cebbus.calibrator.repository.DecisionTreeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DecisionTreeServiceImpl implements DecisionTreeService {

    private final ApplicationContext context;
    private final CustomClassOperations operations;
    private final DecisionTreeRepository repository;

    private final StructureService structureService;

    @Override
    public DecisionTree createDecisionTree(DecisionTreeReq request) {
        Structure structure = structureService.get(request.getStructureId().longValue());
        List<Object> trainingDataList = structureService.loadData(structure, true);

        Analysis analysis = getAnalysisBean(request);
        DecisionTreeItem root = analysis.createDecisionTree(structure, trainingDataList);

        loadDecisionTree(request).ifPresent(tree -> delete(tree.getId()));
        return create(request, root);
    }

    @Override
    public <T> void testDecisionTree(DecisionTreeReq request, DecisionTree tree) {
        Structure structure = structureService.get(request.getStructureId().longValue());
        List<Object> testDataList = structureService.loadData(structure, false);

        Analysis analysis = getAnalysisBean(request);
        TestResult testResult = analysis.testDecisionTree(structure, testDataList, tree);

        String classField = getField(structure);
        Class<T> customClass = operations.resolveCustomClass(structure.getClassName());
        structureService.assignClass(testResult, classField, customClass);
    }

    @Override
    public DecisionTree save(DecisionTree tree) {
        return repository.save(tree);
    }

    @Override
    public DecisionTree update(DecisionTree tree) {
        return repository.save(tree);
    }

    @Override
    public DecisionTree get(Long id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<DecisionTree> loadDecisionTree(DecisionTreeReq request) {
        Specification<DecisionTree> specification = new SpecificationBuilder<DecisionTree>()
                .with("structure.id", "eq", request.getStructureId())
                .with("method", "eq", request.getMethodType())
                .build();

        return repository.findOne(specification);
    }

    private DecisionTree create(DecisionTreeReq request, DecisionTreeItem root) {
        Structure structure = new Structure();
        structure.setId(request.getStructureId().longValue());

        DecisionTree tree = new DecisionTree();
        tree.setMethod(request.getMethodType());
        tree.setStructure(structure);
        tree.setRoot(root);

        setTreeToChild(tree, root);

        return save(tree);
    }

    private void setTreeToChild(DecisionTree tree, DecisionTreeItem item) {
        item.setDecisionTree(tree);

        if (item.getChildren() != null) {
            for (DecisionTreeItem child : item.getChildren()) {
                setTreeToChild(tree, child);
            }
        }
    }

    private String getField(Structure structure) {
        return structure.getFields().stream()
                .filter(StructureField::isClassifier)
                .findFirst().orElseThrow()
                .getFieldName();
    }

    private Analysis getAnalysisBean(DecisionTreeReq request) {
        Class<? extends Analysis> analysisClass = request.getMethodType().getAnalysisClass();
        return context.getBean(analysisClass);
    }
}
