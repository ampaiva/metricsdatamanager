package com.ampaiva.metricsdatamanager.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ampaiva.hlo.cm.ConcernCollection;
import com.ampaiva.hlo.cm.IMethodCalls;
import com.ampaiva.metricsdatamanager.config.IConcernCallsConfig;
import com.ampaiva.metricsdatamanager.util.IHashArray;
import com.ampaiva.metricsdatamanager.util.LCS;

public class ConcernCallsManager {
    public static final String SEPARATOR = "#";
    private final IHashArray hashArray;
    private final IConcernCallsConfig config;

    public ConcernCallsManager(IConcernCallsConfig config, IHashArray hashArray) {
        this.config = config;
        this.hashArray = hashArray;
    }

    private int[] getDuplications(List<Integer> seqA, List<Integer> seqB) {
        if (seqA.size() >= config.getMinSeq() && seqB.size() >= config.getMinSeq()) {
            Integer[] a = seqA.toArray(new Integer[seqA.size()]);
            Integer[] b = seqB.toArray(new Integer[seqB.size()]);
            int[] indexes = LCS.lcs(LCS.convert(a), LCS.convert(b));
            if (indexes.length / 2 >= config.getMinSeq()) {
                return indexes;
            }
        }
        return null;
    }

    private List<int[]> getSequenceDuplications(List<Integer> seqA, List<List<Integer>> sequencesB) {
        List<int[]> duplications = new ArrayList<int[]>();
        for (List<Integer> seqB : sequencesB) {
            int[] indexes = getDuplications(seqA, seqB);
            duplications.add(indexes);
        }
        return duplications;
    }

    private List<List<int[]>> getSequencesDuplications(List<List<Integer>> sequencesA, List<List<Integer>> sequencesB) {
        List<List<int[]>> duplications = new ArrayList<List<int[]>>();
        for (List<Integer> seqA : sequencesA) {
            List<int[]> list = getSequenceDuplications(seqA, sequencesB);
            duplications.add(list);
        }
        return duplications;
    }

    private List<List<List<int[]>>> getAllSequencesDuplications(List<List<Integer>> sequencesA,
            List<List<List<Integer>>> sequencesList) {
        List<List<List<int[]>>> duplications = new ArrayList<List<List<int[]>>>();
        for (List<List<Integer>> sequencesB : sequencesList) {
            List<List<int[]>> list = getSequencesDuplications(sequencesA, sequencesB);
            duplications.add(list);
        }
        return duplications;
    }

    public List<List<List<int[]>>> getAllDuplications(IMethodCalls concernCollectionA, List<IMethodCalls> classesList) {
        List<List<Integer>> sequencesA = getCallsIndexes(concernCollectionA.getSequences());
        List<List<List<Integer>>> sequencesList = new ArrayList<List<List<Integer>>>();
        for (IMethodCalls concernCollectionB : classesList) {
            List<List<Integer>> sequencesB = getCallsIndexes(concernCollectionB.getSequences());
            sequencesList.add(sequencesB);
        }
        return getAllSequencesDuplications(sequencesA, sequencesList);
    }

    private Collection<? extends String> getDuplications(IMethodCalls concernCollectionA, List<IMethodCalls> classesList) {
        List<String> collection = new ArrayList<String>();
        List<List<List<int[]>>> duplications = getAllDuplications(concernCollectionA, classesList);
        // for duplications of class A
        for (int i = 0; i < duplications.size(); i++) {
            List<List<int[]>> methodsA = duplications.get(i);
            // for methods of class A
            for (int j = 0; j < methodsA.size(); j++) {
                List<int[]> methodsB = methodsA.get(j);
                // for methods of class B compared against methodsA[j]
                for (int k = 0; k < methodsB.size(); k++) {
                    int[] dups = methodsB.get(k);
                    if (dups == null) {
                        continue;
                    }
                    // Found a duplication methodsA[j] is duplicated with methodsB[k]. dups contains the duplications
                    String str = convert(concernCollectionA, classesList.get(i), j, k, dups);
                    collection.add(str);
                }
            }
        }
        return collection;
    }

    //TODO: Move to a new class
    public List<String> getDuplications(List<IMethodCalls> concernCollections) {
        List<String> duplications = new ArrayList<String>();
        for (int i = 0; i < concernCollections.size() - 1; i++) {
            IMethodCalls concernCollection = concernCollections.get(i);
            List<IMethodCalls> subList = concernCollections.subList(i + 1, concernCollections.size());
            duplications.addAll(getDuplications(concernCollection, subList));
        }
        return duplications;
    }

    private String convert(IMethodCalls concernCollectionA, IMethodCalls concernCollectionB, int methodAIndex,
            int methodBIndex, int[] indexes) {
        List<List<Integer>> sequencesA = getCallsIndexes(concernCollectionA.getSequences());
        StringBuilder sb = new StringBuilder();
        int lastPrinted = 0;
        for (int k = 0; k < indexes.length; k += 2) {
            if (sb.length() > 0) {
                sb.append(SEPARATOR);
            }
            int index = indexes[k];
            while (lastPrinted < index) {
                int seqAIndex = sequencesA.get(methodAIndex).get(lastPrinted++);
                String str = "**" + ":" + hashArray.getByIndex(seqAIndex) + SEPARATOR;
                sb.append(str);

            }
            lastPrinted = index + 1;
            int seqAIndex = sequencesA.get(methodAIndex).get(index);
            String str = index + ":" + hashArray.getByIndex(seqAIndex);
            sb.append(str);
        }
        String str = ((ConcernCollection) concernCollectionA).toString() + SEPARATOR
                + concernCollectionA.getMethodNames().get(methodAIndex) + SEPARATOR
                + ((ConcernCollection) concernCollectionB).toString() + SEPARATOR
                + concernCollectionB.getMethodNames().get(methodBIndex) + SEPARATOR + sb.toString();
        return str;
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
