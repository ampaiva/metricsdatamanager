package com.ampaiva.metricsdatamanager.util.view;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ampaiva.metricsdatamanager.util.view.IProgressReport.Phase;

public class ProgressUpdateTest extends EasyMockSupport {
    private IProgressReport progressReportMock;

    /*
     * Setup mocks before each test.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        progressReportMock = createMock(IProgressReport.class);
    }

    /**
     * Verifies all mocks after each test.
     */
    @After()
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testNoReport() {
        replayAll();

        IProgressUpdate update = ProgressUpdate.start("Root ", 1);
        update.beginIndex();
        doFor(0, 1);
        update.endIndex();
    }

    @Test
    public void testReport1Level() {
        progressReportMock.onChanged(Phase.STARTED, "Root", 0, 1, 0);
        progressReportMock.onChanged(Phase.BEGIN_ITEM, "Root", 0, 1, 0);
        progressReportMock.onChanged(Phase.END_ITEM, "Root", 0, 1, 0);
        progressReportMock.onChanged(Phase.FINISHED, "Root", 1, 1, 0);

        replayAll();

        IProgressUpdate update = ProgressUpdate.start(progressReportMock, "Root", 1);
        update.beginIndex();
        update.endIndex();
    }

    @Test
    public void testReport2Level() {
        progressReportMock.onChanged(Phase.STARTED, "Root", 0, 1, 0);
        progressReportMock.onChanged(Phase.BEGIN_ITEM, "Root", 0, 1, 0);

        int level = 1;
        int size = 1;
        progressReportMock.onChanged(Phase.STARTED, level + " Loop", 0, size, level);
        for (int i = 0; i < size; i++) {
            progressReportMock.onChanged(Phase.BEGIN_ITEM, level + " Loop", i, size, level);
            progressReportMock.onChanged(Phase.END_ITEM, level + " Loop", i, size, level);
        }
        progressReportMock.onChanged(Phase.FINISHED, level + " Loop", size, size, level);

        progressReportMock.onChanged(Phase.END_ITEM, "Root", 0, 1, 0);
        progressReportMock.onChanged(Phase.FINISHED, "Root", 1, 1, 0);

        replayAll();

        IProgressUpdate update = ProgressUpdate.start(progressReportMock, "Root", 1);
        update.beginIndex();
        IProgressUpdate progress2 = ProgressUpdate.start(level + " Loop", size);
        for (int i = 0; i < size; i++) {
            progress2.beginIndex();
        }
        update.endIndex();
    }

    @Test
    public void testReport2LevelSize2() {
        progressReportMock.onChanged(Phase.STARTED, "Root", 0, 1, 0);
        progressReportMock.onChanged(Phase.BEGIN_ITEM, "Root", 0, 1, 0);

        int level = 1;
        int size = 2;
        progressReportMock.onChanged(Phase.STARTED, level + " Loop", 0, size, level);
        for (int i = 0; i < size; i++) {
            progressReportMock.onChanged(Phase.BEGIN_ITEM, level + " Loop", i, size, level);
            progressReportMock.onChanged(Phase.END_ITEM, level + " Loop", i, size, level);
        }
        progressReportMock.onChanged(Phase.FINISHED, level + " Loop", size, size, level);

        progressReportMock.onChanged(Phase.END_ITEM, "Root", 0, 1, 0);
        progressReportMock.onChanged(Phase.FINISHED, "Root", 1, 1, 0);

        replayAll();

        IProgressUpdate update = ProgressUpdate.start(progressReportMock, "Root", 1);
        update.beginIndex();
        IProgressUpdate progress2 = ProgressUpdate.start(level + " Loop", size);
        for (int i = 0; i < size; i++) {
            progress2.beginIndex();
        }
        update.endIndex();
    }

    @Test
    public void testReport2LevelSize3() {
        progressReportMock.onChanged(Phase.STARTED, "Root", 0, 1, 0);
        progressReportMock.onChanged(Phase.BEGIN_ITEM, "Root", 0, 1, 0);

        int level = 1;
        int size = 3;
        progressReportMock.onChanged(Phase.STARTED, level + " Loop", 0, size, level);
        for (int i = 0; i < size; i++) {
            progressReportMock.onChanged(Phase.BEGIN_ITEM, level + " Loop", i, size, level);
            progressReportMock.onChanged(Phase.END_ITEM, level + " Loop", i, size, level);
        }
        progressReportMock.onChanged(Phase.FINISHED, level + " Loop", size, size, level);

        progressReportMock.onChanged(Phase.END_ITEM, "Root", 0, 1, 0);
        progressReportMock.onChanged(Phase.FINISHED, "Root", 1, 1, 0);

        replayAll();

        IProgressUpdate update = ProgressUpdate.start(progressReportMock, "Root", 1);
        update.beginIndex();
        IProgressUpdate progress2 = ProgressUpdate.start(level + " Loop", size);
        for (int i = 0; i < size; i++) {
            progress2.beginIndex();
        }
        update.endIndex();
    }

    private void doFor(int level, int maxLevel) {
        int size = 0;
        IProgressUpdate progress = ProgressUpdate.start(level + " Loop", size);
        for (int i = 0; i < size; i++) {
            progress.beginIndex();
            if (level < maxLevel) {
                doFor(level + 1, maxLevel);
            }
        }
    }
}
