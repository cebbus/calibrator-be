package com.cebbus.calibrator.domain.enums;

import com.cebbus.calibrator.calculation.*;

public enum MethodType {
    ID_3(IdTreeAnalysis.class),
    C4_5(CFourPointFiveAnalysis.class),
    GINI(GiniAnalysis.class),
    TWO_ING(TwoingAnalysis.class);

    private final Class<? extends Analysis> analysisClass;

    MethodType(Class<? extends Analysis> analysisClass) {
        this.analysisClass = analysisClass;
    }

    public Class<? extends Analysis> getAnalysisClass() {
        return analysisClass;
    }
}
