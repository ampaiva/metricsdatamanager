package com.ampaiva.metricsdatamanager.controller;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.util.Helper;
import com.ampaiva.hlo.util.view.IProgressReport;
import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressReport;
import com.ampaiva.hlo.util.view.ProgressUpdate;
import com.ampaiva.metricsdatamanager.config.IConcernCallsConfig;
import com.ampaiva.metricsdatamanager.model.MethodCode;
import com.ampaiva.metricsdatamanager.util.HashArray;
import com.ampaiva.metricsdatamanager.util.IHashArray;
import com.ampaiva.metricsdatamanager.util.ZipStreamUtil;

public class ConcernCallsManagerTest extends EasyMockSupport {
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
    public void getConcernClones() throws Exception {
        expect(config.getMinSeq()).andReturn(5).anyTimes();
        expect(config.getMaxDistance()).andReturn(5).anyTimes();

        replayAll();

        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(new File(
                "src/test/resources/com/ampaiva/metricsdatamanager/util/ZipTest2.zip")));
        List<ICodeSource> codeSources = Arrays.asList((ICodeSource) zipStreamUtil);
        concernCallsManager = new ConcernCallsManager(config, new HashArray());
        List<MethodCode> methodCodes = concernCallsManager.getMethodCodes(codeSources);
        assertNotNull(methodCodes);
        List<ConcernClone> duplications = concernCallsManager.getConcernClones(methodCodes);
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
        List<MethodCode> methodCodes = concernCallsManager.getMethodCodes(codeSources);
        assertNotNull(methodCodes);
        List<ConcernClone> duplications = concernCallsManager.getConcernClones(methodCodes);
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
        List<MethodCode> methodCodes = concernCallsManager.getMethodCodes(codeSources);
        assertNotNull(methodCodes);
        List<ConcernClone> duplications = concernCallsManager.getConcernClones(methodCodes);
        assertNotNull(duplications);
        assertTrue(duplications.size() >= 32);
    }

    @Ignore
    public void getConcernClonesAll() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        final int MIN_SEQ = 4;
        final int MAX_SEQ = 5;
        final int MAX_DISTANCE = 1;
        List<File> files = Helper.getFilesRecursevely("src/test/resources/com/ampaiva/metricsdatamanager/util", ".zip");
        IProgressReport report = new ProgressReport(3);
        IProgressUpdate update3 = ProgressUpdate.start(report, "Processing file", files.size());
        for (File file : files) {
            update3.beginIndex(file);
            ZipStreamUtil zipStreamUtil = new ZipStreamUtil(file.toString(), Helper.convertFile2InputStream(new File(
                    file.getAbsolutePath())));
            List<ICodeSource> codeSources = Arrays.asList((ICodeSource) zipStreamUtil);
            List<MethodCode> methodCodes = concernCallsManager.getMethodCodes(codeSources);
            assertNotNull(methodCodes);

            IProgressUpdate update = ProgressUpdate.start("Processing sequence", MAX_SEQ - MIN_SEQ + 1);
            for (int minSeq = MIN_SEQ; minSeq <= MAX_SEQ; minSeq++) {
                update.beginIndex();
                IProgressUpdate update2 = ProgressUpdate.start("Processing distance", MAX_DISTANCE);
                for (int maxDistance = 0; maxDistance < MAX_DISTANCE; maxDistance++) {
                    update2.beginIndex();

                    resetAll();

                    expect(config.getMinSeq()).andReturn(minSeq).anyTimes();
                    expect(config.getMaxDistance()).andReturn(maxDistance).anyTimes();

                    replayAll();

                    concernCallsManager = new ConcernCallsManager(config, new HashArray());
                    List<ConcernClone> duplications = concernCallsManager.getConcernClones(methodCodes);
                    assertNotNull(duplications);

                    verifyAll();
                }
            }
            update3.endIndex();
        }
        resetAll();
        replayAll();
        BasicConfigurator.resetConfiguration();
    }
}
