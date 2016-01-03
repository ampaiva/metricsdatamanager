package com.ampaiva.metricsdatamanager;

import java.io.File;

import com.ampaiva.metricsdatamanager.model.Clone;

public class ClonePair implements Comparable<ClonePair> {
    final CloneSide copy;
    final CloneSide paste;

    public ClonePair(Clone clone) {
        int beglinCopy = clone.getCopy().getBeglin();
        int endlinCopy = clone.getCopy().getMethodBean().getCalls()
                .get(clone.getCopy().getPosition() + clone.getAnalyseBean().getMinSeq() - 1).getEndlin();
        int beglinPaste = clone.getPaste().getBeglin();
        int endlinPaste = clone.getPaste().getMethodBean().getCalls()
                .get(clone.getPaste().getPosition() + clone.getAnalyseBean().getMinSeq() - 1).getEndlin();
        String copyName = new File(clone.getCopy().getMethodBean().getUnitBean().getName()).getName();
        String pasteName = new File(clone.getPaste().getMethodBean().getUnitBean().getName()).getName();
        CloneSide side1 = new CloneSide(copyName, beglinCopy, endlinCopy);
        CloneSide side2 = new CloneSide(pasteName, beglinPaste, endlinPaste);
        int compare = side1.compareTo(side2);
        copy = compare <= 0 ? side1 : side2;
        paste = compare <= 0 ? side2 : side1;
    }

    @Override
    public int compareTo(ClonePair other) {
        int compare = copy.compareTo(other.copy);
        if (compare == 0) {
            compare = paste.compareTo(other.paste);
        }
        return compare;
    }

    public String getKey() {
        return copy.name + "/" + paste.name;
    }
}
