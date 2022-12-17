package com.cebbus.calibrator.controller;

import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.DecisionTree;
import com.cebbus.calibrator.domain.DecisionTreeItem;
import com.cebbus.calibrator.service.DecisionTreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/forest")
public class DecisionTreeController {

    private final DecisionTreeService service;

    @PostMapping("/load")
    public ResponseEntity<DecisionTreeItem> loadDecisionTree(@RequestBody DecisionTreeReq request) {
        Optional<DecisionTree> decisionTree = service.loadDecisionTree(request);
        return decisionTree.map(tree -> ResponseEntity.ok(tree.getRoot())).orElseGet(() -> ResponseEntity.ok(null));
    }

    @PostMapping("/training")
    public DecisionTreeItem createDecisionTree(@RequestBody DecisionTreeReq request) {
        return service.createDecisionTree(request).getRoot();
    }

    @PostMapping("/test")
    public ResponseEntity<Object> testDecisionTree(@RequestBody DecisionTreeReq request) {
        Optional<DecisionTree> decisionTree = service.loadDecisionTree(request);
        if (decisionTree.isEmpty()) {
            return ResponseEntity.internalServerError().build();
        } else {
            service.testDecisionTree(request, decisionTree.get());
            return ResponseEntity.ok(null);
        }
    }

}
