package com.ampaiva.metricsdatamanager.controller;

import japa.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.ampaiva.hlo.cm.ConcernCollection;
import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.cm.IConcernMetric;
import com.ampaiva.hlo.cm.IMethodCalls;
import com.ampaiva.hlo.cm.IMetricsSource;
import com.ampaiva.hlo.cm.MetricsColector;
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

    public List<List<List<List<int[]>>>> getDuplications2(List<IMethodCalls> concernCollections) {
        setCallsHash(concernCollections);
        List<List<List<List<int[]>>>> duplications = new ArrayList<List<List<List<int[]>>>>(concernCollections.size());
        for (int i = 0; i < concernCollections.size() - 1; i++) {
            IMethodCalls concernCollection = concernCollections.get(i);
            List<IMethodCalls> subList = concernCollections.subList(i + 1, concernCollections.size());
            duplications.add(getAllDuplications(concernCollection, subList));
        }
        return duplications;
    }

    private void persistConcernCollection(MetricsColector metricsColector, List<IMethodCalls> concernCollections)
            throws ParseException, IOException {
        HashMap<String, List<IConcernMetric>> hash = metricsColector.getMetrics().getHash();
        for (Entry<String, List<IConcernMetric>> entry : hash.entrySet()) {
            for (IConcernMetric concernMetric : entry.getValue()) {
                if (concernMetric instanceof ConcernCollection) {
                    concernCollections.add((ConcernCollection) concernMetric);
                }
            }
        }
    }

    private List<IMethodCalls> getConcernCollectionofAllFiles(IMetricsSource metricsSource,
            List<ICodeSource> codeSources) throws Exception {
        final List<IMethodCalls> concernCollections = new ArrayList<IMethodCalls>();
        for (ICodeSource codeSource : codeSources) {
            MetricsColector metricsColector = new MetricsColector(metricsSource, codeSource);
            persistConcernCollection(metricsColector, concernCollections);
        }

        return concernCollections;
    }

    public List<ConcernClone> getConcernClones(List<ICodeSource> codeSources) throws Exception {
        IMetricsSource metricsSource = new IMetricsSource() {

            @Override
            public List<IConcernMetric> getConcernMetrics() {
                return Arrays.asList((IConcernMetric) new ConcernCollection());
            }
        };
        List<IMethodCalls> allClasses = getConcernCollectionofAllFiles(metricsSource, codeSources);
        List<List<List<List<int[]>>>> dupAllClasses = getDuplications2(allClasses);

        return getConcernClones(allClasses, dupAllClasses);
    }

    private List<ConcernClone> getConcernClones(List<IMethodCalls> allClasses,
            List<List<List<List<int[]>>>> dupAllClasses) {
        List<ConcernClone> clones = new ArrayList<ConcernClone>();
        for (int i = 0; i < dupAllClasses.size(); i++) {
            List<List<List<int[]>>> duplications = dupAllClasses.get(i);
            // Each method of class i is compared against all methods of all other classes
            // So, duplications has size equals the number of classes after i
            for (int j = 0; j < duplications.size(); j++) {
                List<List<int[]>> methodsA = duplications.get(j);
                for (int k = 0; k < methodsA.size(); k++) {
                    List<int[]> methodsB = methodsA.get(k);
                    for (int l = 0; l < methodsB.size(); l++) {
                        int[] dups = methodsB.get(l);
                        if (dups == null) {
                            continue;
                        }
                        ConcernClone clone = convert2(allClasses.get(i), allClasses.get(j + i + 1), k, l, dups);
                        clones.add(clone);
                    }
                }
            }
        }
        return clones;
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

    public ConcernClone convert2(IMethodCalls concernCollectionA, IMethodCalls concernCollectionB, int methodAIndex,
            int methodBIndex, int[] indexes) {
        List<List<Integer>> sequencesA = getCallsIndexes(concernCollectionA.getSequences());
        List<List<Integer>> sequencesB = getCallsIndexes(concernCollectionB.getSequences());
        int lastPrintedA = 0, lastPrintedB = 0;
        ConcernClone clone = new ConcernClone();
        clone.methodA = concernCollectionA.getMethodNames().get(methodAIndex);
        clone.methodB = concernCollectionB.getMethodNames().get(methodBIndex);
        clone.sourceA = concernCollectionA.getMethodSources().get(methodAIndex);
        clone.sourceB = concernCollectionB.getMethodSources().get(methodBIndex);
        clone.sequencesA = new ArrayList<String>();
        clone.sequencesB = new ArrayList<String>();
        for (int k = 0; k < indexes.length; k += 2) {
            int indexA = indexes[k];
            int indexB = indexes[k + 1];
            while (lastPrintedA < indexA) {
                int seqAIndex = sequencesA.get(methodAIndex).get(lastPrintedA++);
                String strA = "**" + ":" + hashArray.getByIndex(seqAIndex);
                clone.sequencesA.add(strA);
            }
            lastPrintedA = indexA + 1;
            while (lastPrintedB < indexB) {
                int seqBIndex = sequencesB.get(methodBIndex).get(lastPrintedB++);
                String strB = "**" + ":" + hashArray.getByIndex(seqBIndex);
                clone.sequencesB.add(strB);
            }
            lastPrintedB = indexB + 1;
            int seqAIndex = sequencesA.get(methodAIndex).get(indexA);
            String strA = indexA + ":" + hashArray.getByIndex(seqAIndex);
            clone.sequencesA.add(strA);

            int seqBIndex = sequencesB.get(methodBIndex).get(indexB);
            String strB = indexB + ":" + hashArray.getByIndex(seqBIndex);
            clone.sequencesB.add(strB);
        }
        return clone;
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
