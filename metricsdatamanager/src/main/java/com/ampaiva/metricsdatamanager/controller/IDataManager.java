package com.ampaiva.metricsdatamanager.controller;

public interface IDataManager {

    void open();

    <G> void persist(G entity);

    void close();

    <H> void removeAll(Class<H> _class);
}