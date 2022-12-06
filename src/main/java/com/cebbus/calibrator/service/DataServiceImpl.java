package com.cebbus.calibrator.service;

import com.cebbus.calibrator.common.ClassOperations;
import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.repository.StructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DataServiceImpl implements DataService {

    private final StructureRepository repository;
    private final CustomClassOperations customClassOperations;


    @Override
    public <T> List<T> list(Class<T> clazz) {
        return repository.list(clazz);
    }

    @Override
    public <T> Page<T> getPage(Specification<T> build, PageRequest pageRequest, Class<T> type) {
        return repository.getPage(build, pageRequest, type);
    }

    @Override
    public <T> T save(T instance) {
        ClassOperations.setField(instance, "id", null);
        return repository.saveOrUpdate(instance);
    }

    @Override
    public <T> T update(T instance) {
        return repository.saveOrUpdate(instance);
    }

    @Override
    public <T> void delete(T instance) {
        repository.remove(instance);
    }
}
