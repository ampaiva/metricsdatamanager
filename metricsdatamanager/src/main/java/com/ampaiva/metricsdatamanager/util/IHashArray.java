package com.ampaiva.metricsdatamanager.util;

public interface IHashArray {

    public abstract int put(String key);

    public abstract String getByIndex(int index);

    public abstract int getByKey(String key);

}