package com.cebbus.calibrator.repository;

import com.cebbus.calibrator.domain.Structure;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface StructureRepository extends CrudRepository<Structure, Long>, JpaSpecificationExecutor<Structure>, StructureRepositoryCustom {
}
