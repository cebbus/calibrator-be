package com.cebbus.calibrator.service;

import com.cebbus.calibrator.domain.Structure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

public interface StructureService extends Service<Structure> {
    Structure generate(Long id);

    Page<Structure> getPage(Specification<Structure> build, PageRequest pageRequest);
}
