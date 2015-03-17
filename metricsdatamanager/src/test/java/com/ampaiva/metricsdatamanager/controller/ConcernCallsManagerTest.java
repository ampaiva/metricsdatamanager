package com.ampaiva.metricsdatamanager.controller;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ampaiva.hlo.cm.IMethodCalls;
import com.ampaiva.metricsdatamanager.util.IHashArray;

public class ConcernCallsManagerTest extends EasyMockSupport {

    private static final String METHOD_CALL_0 = "FooA.foo";
    private static final String METHOD_CALL_1 = "FooB.foo";
    private static final String METHOD_CALL_2 = "FooC.foo";

    // Class under test
    private ConcernCallsManager concernCallsManager;
    private IHashArray hashArray;

    /**
     * Setup mocks before each test.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        hashArray = createMock(IHashArray.class);
        concernCallsManager = new ConcernCallsManager(hashArray);
    }

    /**
     * Verifies all mocks after each test.
     */
    @After()
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testGetDuplications() {
        IMethodCalls methodCallsA = createMock(IMethodCalls.class);
        List<List<String>> methodsA = new ArrayList<List<String>>();
        List<String> callsA = Arrays.asList(METHOD_CALL_0, METHOD_CALL_1, METHOD_CALL_2);
        methodsA.add(callsA);
        expect(methodCallsA.getSequences()).andReturn(methodsA);
        expect(hashArray.getByKey(METHOD_CALL_0)).andReturn(0).anyTimes();
        expect(hashArray.getByKey(METHOD_CALL_1)).andReturn(1).anyTimes();
        expect(hashArray.getByKey(METHOD_CALL_2)).andReturn(2).anyTimes();
        expect(hashArray.getByIndex(0)).andReturn(METHOD_CALL_0).anyTimes();
        expect(hashArray.getByIndex(1)).andReturn(METHOD_CALL_1).anyTimes();
        expect(hashArray.getByIndex(2)).andReturn(METHOD_CALL_2).anyTimes();

        IMethodCalls methodCallsB = createMock(IMethodCalls.class);
        List<List<String>> methodsB = new ArrayList<List<String>>();
        List<String> callsB = Arrays.asList(METHOD_CALL_0, METHOD_CALL_2);
        methodsB.add(callsB);
        expect(methodCallsB.getSequences()).andReturn(methodsB);

        replayAll();
        List<IMethodCalls> methodCallsList = Arrays.asList(methodCallsA, methodCallsB);
        List<String> duplications = concernCallsManager.getDuplications(methodCallsList);
        assertNotNull(duplications);
        assertEquals(2, duplications.size());
        Assert.assertArrayEquals(new String[] { METHOD_CALL_0, METHOD_CALL_2 }, duplications.toArray(new String[0]));
    }
}
