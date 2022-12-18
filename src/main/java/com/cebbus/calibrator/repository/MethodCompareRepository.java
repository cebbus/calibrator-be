package com.cebbus.calibrator.repository;

import com.cebbus.calibrator.domain.MethodCompare;
import com.cebbus.calibrator.domain.Structure;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MethodCompareRepository extends CrudRepository<MethodCompare, Long>, JpaSpecificationExecutor<MethodCompare> {
    List<MethodCompare> findAllByStructure(Structure structure);
}
