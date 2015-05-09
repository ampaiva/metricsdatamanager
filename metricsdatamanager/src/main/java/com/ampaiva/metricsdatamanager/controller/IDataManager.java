package com.ampaiva.metricsdatamanager.controller;

import java.util.Collection;

import com.ampaiva.metricsdatamanager.model.Resource;

public interface IDataManager {

    void open();

    <G> void persist(G entity);

    void close();

    <H> void removeAll(Class<H> _class);

    <U> Collection<U> findAll(Class<U> clazz);

    Resource getResourceByName(String projectName, String resourceName);
}