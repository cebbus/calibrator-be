package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.calculation.result.TestResult;
import com.cebbus.calibrator.domain.DecisionTree;
import com.cebbus.calibrator.domain.DecisionTreeItem;
import com.cebbus.calibrator.domain.Structure;

import java.util.List;

public interface Analysis {
    <T> DecisionTreeItem createDecisionTree(Structure structure, List<T> dataList);

    <T> TestResult testDecisionTree(Structure structure, List<T> dataList, DecisionTree tree);
}
