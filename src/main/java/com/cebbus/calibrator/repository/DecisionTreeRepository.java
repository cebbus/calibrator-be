package com.cebbus.calibrator.repository;

import com.cebbus.calibrator.domain.DecisionTree;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DecisionTreeRepository extends CrudRepository<DecisionTree, Long>, JpaSpecificationExecutor<DecisionTree> {
    @Modifying
    @Transactional
    @Query("delete from DecisionTree t where t.structure.id=:structureId")
    void deleteByStructureId(@Param("structureId") Long id);
}
