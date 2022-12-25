package com.cebbus.calibrator.service;

import com.cebbus.calibrator.calculation.result.TestResult;
import com.cebbus.calibrator.domain.Structure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface StructureService extends Service<Structure> {
    Structure generate(Long id);

    Page<Structure> getPage(Specification<Structure> build, PageRequest pageRequest);

    <T> List<T> loadData(Structure structure, boolean training);

    <T> void assignClass(TestResult testResult, String classField, Class<T> clazz);
}
