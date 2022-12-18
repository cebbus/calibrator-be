package com.cebbus.calibrator.service;

import com.cebbus.calibrator.controller.request.DecisionTreeReq;
import com.cebbus.calibrator.domain.MethodCompare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

public interface MethodCompareService extends Service<MethodCompare> {
    Page<MethodCompare> getPage(Specification<MethodCompare> specification, PageRequest pageRequest);

    <T> void start(DecisionTreeReq request);
}
