package com.ampaiva.metricsdatamanager.util.view;

public interface IProgressUpdate {
    void beginIndex(Object... info);

    void endIndex(Object... info);
}
