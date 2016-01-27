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

    public static List<CloneInfo> merge(List<MatchesData> matchesDatas) {
        List<CloneInfo> cloneInfos = new ArrayList<>();
        for (MatchesData matchesData : matchesDatas) {
            int method0 = matchesData.methodIndex;
            for (int j = 0; j < matchesData.methodsMatched.size(); j++) {
                Integer method1 = matchesData.methodsMatched.get(j);
                List<Integer> calls0 = new ArrayList<>();
                List<Integer> calls1 = new ArrayList<>();
                for (List<Integer> callIndex : matchesData.callsMatched.get(j)) {
                    calls0.add(callIndex.get(0));
                    calls1.add(callIndex.get(1));
                }
                CloneInfo cloneInfo0 = getCloneInfo(cloneInfos, method0, calls0);
                CloneInfo cloneInfo1 = getCloneInfo(cloneInfos, method1, calls1);
                if (cloneInfo0 != null && cloneInfo1 == null) {
                    merge(cloneInfo0, method1, calls1);
                } else if (cloneInfo0 == null && cloneInfo1 != null) {
                    merge(cloneInfo1, method0, calls0);
                } else if (cloneInfo0 == null && cloneInfo1 == null) {
                    CloneInfo cloneInfo = new CloneInfo();
                    merge(cloneInfo, method0, calls0);
                    merge(cloneInfo, method1, calls1);
                    cloneInfos.add(cloneInfo);
                }
            }
        }
        return cloneInfos;
    }

    private static void merge(CloneInfo cloneInfo, Integer method, List<Integer> calls) {
        cloneInfo.methods.add(method);
        cloneInfo.calls.add(calls);
    }

    private static CloneInfo getCloneInfo(List<CloneInfo> cloneInfos, int method, List<Integer> calls) {
        for (CloneInfo cloneInfo : cloneInfos) {
            for (int i = 0; i < cloneInfo.methods.size(); i++) {
                if (cloneInfo.methods.get(i) == method) {
                    if (cloneInfo.calls.get(i).size() == calls.size()) {
                        boolean hasAllCalls = true;
                        for (int j = 0; j < cloneInfo.calls.get(i).size(); j++) {
                            if (cloneInfo.calls.get(i).get(j).intValue() != calls.get(j).intValue()) {
                                hasAllCalls = false;
                                break;
                            }
                        }
                        if (hasAllCalls) {
                            return cloneInfo;
                        }
                    }
                    break;
                }
            }
        }
        return null;
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