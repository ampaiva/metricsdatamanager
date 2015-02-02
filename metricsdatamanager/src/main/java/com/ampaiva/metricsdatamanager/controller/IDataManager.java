package com.ampaiva.metricsdatamanager.controller;

import com.ampaiva.metricsdatamanager.model.Resource;

public interface IDataManager {

    void open();

    <G> void persist(G entity);

    void close();

    <H> void removeAll(Class<H> _class);

    Resource getResourceByName(String projectName, String resourceName);
}