package com.ampaiva.metricsdatamanager.util.view;

public interface IProgressReport {
    enum Phase {
        STARTED, BEGIN_ITEM, END_ITEM, FINISHED
    }

    void onChanged(Phase phase, String id, int index, int size, int level);
}
