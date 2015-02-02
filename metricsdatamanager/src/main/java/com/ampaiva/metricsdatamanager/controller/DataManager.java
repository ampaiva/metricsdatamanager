package com.ampaiva.metricsdatamanager.controller;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.ampaiva.metricsdatamanager.model.Resource;

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

    public <T> T find(T clazz, int id) {
        @SuppressWarnings("unchecked")
        T obj = (T) entityManager.find(clazz.getClass(), id);
        return obj;
    }

    @SuppressWarnings("unchecked")
    public <U> Collection<U> findAll(Class<U> clazz) {
        Query query = entityManager.createQuery("SELECT e FROM " + clazz.getSimpleName() + " e");
        return query.getResultList();
    }

    @Override
    public <H> void removeAll(Class<H> _class) {
        Collection<H> entities = findAll(_class);
        for (H entity : entities) {
            remove(entity);
        }
    }

    @Override
    public Resource getResourceByName(String projectName, String resourceName) {
        TypedQuery<Resource> query = entityManager.createQuery(
                "SELECT r FROM Resource r WHERE r.projectBean.name = ?1 and r.name = ?2", Resource.class);
        query.setParameter(1, projectName);
        query.setParameter(2, resourceName);

        return query.getSingleResult();
    }

}
