package com.ampaiva.metricsdatamanager.controller;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DataManager implements IDataManager {
    private EntityManagerFactory emFactory;
    EntityManager entityManager;
    private final String persistenceUnitName;

    public DataManager(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    @Override
    public void open() {
        emFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
        entityManager = emFactory.createEntityManager();
        begin();
    }

    public void begin() {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
    }

    public void commit() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().commit();
        }
    }

    public void rollback() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
    }

    public void flush() {
        commit();
        begin();
    }

    @Override
    public void close() {
        commit();
        entityManager.close();
        entityManager = null;
    }

    @Override
    public <T> void persist(T entity) {
        entityManager.persist(entity);
    }

    public <T> void remove(T entity) {
        entityManager.remove(entity);
    }

    public <T> T find(T project, int id) {
        @SuppressWarnings("unchecked")
        T obj = (T) entityManager.find(project.getClass(), id);
        return obj;
    }
}
