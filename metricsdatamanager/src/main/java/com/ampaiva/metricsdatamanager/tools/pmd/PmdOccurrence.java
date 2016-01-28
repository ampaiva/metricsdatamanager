package com.ampaiva.metricsdatamanager.tools.pmd;

public class PmdOccurrence {
    public final PmdClone pmdClone;
    public final int tokens;
    public final int line;
    public final String file;
    public final String source;

    public PmdOccurrence(PmdClone pmdClone, int line, String file, String source) {
        this.pmdClone = pmdClone;
        this.tokens = pmdClone.tokens;
        this.line = line;
        this.file = file;
        this.source = source;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + line;
        result = prime * result + tokens;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PmdOccurrence other = (PmdOccurrence) obj;
        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }
        if (line != other.line) {
            return false;
        }
        if (tokens != other.tokens) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PmdOccurrence [pmdClone=" + pmdClone + ", line=" + line + ", file=" + file + "]";
    }
}