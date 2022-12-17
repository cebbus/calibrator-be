package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.DecisionTree;
import com.cebbus.calibrator.domain.DecisionTreeItem;

import java.util.Map;

public interface Analysis {
    DecisionTreeItem createDecisionTree(DecisionTreeReq request);

    Map<Object, Object> testDecisionTree(DecisionTreeReq request, DecisionTree tree);
}
