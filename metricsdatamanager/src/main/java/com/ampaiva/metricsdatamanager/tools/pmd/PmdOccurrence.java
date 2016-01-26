package com.ampaiva.metricsdatamanager.tools.pmd;

public class PmdOccurrence {
    public final PmdClone pmdClone;
    public final int line;
    public final String file;
    public final String source;

    public PmdOccurrence(PmdClone pmdClone, int line, String file, String source) {
        this.pmdClone = pmdClone;
        this.line = line;
        this.file = file;
        this.source = source;
    }

    @Override
    public String toString() {
        return "PmdOcurrency [line=" + line + ", file=" + file + "]";
    }
}