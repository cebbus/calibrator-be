package com.cebbus.calibrator.service;

import com.cebbus.calibrator.calculation.result.TestResult;
import com.cebbus.calibrator.common.ClassOperations;
import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.domain.StructureField;
import com.cebbus.calibrator.repository.DecisionTreeRepository;
import com.cebbus.calibrator.repository.StructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StructureServiceImpl implements StructureService {

    private final StructureRepository repository;
    private final DecisionTreeRepository treeRepository;
    private final CustomClassOperations customClassOperations;

    @Override
    public Structure save(Structure structure) {
        return repository.save(structure);
    }

    @Override
    public Structure update(Structure structure) {
        return repository.save(structure);
    }

    @Override
    public Structure get(Long id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    public void delete(Long id) {
        Structure structure = get(id);
        if (Boolean.TRUE.equals(structure.getCreated())) {
            dropClassAndTable(structure);
        }

        treeRepository.deleteByStructureId(id);
        repository.deleteById(id);
    }

    @Override
    public Structure generate(Long id) {
        Structure structure = get(id);

        if (Boolean.TRUE.equals(structure.getCreated())) { //regenerate
            dropClassAndTable(structure);
        }

        String className = structure.getClassName();
        if (customClassOperations.isPresent(className)) {
            throw new RuntimeException("This class name already exist! Name: " + className);
        }

        String tableName = structure.getTableName();
        if (repository.isPresent(tableName)) {
            throw new RuntimeException("This table name already exist! Name: " + tableName);
        }

        createClassAndTable(structure);

        structure.setCreated(true);
        return repository.save(structure);
    }

    @Override
    public Page<Structure> getPage(Specification<Structure> specification, PageRequest pageRequest) {
        return repository.findAll(specification, pageRequest);
    }

    @Override
    public <T> List<T> loadData(Structure structure, boolean training) {
        Class<T> type = customClassOperations.resolveCustomClass(structure.getClassName());
        List<T> dataList = repository.list(type);

        return filterStructureData(structure, dataList, training);
    }

    @Override
    public <T> void assignClass(TestResult testResult, String classField, Class<T> clazz) {
        repository.updateClass(testResult.getClassValMap(), classField, clazz);
    }

    private void createClassAndTable(Structure structure) {
        Class<?> clazz = customClassOperations.createCustomClass(structure);
        repository.createCustomTable(clazz);
    }

    private void dropClassAndTable(Structure structure) {
        Class<?> clazz = customClassOperations.resolveCustomClass(structure.getClassName());

        customClassOperations.dropCustomClass(clazz);
        repository.dropCustomTable(clazz);
    }

    private <T> List<T> filterStructureData(Structure structure, List<T> dataList, boolean training) {
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
}
