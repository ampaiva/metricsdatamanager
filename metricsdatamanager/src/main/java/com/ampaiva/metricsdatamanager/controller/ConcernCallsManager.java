package com.ampaiva.metricsdatamanager.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ampaiva.hlo.cm.ConcernCollection;
import com.ampaiva.hlo.cm.IMethodCalls;
import com.ampaiva.metricsdatamanager.util.IHashArray;
import com.ampaiva.metricsdatamanager.util.LCS;

public class ConcernCallsManager {
    public static final String SEPARATOR = "#";
    private static final int MIN_SEQ = 5;
    private final IHashArray hashArray;

    public ConcernCallsManager(IHashArray hashArray) {
        this.hashArray = hashArray;
    }

    public List<String> getDuplications(List<IMethodCalls> concernCollections) {
        List<String> duplications = new ArrayList<String>();
        for (int i = 0; i < concernCollections.size() - 1; i++) {
            IMethodCalls concernCollection = concernCollections.get(i);
            List<IMethodCalls> subList = concernCollections.subList(i + 1, concernCollections.size());
            duplications.addAll(getDuplications(concernCollection, subList));
        }
        return duplications;
    }

    private Collection<? extends String> getDuplications(IMethodCalls concernCollectionA, List<IMethodCalls> subList) {
        List<String> collection = new ArrayList<String>();
        List<List<Integer>> listA = getCallsIndexes(concernCollectionA.getSequences());
        for (IMethodCalls concernCollectionB : subList) {
            List<List<Integer>> listB = getCallsIndexes(concernCollectionB.getSequences());
            for (int i = 0; i < listA.size(); i++) {
                List<Integer> integersA = listA.get(i);
                if (integersA.size() < MIN_SEQ) {
                    continue;
                }
                Integer[] a = integersA.toArray(new Integer[integersA.size()]);
                for (int j = 0; j < listB.size(); j++) {
                    List<Integer> integersB = listB.get(j);
                    if (integersB.size() < MIN_SEQ) {
                        continue;
                    }
                    Integer[] b = integersB.toArray(new Integer[integersB.size()]);
                    int[] indexes = LCS.lcs(LCS.convert(a), LCS.convert(b));
                    if (indexes.length > MIN_SEQ) {
                        StringBuilder sb = new StringBuilder();
                        for (int k : indexes) {
                            if (sb.length() > 0) {
                                sb.append(SEPARATOR);
                            }
                            String str = k + ":" + hashArray.getByIndex(k);
                            sb.append(str);
                        }
                        String str = ((ConcernCollection) concernCollectionA).getKey() + SEPARATOR
                                + concernCollectionA.getMethodNames().get(i) + SEPARATOR
                                + ((ConcernCollection) concernCollectionB).getKey() + SEPARATOR
                                + concernCollectionB.getMethodNames().get(j) + SEPARATOR + sb.toString();
                        collection.add(str);
                    }
                }
            }
        }
        return collection;
    }

    private List<List<Integer>> getCallsIndexes(List<List<String>> sequences) {
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        for (List<String> sequence : sequences) {
            List<Integer> integers = new ArrayList<Integer>();
            for (String key : sequence) {
                integers.add(hashArray.getByKey(key));
            }
            list.add(integers);
        }

        return list;
    }

    public void setCallsHash(List<IMethodCalls> concernCollections) {
        for (IMethodCalls concernCollection : concernCollections) {
            for (List<String> sequencesList : concernCollection.getSequences()) {
                for (String sequence : sequencesList) {
                    hashArray.put(sequence);
                }
            }
        }
    }

}
