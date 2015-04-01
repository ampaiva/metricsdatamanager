package com.ampaiva.metricsdatamanager.controller;

import java.io.Serializable;
import java.util.List;

public class ConcernClone implements Serializable {
    private static final long serialVersionUID = -7952526730265463604L;

    String methodA;
    String methodB;
    String sourceA;
    String sourceB;
    List<String> sequencesA;
    List<String> sequencesB;

    @Override
    public String toString() {
        return "ConcernClone [methodA=" + methodA + ", methodB=" + methodB + ", sourceA=" + sourceA + ", sourceB="
                + sourceB + ", sequencesA=" + sequencesA + ", sequencesB=" + sequencesB + "]";
    }

}
