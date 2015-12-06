package com.ampaiva.metricsdatamanager.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ampaiva.metricsdatamanager.model.Call;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.model.Unit;

public class SequencesInt {

    private final Map<String, Sequence> sequencesMap;
    private final List<Unit> units;
    private final List<List<Integer>> sequencesInt;

    public SequencesInt(Map<String, Sequence> sequencesMap, List<Unit> units) {
        this.sequencesMap = sequencesMap;
        this.units = units;
        this.sequencesInt = initSequencesInt();
    }

    public static List<String> callsToStringList(List<Call> calls) {

        List<String> callNames = new ArrayList<String>();
        for (Call call : calls) {
            callNames.add(call.getSequenceBean().getName());
        }
        return callNames;
    }

    private List<List<String>> getSequences() {
        List<List<String>> sequencesStr = new ArrayList<List<String>>();
        for (Unit unit : units) {
            for (Method method : unit.getMethods()) {
                List<String> callNames = callsToStringList(method.getCalls());
                sequencesStr.add(callNames);
            }
        }
        return sequencesStr;
    }

    private List<List<Integer>> initSequencesInt(IHashArray hashArray) {
        List<List<String>> sequencesStr = getSequences();
        List<List<Integer>> sequencesInt = getCallsIndexes(hashArray, sequencesStr);
        return sequencesInt;
    }

    private List<List<Integer>> initSequencesInt() {
        IHashArray hashArray = new HashArray();
        for (String sequenceName : sequencesMap.keySet()) {
            hashArray.put(sequenceName);
        }
        List<List<Integer>> sequencesInt = initSequencesInt(hashArray);
        return sequencesInt;
    }

    private List<List<Integer>> getCallsIndexes(IHashArray hashArray, List<List<String>> sequencesList) {
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        for (List<String> sequence : sequencesList) {
            List<Integer> integers = new ArrayList<Integer>();
            for (String key : sequence) {
                integers.add(hashArray.getByKey(key));
            }
            list.add(integers);
        }

        return list;
    }

    public List<List<Integer>> getSequencesInt() {
        return sequencesInt;
    }

    @Override
    public String toString() {
        return "SequencesInt [sequencesInt=" + sequencesInt + "]";
    }

}
