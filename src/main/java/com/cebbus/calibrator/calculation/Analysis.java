package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.domain.DecisionTree;
import com.cebbus.calibrator.domain.DecisionTreeItem;
import com.cebbus.calibrator.domain.Structure;

import java.util.List;
import java.util.Map;

public interface Analysis {
    <T> DecisionTreeItem createDecisionTree(Structure structure, List<T> dataList);

    <T> Map<Object, Object> testDecisionTree(Structure structure, List<T> dataList, DecisionTree tree);
}
