package com.cebbus.calibrator.calculation;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TwoingAnalysis extends CartAnalysis {

    @Override
    double calculateGoodness(double[] pArr, List<double[]> pArrList) {
        double pc = pArrList.stream().mapToDouble(p -> Math.abs(p[0] - p[1])).sum();
        double goodness = 2 * pArr[0] * pArr[1] * pc;

        return Double.isNaN(goodness) || Double.isInfinite(goodness) ? 0d : goodness;
    }
}
