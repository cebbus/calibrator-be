package com.cebbus.calibrator.service;

import com.cebbus.calibrator.domain.Structure;

public interface StructureService extends Service<Structure> {
    Structure generate(Long id);
}
