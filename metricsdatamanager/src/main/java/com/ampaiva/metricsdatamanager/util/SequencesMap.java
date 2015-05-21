package com.ampaiva.metricsdatamanager.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressUpdate;

public class SequencesMap {
    private final Map<Integer, List<List<Integer>>> map;

    public SequencesMap(List<List<Integer>> sequences) {
        this.map = createCallMap(sequences);
    }

    public Map<Integer, List<List<Integer>>> getMap() {
        return map;
    }

    private void removeUniqueEntries(Map<Integer, List<List<Integer>>> map) {
        IProgressUpdate update = ProgressUpdate.start("Searching unique", map.entrySet().size());
        List<Integer> keys = new ArrayList<Integer>();
        for (Entry<Integer, List<List<Integer>>> entry : map.entrySet()) {
            update.beginIndex(entry);
            boolean sameGroup = true;
            for (int i = 1; i < entry.getValue().size(); i++) {
                if (entry.getValue().get(i).get(0).intValue() != entry.getValue().get(0).get(0).intValue()) {
                    sameGroup = false;
                    break;
                }
            }
            if (sameGroup) {
                keys.add(entry.getKey());
            }
        }
        update.endIndex();
        IProgressUpdate update2 = ProgressUpdate.start("Removing unique", keys.size());
        for (Integer integer : keys) {
            update2.beginIndex(integer);
            map.remove(integer);
        }
        update2.endIndex();
    }

    private Map<Integer, List<List<Integer>>> createCallMap(List<List<Integer>> sequences) {
        Map<Integer, List<List<Integer>>> map = new HashMap<Integer, List<List<Integer>>>();
        for (int i = 0; i < sequences.size(); i++) {
            List<Integer> sequence = sequences.get(i);
            for (int j = 0; j < sequence.size(); j++) {
                Integer call = sequence.get(j);
                List<List<Integer>> list = map.get(call);
                if (list == null) {
                    list = new ArrayList<List<Integer>>();
                    map.put(call, list);
                }
                list.add(Arrays.asList(i, j));
            }
        }
        removeUniqueEntries(map);
        return map;
    }

}
