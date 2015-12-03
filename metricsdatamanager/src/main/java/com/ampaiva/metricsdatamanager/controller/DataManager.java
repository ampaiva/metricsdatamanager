package com.ampaiva.metricsdatamanager.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class DataManager implements IDataManager {
    private EntityManagerFactory emFactory;
    EntityManager entityManager;
    private final String persistenceUnitName;
    private final Map<String, String> properties = new HashMap<String, String>();

    public DataManager(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public void open() {
        emFactory = Persistence.createEntityManagerFactory(persistenceUnitName, properties);
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

    @Override
    public <T> void refresh(T entity) {
        entityManager.refresh(entity);
    }

    public <T> void remove(T entity) {
        entityManager.remove(entity);
    }

    public <T> T find(T clazz, int id) {
        @SuppressWarnings("unchecked")
        T obj = (T) entityManager.find(clazz.getClass(), id);
        return obj;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Collection<U> findAll(Class<U> clazz) {
        Query query = entityManager.createQuery("SELECT e FROM " + clazz.getSimpleName() + " e order by e.id");
        return query.getResultList();
    }

    @Override
    public <U> List<U> getResultList(Class<U> clazz, String namedQuery, Object... params) {
        TypedQuery<U> query = entityManager.createNamedQuery(namedQuery, clazz);
        if (params != null) {
            for (int i = 1; i <= params.length; i++) {
                query.setParameter(String.valueOf(i), params[i]);
            }
        }
        List<U> results = query.getResultList();
        return results;
    }

    @Override
    public <U> U getSingleResult(Class<U> clazz, String namedQuery, Object... params) {
        TypedQuery<U> query = entityManager.createNamedQuery(namedQuery, clazz);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                query.setParameter(String.valueOf(i + 1), params[i]);
            }
        }
        try {
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public <H> void removeAll(Class<H> _class) {
        Collection<H> entities = findAll(_class);
        for (H entity : entities) {
            remove(entity);
        }
    }
}
