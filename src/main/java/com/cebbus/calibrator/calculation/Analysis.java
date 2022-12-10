package com.cebbus.calibrator.calculation;

import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.DecisionTreeItem;

public interface Analysis {
    DecisionTreeItem createDecisionTree(DecisionTreeReq request);
}
