package com.ampaiva.metricsdatamanager.util;

public interface IProgressUpdate {

    public abstract void start(int limit);

    public abstract void step(int value);

    public abstract void step();

    public abstract int progress();

    public abstract int limit();

}