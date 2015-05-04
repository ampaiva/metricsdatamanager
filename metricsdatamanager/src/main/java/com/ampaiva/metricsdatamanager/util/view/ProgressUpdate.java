package com.ampaiva.metricsdatamanager.util.view;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ampaiva.metricsdatamanager.util.view.IProgressReport.Phase;

public class ProgressUpdate implements IProgressUpdate {

    private static final Log LOG = LogFactory.getLog(ProgressUpdate.class);

    private static Map<Thread, LinkedList<ProgressUpdate>> map = new ConcurrentHashMap<Thread, LinkedList<ProgressUpdate>>();

    private final String id;
    private final int size;
    private final int level;
    private int index;
    private int prevIndex = -1;
    private final IProgressReport progressReport;

    private ProgressUpdate(IProgressReport progressReport, String id, int size, int level) {
        this.id = id;
        this.progressReport = progressReport;
        this.size = size;
        this.level = level;
    }

    private static LinkedList<ProgressUpdate> getLinkedList() {
        LinkedList<ProgressUpdate> current = map.get(Thread.currentThread());
        return current;
    }

    public static IProgressUpdate start(IProgressReport progressReport, String id, int size) {
        LinkedList<ProgressUpdate> linkedList = getLinkedList();
        if (linkedList == null) {
            linkedList = new LinkedList<ProgressUpdate>();
            map.put(Thread.currentThread(), linkedList);
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Progress report already registered");
            }
        }
        return addNew(progressReport, id, size);
    }

    public static IProgressUpdate start(String id, int size) {
        LinkedList<ProgressUpdate> linkedList = getLinkedList();
        if (linkedList != null) {
            return addNew(linkedList.getLast().progressReport, id, size);
        }
        // Fake
        return new IProgressUpdate() {

            @Override
            public void endIndex() {
            }

            @Override
            public void beginIndex() {
            }
        };
    }

    private static IProgressUpdate addNew(IProgressReport progressReport, String id, int size) {
        LinkedList<ProgressUpdate> linkedList = getLinkedList();
        ProgressUpdate progressUpdate = new ProgressUpdate(progressReport, id, size, linkedList.size());
        linkedList.add(progressUpdate);
        progressUpdate.report(Phase.STARTED);
        return progressUpdate;
    }

    @Override
    public void beginIndex() {
        finishChild();
        if (prevIndex == index) {
            endIndex();
        }
        prevIndex = index;

        report(Phase.BEGIN_ITEM);
    }

    @Override
    public void endIndex() {
        finishChild();
        report(Phase.END_ITEM);
        prevIndex = index;
        index++;
        if (index == size) {
            finish();
        }
    }

    private void finish() {
        report(Phase.FINISHED);
        LinkedList<ProgressUpdate> linkedList = getLinkedList();
        linkedList.remove(this);
        if (linkedList.size() == 0) {
            map.remove(Thread.currentThread());
        }
    }

    private void finishChild() {
        LinkedList<ProgressUpdate> linkedList = getLinkedList();
        ProgressUpdate last;
        while ((last = linkedList.getLast()) != this) {
            last.endIndex();
            if (last.index < last.size) {
                last.finish();
            }
        }
    }

    private void report(Phase phase) {
        progressReport.onChanged(phase, id, index, size, level);
    }
}
