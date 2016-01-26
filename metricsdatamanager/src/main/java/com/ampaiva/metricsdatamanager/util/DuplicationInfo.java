package com.ampaiva.metricsdatamanager.util;

public class DuplicationInfo {
    public final int count;
    public final int position0;
    public final int position1;

    public DuplicationInfo(int count, int position0, int position1) {
        this.count = count;
        this.position0 = position0;
        this.position1 = position1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + count;
        result = prime * result + position0;
        result = prime * result + position1;
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
        DuplicationInfo other = (DuplicationInfo) obj;
        if (count != other.count) {
            return false;
        }
        if (position0 != other.position0) {
            return false;
        }
        if (position1 != other.position1) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DuplicationInfo [count=" + count + ", position0=" + position0 + ", position1=" + position1 + "]";
    }
}