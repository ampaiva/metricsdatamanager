package com.ampaiva.metricsdatamanager.util;

public interface IHashArray {

    public int put(String key);

    public String getByIndex(int index);

    public int getByKey(String key);

    public int size();
}