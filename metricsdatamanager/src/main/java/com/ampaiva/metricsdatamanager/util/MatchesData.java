package com.ampaiva.metricsdatamanager.util;

import java.util.ArrayList;
import java.util.List;

public class MatchesData {
    // Index of method where copy was found
    public int methodIndex;
    // Indexes of methods where paste was found
    public List<Integer> methodsMatched;
    // Pair of calls matched for each method matched. 
    public List<List<List<Integer>>> callsMatched;

    // methodsMatched.size() == callsMatched.size()
    // callsMatched.get(n).size() == # of coincident calls between methods[methodIndex] and methods[methodsMatched.get(n)]
    // callsMatched.get(n).get(0).size() == 2
    public MatchesData(int groupIndex, List<Integer> groupsMatched, List<List<List<Integer>>> callsMatched) {
        this.methodIndex = groupIndex;
        this.methodsMatched = groupsMatched;
        this.callsMatched = callsMatched;
    }

    public MatchesData(int groupIndex) {
        this(groupIndex, new ArrayList<Integer>(), new ArrayList<List<List<Integer>>>());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + methodIndex;
        result = prime * result + ((methodsMatched == null) ? 0 : methodsMatched.hashCode());
        result = prime * result + ((callsMatched == null) ? 0 : callsMatched.hashCode());
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
        MatchesData other = (MatchesData) obj;
        if (methodIndex != other.methodIndex) {
            return false;
        }
        if (methodsMatched == null) {
            if (other.methodsMatched != null) {
                return false;
            }
        } else if (!methodsMatched.equals(other.methodsMatched)) {
            return false;
        }
        if (callsMatched == null) {
            if (other.callsMatched != null) {
                return false;
            }
        } else if (!callsMatched.equals(other.callsMatched)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MatchesData [groupIndex=" + methodIndex + ", groupsMatched=" + methodsMatched + ", sequencesMatches="
                + callsMatched + "]";
    }
}