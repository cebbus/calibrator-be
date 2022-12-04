package com.cebbus.calibrator.repository;

import com.cebbus.calibrator.domain.Structure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StructureRepository extends JpaRepository<Structure, Long>, StructureRepositoryCustom {
}
