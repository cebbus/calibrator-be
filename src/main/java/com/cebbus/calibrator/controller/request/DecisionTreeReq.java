package com.cebbus.calibrator.controller.request;

import com.cebbus.calibrator.domain.enums.MethodType;
import lombok.Data;

@Data
public class DecisionTreeReq {
    private final Integer structureId;
    private final MethodType methodType;
}
