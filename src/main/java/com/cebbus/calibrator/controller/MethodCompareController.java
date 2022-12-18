package com.cebbus.calibrator.controller;

import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.MethodCompare;
import com.cebbus.calibrator.filter.SortWrapper;
import com.cebbus.calibrator.filter.SpecificationBuilder;
import com.cebbus.calibrator.service.MethodCompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/compare")
public class MethodCompareController {

    private final MethodCompareService service;

    @GetMapping
    public Page<MethodCompare> list(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String filter) {
        PageRequest pageRequest = PageRequest.of(--page, limit, SortWrapper.valueOf(sort));

        SpecificationBuilder<MethodCompare> builder = new SpecificationBuilder<>(MethodCompare.class);
        builder.with(filter);

        return service.getPage(builder.build(), pageRequest);
    }

    @PostMapping("/start")
    public ResponseEntity<?> loadDecisionTree(@RequestBody DecisionTreeReq request) {
        service.start(request);
        return ResponseEntity.ok(null);
    }

}
