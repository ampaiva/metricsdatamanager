package com.ampaiva.metricsdatamanager.controller;

import java.io.Serializable;
import java.util.List;

import com.ampaiva.hlo.util.SourceHandler;

public class ConcernClone implements Serializable {
    private static final long serialVersionUID = -7952526730265463604L;

    public List<String> methods;
    public List<SourceHandler> sources;
    public List<List<String>> sequences;
    public List<List<Integer>> duplications;

    @Override
    public String toString() {
        return "ConcernClone [methods=" + methods + ", sources=" + sources + ", sequences=" + sequences + "]";
    }

}
