package com.ampaiva.metricsdatamanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FilterClonePair {

    public static List<ClonePair> getClonePairs(List<ClonePair> clones) {
        Set<ClonePair> clonesData = Collections.synchronizedSortedSet(new TreeSet<>());
        for (ClonePair clone : clones) {
            clonesData.add(clone);
        }
        List<ClonePair> result = new ArrayList<>();
        Set<String> hash = new HashSet<>();
        for (ClonePair cloneData : clonesData) {
            if (!hash.contains(cloneData.getKey())) {
                hash.add(cloneData.getKey());
                result.add(cloneData);
            }
        }
        return result;
    }

}
