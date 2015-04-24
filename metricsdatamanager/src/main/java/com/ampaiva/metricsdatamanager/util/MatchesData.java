package com.ampaiva.metricsdatamanager.util;

import java.util.ArrayList;
import java.util.List;

public class MatchesData {
    // Index of groups where copy was found
    public int groupIndex;
    // Indexes of groups where paste was found
    public List<Integer> groupsMatched;
    // Sequences for each group matched
    public List<List<List<Integer>>> sequencesMatches;

    public MatchesData(int groupIndex, List<Integer> groupsMatched, List<List<List<Integer>>> sequencesMatches) {
        this.groupIndex = groupIndex;
        this.groupsMatched = groupsMatched;
        this.sequencesMatches = sequencesMatches;
    }

    public MatchesData(int groupIndex) {
        this(groupIndex, new ArrayList<Integer>(), new ArrayList<List<List<Integer>>>());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + groupIndex;
        result = prime * result + ((groupsMatched == null) ? 0 : groupsMatched.hashCode());
        result = prime * result + ((sequencesMatches == null) ? 0 : sequencesMatches.hashCode());
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
        if (groupIndex != other.groupIndex) {
            return false;
        }
        if (groupsMatched == null) {
            if (other.groupsMatched != null) {
                return false;
            }
        } else if (!groupsMatched.equals(other.groupsMatched)) {
            return false;
        }
        if (sequencesMatches == null) {
            if (other.sequencesMatches != null) {
                return false;
            }
        } else if (!sequencesMatches.equals(other.sequencesMatches)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MatchesData [groupIndex=" + groupIndex + ", groupsMatched=" + groupsMatched + ", sequencesMatches="
                + sequencesMatches + "]";
    }
}