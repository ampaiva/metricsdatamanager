package com.ampaiva.metricsdatamanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FilterClonePair {

    public static List<CloneGroup> getClonePairs(List<CloneGroup> clones) {
        Set<CloneGroup> clonesData = Collections.synchronizedSortedSet(new TreeSet<>());
        for (CloneGroup clone : clones) {
            clonesData.add(clone);
        }
        List<CloneGroup> result = new ArrayList<>();
        result.addAll(clonesData);

        return result;
    }

}
