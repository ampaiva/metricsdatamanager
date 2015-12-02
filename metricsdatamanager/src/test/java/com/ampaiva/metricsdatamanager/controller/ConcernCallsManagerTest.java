package com.ampaiva.metricsdatamanager.controller;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ampaiva.hlo.util.Helper;
import com.ampaiva.hlo.util.view.IProgressReport;
import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressReport;
import com.ampaiva.hlo.util.view.ProgressUpdate;
import com.ampaiva.metricsdatamanager.config.IConcernCallsConfig;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.model.Unit;
import com.ampaiva.metricsdatamanager.util.MatchesData;
import com.ampaiva.metricsdatamanager.util.SequencesInt;
import com.ampaiva.metricsdatamanager.util.ZipStreamUtil;

public class ConcernCallsManagerTest extends EasyMockSupport {
    // Class under test
    private ConcernCallsManager concernCallsManager;
    private IConcernCallsConfig config;

    /**
     * Setup mocks before each test.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        config = createMock(IConcernCallsConfig.class);
        concernCallsManager = new ConcernCallsManager();
    }

    /**
     * Verifies all mocks after each test.
     */
    @After()
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void getConcernClonesZipTest2() throws Exception {
        expect(config.getMinSeq()).andReturn(5).anyTimes();

        replayAll();

        List<Sequence> sequences = new ArrayList<Sequence>();
        File file = new File("src/test/resources/com/ampaiva/metricsdatamanager/util/ZipTest2.zip");
        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(file));
        Repository repository = concernCallsManager.createRepository(Arrays.asList(zipStreamUtil), file.getName(),
                sequences);
        assertNotNull(repository);
        assertEquals("ZipTest2.zip", repository.getLocation());
        List<Unit> units = repository.getUnits();
        assertNotNull(units);
        concernCallsManager = new ConcernCallsManager(new SequencesInt(sequences, units));
        List<MatchesData> sequenceMatches = concernCallsManager.getSequenceMatches(config);
        List<ConcernClone> duplications = concernCallsManager.getConcernClones(sequenceMatches, units);
        assertNotNull(duplications);
    }

    @Test
    public void getConcernClonesZipTest3() throws Exception {
        expect(config.getMinSeq()).andReturn(5).anyTimes();

        replayAll();

        List<Sequence> sequences = new ArrayList<Sequence>();
        File file = new File("src/test/resources/com/ampaiva/metricsdatamanager/util/ZipTest3.zip");
        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(file));
        Repository repository = concernCallsManager.createRepository(Arrays.asList(zipStreamUtil), file.getName(),
                sequences);
        List<Unit> units = repository.getUnits();
        assertNotNull(units);
        concernCallsManager = new ConcernCallsManager(new SequencesInt(sequences, units));
        List<MatchesData> sequenceMatches = concernCallsManager.getSequenceMatches(config);
        List<ConcernClone> duplications = concernCallsManager.getConcernClones(sequenceMatches, units);
        assertNotNull(duplications);
        assertTrue(duplications.size() > 0);
    }

    @Test
    public void getConcernClonesZipTest4() throws Exception {
        expect(config.getMinSeq()).andReturn(4).anyTimes();

        replayAll();

        List<Sequence> sequences = new ArrayList<Sequence>();
        File file = new File("src/test/resources/com/ampaiva/metricsdatamanager/util/ZipTest4.zip");
        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(file));
        Repository repository = concernCallsManager.createRepository(Arrays.asList(zipStreamUtil), file.getName(),
                sequences);
        List<Unit> units = repository.getUnits();
        assertNotNull(units);
        concernCallsManager = new ConcernCallsManager(new SequencesInt(sequences, units));
        List<MatchesData> sequenceMatches = concernCallsManager.getSequenceMatches(config);
        List<ConcernClone> duplications = concernCallsManager.getConcernClones(sequenceMatches, units);
        assertNotNull(duplications);
        assertEquals(1, duplications.size());
        ConcernClone concernClone = duplications.get(0);
        assertNotNull(concernClone);
        assertEquals(2, concernClone.methods.size());
        assertEquals(2, concernClone.sequences.size());
        assertEquals(5, concernClone.sequences.get(0).size());
        assertEquals(7, concernClone.sequences.get(1).size());
        assertEquals(1, concernClone.duplications.size());

    }

    @Test
    public void getConcernClonesAll() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        List<File> files = Helper.getFilesRecursevely("/temp", ".zip");
        IProgressReport report = new ProgressReport(3);
        IProgressUpdate update3 = ProgressUpdate.start(report, "Processing file", files.size());
        for (File file : files) {
            update3.beginIndex(file);
            ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(file));
            List<Sequence> sequences = new ArrayList<Sequence>();
            Repository repository = concernCallsManager.createRepository(Arrays.asList(zipStreamUtil), file.getName(),
                    sequences);
            List<Unit> units = repository.getUnits();
            assertNotNull(units);

            final int MIN_SEQ = 5;
            final int MAX_SEQ = 5;
            final int MAX_DISTANCE = 1;
            IProgressUpdate update = ProgressUpdate.start("Processing sequence", MAX_SEQ - MIN_SEQ + 1);
            for (int minSeq = MIN_SEQ; minSeq <= MAX_SEQ; minSeq++) {
                update.beginIndex();
                IProgressUpdate update2 = ProgressUpdate.start("Processing distance", MAX_DISTANCE);
                for (int maxDistance = 0; maxDistance < MAX_DISTANCE; maxDistance++) {
                    update2.beginIndex();

                    resetAll();

                    expect(config.getMinSeq()).andReturn(minSeq).anyTimes();

                    replayAll();

                    concernCallsManager = new ConcernCallsManager(new SequencesInt(sequences, units));
                    List<MatchesData> sequenceMatches = concernCallsManager.getSequenceMatches(config);
                    List<ConcernClone> duplications = concernCallsManager.getConcernClones(sequenceMatches, units);
                    assertNotNull(duplications);
                    if (duplications.size() > 0) {
                        System.out.println(duplications.get(0));
                    }

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
