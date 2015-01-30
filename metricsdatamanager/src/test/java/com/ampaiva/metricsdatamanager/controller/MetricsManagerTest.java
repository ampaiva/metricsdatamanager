package com.ampaiva.metricsdatamanager.controller;

import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ampaiva.hlo.cm.ConcernMetricNode;
import com.ampaiva.metricsdatamanager.model.Ocurrency;
import com.ampaiva.metricsdatamanager.model.Project;
import com.ampaiva.metricsdatamanager.model.Resource;

public class MetricsManagerTest extends EasyMockSupport {

    private static final String CLASS_NAME = "com.ampaiva.metricsdatamanager.test.TestClass";
    private static final String PROJECT_LOCATION = "projectLocation";
    private static final String PROJECT_NAME = "projectName";
    // Class under test
    private MetricsManager metricsManager;
    // Mocks
    private IDataManager dataManager;

    /**
     * Setup mocks before each test.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        dataManager = createMock(IDataManager.class);
        metricsManager = new MetricsManager(dataManager);
    }

    /**
     * Verifies all mocks after each test.
     */
    @After()
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testDeleteAllData() {

        dataManager.open();
        dataManager.removeAll(Project.class);
        dataManager.close();

        replayAll();

        metricsManager.deleteAllData();
    }

    @Test
    public void testPersist() {

        dataManager.open();
        dataManager.persist(isA(Project.class));
        dataManager.close();

        replayAll();
        HashMap<String, List<ConcernMetricNode>> metrics = new HashMap<String, List<ConcernMetricNode>>();
        List<ConcernMetricNode> nodes = new LinkedList<ConcernMetricNode>();
        ConcernMetricNode concernMetricNode = new ConcernMetricNode("11\n2\n3333", 1, 2, 3, 4);
        nodes.add(concernMetricNode);
        metrics.put(CLASS_NAME, nodes);

        Project project = metricsManager.persist(PROJECT_NAME, PROJECT_LOCATION, EOcurrencyType.EXCEPTION_HANDLING,
                metrics);
        assertNotNull(project);
        assertEquals(PROJECT_NAME, project.getName());
        assertEquals(PROJECT_LOCATION, project.getLocation());
        List<Resource> resources = project.getResources();
        assertNotNull(resources);
        assertEquals(1, resources.size());
        Resource resource = resources.get(0);
        assertNotNull(resource);
        assertEquals(CLASS_NAME, resource.getName());
        List<Ocurrency> ocurrencies = resource.getOcurrencies();
        assertNotNull(ocurrencies);
        assertEquals(1, ocurrencies.size());
        Ocurrency ocurrency = ocurrencies.get(0);
        assertNotNull(ocurrency);
        assertEquals(EOcurrencyType.EXCEPTION_HANDLING, ocurrency.getType());
        assertEquals(1, ocurrency.getBeginline());
        assertEquals(2, ocurrency.getBegincolumn());
        assertEquals(3, ocurrency.getEndline());
        assertEquals(4, ocurrency.getEndcolumn());
    }
}
