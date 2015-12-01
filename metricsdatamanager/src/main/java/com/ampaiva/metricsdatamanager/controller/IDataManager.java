package com.ampaiva.metricsdatamanager.controller;

import java.util.Collection;
import java.util.List;

public interface IDataManager {

    void open();

    <G> void persist(G entity);

    <T> void refresh(T entity);

    <H> void removeAll(Class<H> _class);

    void close();

    <U> Collection<U> findAll(Class<U> clazz);

    <U> List<U> getResultList(Class<U> clazz, String namedQuery, Object... params);

    <U> U getSingleResult(Class<U> clazz, String namedQuery, Object... params);
}