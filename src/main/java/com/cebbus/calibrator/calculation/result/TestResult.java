package com.cebbus.calibrator.calculation.result;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@RequiredArgsConstructor
public class TestResult {
    private final Double avgNodeWalk;
    private final Map<Object, Object> classValMap;
}
