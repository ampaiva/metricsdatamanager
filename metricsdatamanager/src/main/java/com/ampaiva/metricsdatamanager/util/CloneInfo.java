package com.ampaiva.metricsdatamanager.util;

import java.util.ArrayList;
import java.util.List;

public class CloneInfo {
    // Indexes of methods
    public final List<Integer> methods;
    // Calls matched for each method matched. 
    public final List<List<Integer>> calls;

    public CloneInfo(List<Integer> methods, List<List<Integer>> calls) {
        this.methods = methods;
        this.calls = calls;
    }

    public CloneInfo() {
        methods = new ArrayList<>();
        calls = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((calls == null) ? 0 : calls.hashCode());
        result = prime * result + ((methods == null) ? 0 : methods.hashCode());
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
        CloneInfo other = (CloneInfo) obj;
        if (calls == null) {
            if (other.calls != null) {
                return false;
            }
        } else if (!calls.equals(other.calls)) {
            return false;
        }
        if (methods == null) {
            if (other.methods != null) {
                return false;
            }
        } else if (!methods.equals(other.methods)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CloneInfo [methods=" + methods + ", calls=" + calls + "]";
    }

}
