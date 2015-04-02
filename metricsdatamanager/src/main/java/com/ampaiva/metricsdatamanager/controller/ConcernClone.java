package com.ampaiva.metricsdatamanager.controller;

import java.io.Serializable;
import java.util.List;

public class ConcernClone implements Serializable {
    private static final long serialVersionUID = -7952526730265463604L;

    public String methodA;
    public String methodB;
    public List<String> sources;
    public List<List<String>> sequences;
    public int[] duplications;

    @Override
    public String toString() {
        return "ConcernClone [methodA=" + methodA + ", methodB=" + methodB + ", sources=" + sources + ", sequences="
                + sequences + "]";
    }

}
