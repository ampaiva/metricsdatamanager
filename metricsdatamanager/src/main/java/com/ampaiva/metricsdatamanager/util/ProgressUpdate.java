package com.ampaiva.metricsdatamanager.util;

public class ProgressUpdate implements IProgressUpdate {
    private int progress;
    private int limit;

    @Override
    public void start(int limit) {
        this.limit = limit;
    }

    @Override
    public void step(int value) {
        progress += value;
    }

    @Override
    public void step() {
        step(1);
    }

    @Override
    public int progress() {
        if (limit == 0) {
            return 0;
        }
        return progress * 100 / limit;
    }

    @Override
    public int limit() {
        return limit;
    }

}
