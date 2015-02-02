package com.ampaiva.metricsdatamanager.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;

import org.junit.Test;

import com.ampaiva.metricsdatamanager.model.Duplication;
import com.ampaiva.metricsdatamanager.model.Ocurrency;
import com.ampaiva.metricsdatamanager.model.Project;
import com.ampaiva.metricsdatamanager.model.Resource;

public class DataManagerTest {

    private static final String RESOURCE_NAME = "com.ampaiva.AnyClass";
    private static final String PU_NAME = "metricsdatamanagerTEST";
    private static final String PROJECT_LOCATION = "/somewhere/TestProject.zip";
    private static final String PROJECT_NAME = "TestProject";

    static class ProjectBuilder {

        List<Project> projects = new ArrayList<Project>();

        static ProjectBuilder New() {
            ProjectBuilder projectBuilder = new ProjectBuilder();
            return projectBuilder;
        }

        ProjectBuilder add() {
            Project project = new Project();
            project.setName(PROJECT_NAME + projects.size());
            project.setLocation(PROJECT_LOCATION + projects.size());
            project.setResources(new ArrayList<Resource>());
            projects.add(project);
            return this;
        }

        ProjectBuilder addResource() {
            Project project = get();
            Resource resource = new Resource();
            resource.setName(RESOURCE_NAME + project.getResources().size());
            resource.setProjectBean(project);
            resource.setOcurrencies(new ArrayList<Ocurrency>());
            project.addResource(resource);
            return this;
        }

        ProjectBuilder addOcurrency(EOcurrencyType eOcurrencyType, int beginline, int begincolumn, int endline,
                int endcolumn) {
            Project project = get();
            Resource resource = get(project);
            Ocurrency ocurrency = new Ocurrency();
            ocurrency.setType(eOcurrencyType.ordinal());
            ocurrency.setBeginline(beginline);
            ocurrency.setBegincolumn(begincolumn);
            ocurrency.setEndline(endline);
            ocurrency.setEndcolumn(endcolumn);
            ocurrency.setResourceBean(resource);
            resource.addOcurrency(ocurrency);
            return this;
        }

        Project get() {
            return get(projects.size() - 1);
        }

        Resource get(Project project) {
            return project.getResources().get(project.getResources().size() - 1);
        }

        Resource get(Project project, int resourceIndex) {
            return project.getResources().get(resourceIndex);
        }

        Ocurrency get(Resource resource) {
            return resource.getOcurrencies().get(resource.getOcurrencies().size() - 1);
        }

        Ocurrency get(Resource resource, int index) {
            return resource.getOcurrencies().get(index);
        }

        Project get(int projectIndex) {
            return projects.get(projectIndex);
        }

        Resource getResource(int projectIndex, int resourceIndex) {
            return get(get(projectIndex), resourceIndex);
        }

        Ocurrency getOcurrency(int projectIndex, int resourceIndex, int ocurrencyIndex) {
            return get(getResource(projectIndex, resourceIndex), ocurrencyIndex);
        }

        public void commit(DataManager dataManager) {
            for (Project project : projects) {
                dataManager.persist(project);
            }
            dataManager.commit();
        }
    }

    @Test
    public void testEmpty() throws Exception {
        DataManager dataManager = new DataManager(PU_NAME);
        dataManager.open();
        Project project = new Project();
        project.setName(PROJECT_NAME);
        project.setLocation(PROJECT_LOCATION);
        Resource resource = new Resource();
        resource.setProjectBean(project);
        resource.setName(RESOURCE_NAME);
        Resource resourceNoOcurrencies = new Resource();
        resourceNoOcurrencies.setName("No Ocurrencies");
        resourceNoOcurrencies.setProjectBean(project);
        Ocurrency ocurrency = new Ocurrency();
        ocurrency.setBeginline(1);
        ocurrency.setBegincolumn(2);
        ocurrency.setEndline(3);
        ocurrency.setEndcolumn(4);
        ocurrency.setResourceBean(resource);
        resource.setOcurrencies(Arrays.asList(ocurrency));
        project.setResources(Arrays.asList(resource, resourceNoOcurrencies));
        dataManager.persist(project);
        dataManager.commit();

        int id1 = project.getId();
        assertTrue(id1 > 0);
        Project p1 = dataManager.find(project, id1);
        assertNotNull(p1);
        assertEquals(id1, project.getId());
        assertEquals(PROJECT_NAME, project.getName());
        assertEquals(PROJECT_LOCATION, project.getLocation());
        List<Resource> resources = p1.getResources();
        assertNotNull(resources);
        assertEquals(2, resources.size());
        Resource resource2 = resources.get(0);
        assertEquals(p1, resource2.getProjectBean());
        assertTrue(resource2.getId() > 0);
        assertEquals(RESOURCE_NAME, resource2.getName());
        List<Ocurrency> ocurrencies = resource2.getOcurrencies();
        assertNotNull(ocurrencies);
        assertEquals(1, ocurrencies.size());
        Ocurrency ocurrency2 = ocurrencies.get(0);
        assertTrue(ocurrency2.getId() > 0);
        assertEquals(resource2, ocurrency2.getResourceBean());
        assertEquals(1, ocurrency2.getBeginline());
        assertEquals(2, ocurrency2.getBegincolumn());
        assertEquals(3, ocurrency2.getEndline());
        assertEquals(4, ocurrency2.getEndcolumn());
        try {
            dataManager.begin();
            Project project2 = new Project();
            project2.setName(PROJECT_NAME);
            project2.setLocation(PROJECT_LOCATION);
            dataManager.persist(project2);
            dataManager.commit();
            fail("Name must be unique");
        } catch (RollbackException ex) {
            // Passed
            dataManager.rollback();
        }
        Project p1_2 = dataManager.find(project, id1);
        assertNotNull(p1_2);
        dataManager.begin();
        dataManager.remove(p1_2);
        dataManager.commit();
        p1_2 = dataManager.find(project, project.getId());
        assertNull(p1_2);
        dataManager.close();
    }

    @Test
    public void testFindAll() throws Exception {
        DataManager dataManager = new DataManager(PU_NAME);
        dataManager.open();
        Project project1 = new Project();
        project1.setName(PROJECT_NAME);
        project1.setLocation(PROJECT_LOCATION);
        Project project2 = new Project();
        project2.setName(PROJECT_NAME + "2");
        project2.setLocation(PROJECT_LOCATION);
        dataManager.persist(project1);
        dataManager.persist(project2);
        dataManager.commit();

        int id1 = project1.getId();
        assertTrue(id1 > 0);
        int id2 = project2.getId();
        assertTrue(id2 > 0);
        Collection<Project> projects = dataManager.findAll(Project.class);
        assertNotNull(projects);
        assertEquals(2, projects.size());
        assertTrue(projects.contains(project1));

        dataManager.begin();
        dataManager.removeAll(Project.class);
        dataManager.commit();
        Collection<Project> projects2 = dataManager.findAll(Project.class);
        assertNotNull(projects2);
        assertEquals(0, projects2.size());
        dataManager.close();
    }

    @Test
    public void testDuplicationBlock() throws Exception {
        DataManager dataManager = new DataManager(PU_NAME);
        dataManager.open();
        ProjectBuilder projectBuilder = ProjectBuilder.New().add().addResource()
                .addOcurrency(EOcurrencyType.DUPLICATION, 1, 0, 3, 0).addResource()
                .addOcurrency(EOcurrencyType.DUPLICATION, 1, 0, 3, 0);
        dataManager.persist(projectBuilder.get());
        Duplication duplication = new Duplication();
        int copy = projectBuilder.getOcurrency(0, 0, 0).getId();
        duplication.setCopy(copy);
        int paste = projectBuilder.getOcurrency(0, 1, 0).getId();
        duplication.setPaste(paste);
        dataManager.persist(duplication);
        dataManager.commit();

        int id1 = duplication.getId();
        assertTrue(id1 > 0);
        Duplication duplication1 = dataManager.find(duplication, id1);
        assertNotNull(duplication1);
        assertEquals(id1, duplication.getId());
        assertEquals(copy, duplication.getCopy());
        assertEquals(paste, duplication.getPaste());
        dataManager.begin();
        dataManager.remove(projectBuilder.get());
        dataManager.remove(duplication);
        dataManager.commit();
        duplication = dataManager.find(duplication, duplication.getId());
        assertNull(duplication);
        dataManager.close();
    }

    @Test
    public void testFindProject() throws Exception {
        DataManager dataManager = new DataManager(PU_NAME);
        dataManager.open();
        ProjectBuilder projectBuilder = ProjectBuilder.New().add().addResource().add().addResource();
        projectBuilder.commit(dataManager);

        TypedQuery<Project> query1 = dataManager.entityManager.createQuery("SELECT p FROM Project p WHERE p.name = ?1",
                Project.class);
        query1.setParameter(1, PROJECT_NAME + 1);
        Project project1 = query1.getSingleResult();
        assertNotNull(project1);
        Resource resource0 = dataManager.getResourceByName(PROJECT_NAME + 0, RESOURCE_NAME + 0);
        assertNotNull(resource0);

        dataManager.begin();
        dataManager.removeAll(Project.class);
        dataManager.commit();
        try {
            project1 = query1.getSingleResult();
            fail("Should throw NoResultException");
        } catch (NoResultException ex) {

        }
        dataManager.close();
    }

}
