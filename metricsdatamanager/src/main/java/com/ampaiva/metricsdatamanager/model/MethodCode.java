package com.ampaiva.metricsdatamanager.model;

import java.util.List;

public class MethodCode {
    public final String methodName;
    public final String methodSource;
    public final List<String> methodSequences;

    public MethodCode(String methodName, String methodSource, List<String> methodSequences) {
        this.methodName = methodName;
        this.methodSource = methodSource;
        this.methodSequences = methodSequences;
    }
}
