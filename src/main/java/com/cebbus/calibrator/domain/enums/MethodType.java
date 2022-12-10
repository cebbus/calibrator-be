package com.cebbus.calibrator.domain.enums;

import com.cebbus.calibrator.calculation.Analysis;
import com.cebbus.calibrator.calculation.IdTreeAnalysis;

public enum MethodType {
    ID_3(IdTreeAnalysis.class),
    C4_5(IdTreeAnalysis.class),
    GINI(IdTreeAnalysis.class),
    TWO_ING(IdTreeAnalysis.class);

    private final Class<? extends Analysis> analysisClass;

    MethodType(Class<? extends Analysis> analysisClass) {
        this.analysisClass = analysisClass;
    }

    public Class<? extends Analysis> getAnalysisClass() {
        return analysisClass;
    }
}
