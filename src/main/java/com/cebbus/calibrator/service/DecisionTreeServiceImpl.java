package com.cebbus.calibrator.service;

import com.cebbus.calibrator.calculation.Analysis;
import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.DecisionTree;
import com.cebbus.calibrator.domain.DecisionTreeItem;
import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.domain.StructureField;
import com.cebbus.calibrator.filter.SpecificationBuilder;
import com.cebbus.calibrator.repository.DecisionTreeRepository;
import com.cebbus.calibrator.repository.StructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DecisionTreeServiceImpl implements DecisionTreeService {

    private final ApplicationContext context;
    private final DecisionTreeRepository repository;
    private final StructureRepository structureRepository;
    private final CustomClassOperations operations;

    @Override
    public DecisionTree createDecisionTree(DecisionTreeReq request) {
        Class<? extends Analysis> analysisClass = request.getMethodType().getAnalysisClass();
        Analysis analysis = context.getBean(analysisClass);
        DecisionTreeItem root = analysis.createDecisionTree(request);

        loadDecisionTree(request).ifPresent(tree -> delete(tree.getId()));
        return create(request, root);
    }

    @Override
    public <T> void testDecisionTree(DecisionTreeReq request, DecisionTree tree) {
        Class<? extends Analysis> analysisClass = request.getMethodType().getAnalysisClass();
        Analysis analysis = context.getBean(analysisClass);

        String classField = getField(request);
        Class<T> customClass = getType(request);

        Map<Object, Object> classValMap = analysis.testDecisionTree(request, tree);
        structureRepository.updateClass(classValMap, classField, customClass);
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

    private String getField(DecisionTreeReq request) {
        long id = request.getStructureId().longValue();
        Structure structure = structureRepository.findById(id).orElseThrow();

        return structure.getFields().stream()
                .filter(StructureField::isClassifier)
                .findFirst().orElseThrow()
                .getFieldName();
    }

    private <T> Class<T> getType(DecisionTreeReq request) {
        long id = request.getStructureId().longValue();
        Structure structure = structureRepository.findById(id).orElseThrow();
        return operations.resolveCustomClass(structure.getClassName());
    }
}
