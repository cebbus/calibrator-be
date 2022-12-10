package com.cebbus.calibrator.service;

import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.repository.DecisionTreeRepository;
import com.cebbus.calibrator.repository.StructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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

    private void createClassAndTable(Structure structure) {
        Class<?> clazz = customClassOperations.createCustomClass(structure);
        repository.createCustomTable(clazz);
    }

    private void dropClassAndTable(Structure structure) {
        Class<?> clazz = customClassOperations.resolveCustomClass(structure.getClassName());

        customClassOperations.dropCustomClass(clazz);
        repository.dropCustomTable(clazz);
    }
}
