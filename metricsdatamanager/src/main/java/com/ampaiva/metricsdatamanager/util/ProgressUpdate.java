package com.ampaiva.metricsdatamanager.util;

public class ProgressUpdate implements IProgressUpdate {
    private int current;
    private int limit;

    @Override
    public void start(int limit) {
        this.limit = limit;
    }

    @Override
    public void step(int value) {
        current += value;
    }

    @Override
    public void step() {
        step(1);
    }

    @Override
    public int percent() {
        if (limit == 0) {
            return 0;
        }
        return current * 100 / limit;
    }

    @Override
    public int limit() {
        return limit;
    }

    @Override
    public String toString() {
        return "ProgressUpdate [current=" + current + ", limit=" + limit + "]";
    }

    @Override
    public int current() {
        return current;
    }

}
