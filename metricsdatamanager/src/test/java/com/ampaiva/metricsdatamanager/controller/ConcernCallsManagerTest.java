package com.ampaiva.metricsdatamanager.controller;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.cm.IMethodCalls;
import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.config.IConcernCallsConfig;
import com.ampaiva.metricsdatamanager.util.HashArray;
import com.ampaiva.metricsdatamanager.util.IHashArray;
import com.ampaiva.metricsdatamanager.util.ZipStreamUtil;
import com.ampaiva.metricsdatamanager.util.view.IProgressReport;
import com.ampaiva.metricsdatamanager.util.view.IProgressUpdate;
import com.ampaiva.metricsdatamanager.util.view.ProgressReport;
import com.ampaiva.metricsdatamanager.util.view.ProgressUpdate;

public class ConcernCallsManagerTest extends EasyMockSupport {

    private static final String METHOD_CALL_0 = "FooA.foo";
    private static final String METHOD_CALL_1 = "FooB.foo";
    private static final String METHOD_CALL_2 = "FooC.foo";
    private static final String METHOD_CALL_3 = "FooD.foo";

    // Class under test
    private ConcernCallsManager concernCallsManager;
    private IConcernCallsConfig config;
    private IHashArray hashArray;

    /**
     * Setup mocks before each test.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        config = createMock(IConcernCallsConfig.class);
        hashArray = createMock(IHashArray.class);
        concernCallsManager = new ConcernCallsManager(config, hashArray);
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
        expect(methodCallsA.getSequences()).andReturn(
                Arrays.asList(Arrays.asList(METHOD_CALL_0, METHOD_CALL_1, METHOD_CALL_2)));
        expect(hashArray.getByKey(METHOD_CALL_0)).andReturn(0).anyTimes();
        expect(hashArray.getByKey(METHOD_CALL_1)).andReturn(1).anyTimes();
        expect(hashArray.getByKey(METHOD_CALL_2)).andReturn(2).anyTimes();
        expect(hashArray.getByIndex(0)).andReturn(METHOD_CALL_0).anyTimes();
        expect(hashArray.getByIndex(1)).andReturn(METHOD_CALL_1).anyTimes();
        expect(hashArray.getByIndex(2)).andReturn(METHOD_CALL_2).anyTimes();
        expect(config.getMinSeq()).andReturn(2).anyTimes();
        expect(config.getMaxDistance()).andReturn(1).anyTimes();

        IMethodCalls methodCallsB = createMock(IMethodCalls.class);
        expect(methodCallsB.getSequences()).andReturn(Arrays.asList(Arrays.asList(METHOD_CALL_0, METHOD_CALL_2)));

        replayAll();
        List<List<List<int[]>>> duplications = concernCallsManager.getAllDuplications(methodCallsA,
                Arrays.asList(methodCallsB));
        assertNotNull(duplications);
        assertEquals(1, duplications.size());
        assertEquals(1, duplications.get(0).size());
        assertEquals(1, duplications.get(0).get(0).size());
        assertNotNull(duplications.get(0).get(0).get(0));
        assertEquals(4, duplications.get(0).get(0).get(0).length);
        Assert.assertArrayEquals(new int[] { 0, 0, 2, 1 }, duplications.get(0).get(0).get(0));
    }

    @Test
    public void testGetDuplicationsAboveMaxDistance() {
        IMethodCalls methodCallsA = createMock(IMethodCalls.class);
        expect(methodCallsA.getSequences()).andReturn(
                Arrays.asList(Arrays.asList(METHOD_CALL_0, METHOD_CALL_1, METHOD_CALL_2)));
        expect(hashArray.getByKey(METHOD_CALL_0)).andReturn(0).anyTimes();
        expect(hashArray.getByKey(METHOD_CALL_1)).andReturn(1).anyTimes();
        expect(hashArray.getByKey(METHOD_CALL_2)).andReturn(2).anyTimes();
        expect(hashArray.getByKey(METHOD_CALL_3)).andReturn(3).anyTimes();
        expect(hashArray.getByIndex(0)).andReturn(METHOD_CALL_0).anyTimes();
        expect(hashArray.getByIndex(1)).andReturn(METHOD_CALL_1).anyTimes();
        expect(hashArray.getByIndex(2)).andReturn(METHOD_CALL_2).anyTimes();
        expect(hashArray.getByIndex(3)).andReturn(METHOD_CALL_3).anyTimes();
        expect(config.getMinSeq()).andReturn(2).anyTimes();
        expect(config.getMaxDistance()).andReturn(0).anyTimes();

        IMethodCalls methodCallsB = createMock(IMethodCalls.class);
        expect(methodCallsB.getSequences()).andReturn(
                Arrays.asList(Arrays.asList(METHOD_CALL_0, METHOD_CALL_3, METHOD_CALL_2)));

        replayAll();
        List<List<List<int[]>>> duplications = concernCallsManager.getAllDuplications(methodCallsA,
                Arrays.asList(methodCallsB));
        assertEquals(1, duplications.size());
        assertEquals(1, duplications.get(0).size());
        assertEquals(1, duplications.get(0).get(0).size());
        assertNull(duplications.get(0).get(0).get(0));
    }

    @Test
    public void getConcernClones() throws Exception {
        expect(config.getMinSeq()).andReturn(5).anyTimes();
        expect(config.getMaxDistance()).andReturn(5).anyTimes();

        replayAll();

        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(new File(
                "src/test/resources/com/ampaiva/metricsdatamanager/util/ZipTest2.zip")));
        List<ICodeSource> codeSources = Arrays.asList((ICodeSource) zipStreamUtil);
        concernCallsManager = new ConcernCallsManager(config, new HashArray());
        List<ConcernClone> duplications = concernCallsManager.getConcernClones(codeSources);
        assertNotNull(duplications);
        assertTrue(duplications.size() > 0);
    }

    @Test
    public void getConcernClonesZipTest3() throws Exception {
        expect(config.getMinSeq()).andReturn(5).anyTimes();
        expect(config.getMaxDistance()).andReturn(1).anyTimes();

        replayAll();

        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(new File(
                "src/test/resources/com/ampaiva/metricsdatamanager/util/ZipTest3.zip")));
        List<ICodeSource> codeSources = Arrays.asList((ICodeSource) zipStreamUtil);
        concernCallsManager = new ConcernCallsManager(config, new HashArray());
        List<ConcernClone> duplications = concernCallsManager.getConcernClones(codeSources);
        assertNotNull(duplications);
        assertEquals(32, duplications.size());
    }

    @Ignore
    public void getConcernClonesMaven() throws Exception {
        expect(config.getMinSeq()).andReturn(5).anyTimes();
        expect(config.getMaxDistance()).andReturn(1).anyTimes();

        replayAll();

        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(new File(
                "src/test/resources/com/ampaiva/metricsdatamanager/util/maven-master.zip")));
        List<ICodeSource> codeSources = Arrays.asList((ICodeSource) zipStreamUtil);
        concernCallsManager = new ConcernCallsManager(config, new HashArray());
        List<ConcernClone> duplications = concernCallsManager.getConcernClones(codeSources);
        assertNotNull(duplications);
        assertTrue(duplications.size() >= 32);
    }

    @Test
    public void getConcernClonesAll() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        final int MIN_SEQ = 4;
        final int MAX_SEQ = 5;
        final int MAX_DISTANCE = 1;
        List<File> files = Helper.getFilesRecursevely("src/test/resources/com/ampaiva/metricsdatamanager/util", ".zip");
        IProgressReport report = new ProgressReport(2);
        IProgressUpdate update = ProgressUpdate.start(report, "Processing sequence", MAX_SEQ - MIN_SEQ + 1);
        for (int minSeq = MIN_SEQ; minSeq <= MAX_SEQ; minSeq++) {
            update.beginIndex();
            IProgressUpdate update2 = ProgressUpdate.start("Processing distance", MAX_DISTANCE);
            for (int maxDistance = 0; maxDistance < MAX_DISTANCE; maxDistance++) {
                update2.beginIndex();
                IProgressUpdate update3 = ProgressUpdate.start("Processing file", files.size());
                for (File file : files) {
                    update3.beginIndex(file);

                    resetAll();

                    expect(config.getMinSeq()).andReturn(minSeq).anyTimes();
                    expect(config.getMaxDistance()).andReturn(maxDistance).anyTimes();

                    replayAll();

                    concernCallsManager = new ConcernCallsManager(config, new HashArray());
                    ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(new File(file
                            .getAbsolutePath())));
                    List<ICodeSource> codeSources = Arrays.asList((ICodeSource) zipStreamUtil);
                    List<ConcernClone> duplications = concernCallsManager.getConcernClones(codeSources);
                    assertNotNull(duplications);

                    verifyAll();
                }
            }
            update.endIndex();
        }
        resetAll();
        replayAll();
        BasicConfigurator.resetConfiguration();
    }
}
