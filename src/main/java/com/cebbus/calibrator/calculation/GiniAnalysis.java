package com.cebbus.calibrator.calculation;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GiniAnalysis extends CartAnalysis {

    @Override
    double calculateGoodness(double[] pArr, List<double[]> pArrList) {
        double giniLeft = 1 - pArrList.stream().mapToDouble(p -> Math.pow(p[0], 2)).sum();
        double giniRight = 1 - pArrList.stream().mapToDouble(p -> Math.pow(p[1], 2)).sum();
        double goodness = 1 - ((pArr[0] * giniLeft) + (pArr[1] * giniRight));

        return Double.isNaN(goodness) || Double.isInfinite(goodness) ? 0d : goodness;
    }
}
