package com.cebbus.calibrator.service;

import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.DecisionTree;

import java.util.Optional;

public interface DecisionTreeService extends Service<DecisionTree> {
    DecisionTree createDecisionTree(DecisionTreeReq request);

    <T> void testDecisionTree(DecisionTreeReq request, DecisionTree tree);

    Optional<DecisionTree> loadDecisionTree(DecisionTreeReq request);

}
