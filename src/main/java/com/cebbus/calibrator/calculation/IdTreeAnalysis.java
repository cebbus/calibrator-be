package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.repository.StructureRepository;
import org.springframework.stereotype.Component;

@Component
public class IdTreeAnalysis extends EntropyAnalysis {

    public IdTreeAnalysis(
            StructureRepository structureRepository,
            CustomClassOperations customClassOperations) {
        super(structureRepository, customClassOperations);
    }

}
