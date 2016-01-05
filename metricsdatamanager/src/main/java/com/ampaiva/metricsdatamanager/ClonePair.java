package com.ampaiva.metricsdatamanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd.PmdClone;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd.PmdClone.PmdOcurrency;

public class ClonePair implements Comparable<ClonePair> {
    public final CloneSide copy;
    public final CloneSide paste;
    final boolean found;

    public ClonePair(CloneSide side1, CloneSide side2, boolean found) {
        int compare = side1.compareTo(side2);
        copy = compare <= 0 ? side1 : side2;
        paste = compare <= 0 ? side2 : side1;
        this.found = found;
    }

    public static <T> List<ClonePair> getClonePairs(T clone, boolean found) {
        List<ClonePair> clonePairs = new ArrayList<>();
        CloneSide[] sides = convert(clone);
        for (int i = 0; i < sides.length; i++) {
            CloneSide side1 = sides[i];
            for (int j = i + 1; j < sides.length; j++) {
                CloneSide side2 = sides[j];
                clonePairs.add(new ClonePair(side1, side2, found));
            }
        }
        return clonePairs;
    }

    public ClonePair(CloneSide[] sides, boolean found) {
        this(sides[0], sides[1], found);
    }

    private static <T> CloneSide[] convert(T clone) {
        return clone instanceof Clone ? convert((Clone) clone) : convert((PmdClone) clone);

    }

    private static CloneSide[] convert(PmdClone clone) {
        List<CloneSide> clones = new ArrayList<>();
        for (int i = 0; i < clone.ocurrencies.size(); i++) {
            PmdOcurrency ocurrency_i = clone.ocurrencies.get(i);
            int beglinCopy = ocurrency_i.line;
            int endlinCopy = ocurrency_i.line + clone.lines;
            String copyName = getFileName(ocurrency_i.file);
            CloneSide side1 = new CloneSide(copyName, beglinCopy, endlinCopy, ocurrency_i.file);
            clones.add(side1);
        }
        return clones.toArray(new CloneSide[clones.size()]);
    }

    private static CloneSide[] convert(Clone clone) {
        int beglinCopy = clone.getCopy().getBeglin();
        int endlinCopy = clone.getCopy().getMethodBean().getCalls()
                .get(clone.getCopy().getPosition() + clone.getAnalyseBean().getMinSeq() - 1).getEndlin();
        int beglinPaste = clone.getPaste().getBeglin();
        int endlinPaste = clone.getPaste().getMethodBean().getCalls()
                .get(clone.getPaste().getPosition() + clone.getAnalyseBean().getMinSeq() - 1).getEndlin();
        String copyName = getFileName(clone.getCopy().getMethodBean().getUnitBean().getName());
        String pasteName = getFileName(clone.getPaste().getMethodBean().getUnitBean().getName());
        CloneSide side1 = new CloneSide(copyName, beglinCopy, endlinCopy, clone.getCopy().getMethodBean().getSource());
        CloneSide side2 = new CloneSide(pasteName, beglinPaste, endlinPaste,
                clone.getPaste().getMethodBean().getSource());
        return new CloneSide[] { side1, side2 };

    }

    private static String getFileName(String filePath) {
        return new File(filePath).getName().replaceFirst("(.*)\\.java", "$1");
    }

    @Override
    public int compareTo(ClonePair other) {
        int compare = copy.compareTo(other.copy);
        if (compare == 0) {
            compare = paste.compareTo(other.paste);
            if (compare == 0) {
                // false (not found) > true (found)
                // however, in Java
                // false < true
                // so, we invert the comparison
                compare = new Boolean(other.found).compareTo(new Boolean(found));
            }
        }
        return compare;
    }

    public String getKey() {
        return copy.name + "/" + paste.name;
    }

    @Override
    public String toString() {
        return "ClonePair [copy=" + copy + ", paste=" + paste + ", found=" + found + "]";
    }
}
