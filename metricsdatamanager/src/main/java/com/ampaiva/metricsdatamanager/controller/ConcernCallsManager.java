package com.ampaiva.metricsdatamanager.controller;

import japa.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.ampaiva.metricsdatamanager.util.IProgressUpdate;
import com.ampaiva.metricsdatamanager.util.LCS;
import com.ampaiva.metricsdatamanager.util.MatchesData;
import com.ampaiva.metricsdatamanager.util.ProgressUpdate;
import com.ampaiva.metricsdatamanager.util.SequenceMatch;

public class ConcernCallsManager {
    public static final String SEPARATOR = "#";
    private final IHashArray hashArray;
    private final IConcernCallsConfig config;
    private final IProgressUpdate progressUpdate;

    public ConcernCallsManager(IConcernCallsConfig config, IHashArray hashArray, IProgressUpdate progressUpdate) {
        this.config = config;
        this.hashArray = hashArray;
        this.progressUpdate = progressUpdate;
    }

    public ConcernCallsManager(IConcernCallsConfig config, IHashArray hashArray) {
        this(config, hashArray, new ProgressUpdate());
    }

    private int[] getDuplications(List<Integer> seqA, List<Integer> seqB) {
        int minSeq = config.getMinSeq();
        int maxDistance = config.getMaxDistance();
        if (seqA.size() >= minSeq && seqB.size() >= minSeq) {
            Integer[] a = seqA.toArray(new Integer[seqA.size()]);
            Integer[] b = seqB.toArray(new Integer[seqB.size()]);
            int[] indexes = LCS.lcs(LCS.convert(a), LCS.convert(b));
            int duplications = indexes.length / 2;
            if (duplications >= minSeq) {
                int seqCount = 1;
                for (int i = 2; i < indexes.length; i += 2) {
                    int distance1 = indexes[i] - indexes[i - 2] - 1;
                    int distance2 = indexes[i + 1] - indexes[i - 1] - 1;
                    if (distance1 <= maxDistance && distance2 <= maxDistance) {
                        seqCount++;
                    } else {
                        seqCount = 1;
                    }
                    if (seqCount >= minSeq) {
                        return indexes;
                    }
                }
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

    public List<List<List<List<int[]>>>> getDuplications(List<IMethodCalls> concernCollections) {
        setCallsHash(concernCollections);
        List<List<List<List<int[]>>>> duplications = new ArrayList<List<List<List<int[]>>>>(concernCollections.size());
        progressUpdate.start(concernCollections.size() - 1);
        for (int i = 0; i < progressUpdate.limit(); i++, progressUpdate.step()) {
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
            List<ICodeSource> codeSources) throws ParseException, IOException {
        final List<IMethodCalls> concernCollections = new ArrayList<IMethodCalls>();
        for (ICodeSource codeSource : codeSources) {
            MetricsColector metricsColector = new MetricsColector(metricsSource, codeSource);
            persistConcernCollection(metricsColector, concernCollections);
        }

        return concernCollections;
    }

    public List<ConcernClone> _getConcernClones(List<ICodeSource> codeSources) throws IOException, ParseException {
        IMetricsSource metricsSource = new IMetricsSource() {

            @Override
            public List<IConcernMetric> getConcernMetrics() {
                return Arrays.asList((IConcernMetric) new ConcernCollection());
            }
        };
        List<IMethodCalls> allClasses = getConcernCollectionofAllFiles(metricsSource, codeSources);
        List<List<List<List<int[]>>>> dupAllClasses = getDuplications(allClasses);

        return getConcernClones(allClasses, dupAllClasses);
    }

    public List<ConcernClone> getConcernClones(List<ICodeSource> codeSources) throws IOException, ParseException {
        IMetricsSource metricsSource = new IMetricsSource() {

            @Override
            public List<IConcernMetric> getConcernMetrics() {
                return Arrays.asList((IConcernMetric) new ConcernCollection());
            }
        };
        List<IMethodCalls> allMethodCalls = getConcernCollectionofAllFiles(metricsSource, codeSources);
        setCallsHash(allMethodCalls);
        List<String> methodNames = new ArrayList<String>();
        List<String> methodSources = new ArrayList<String>();
        List<List<String>> methodSequences = new ArrayList<List<String>>();
        List<List<Integer>> sequences = new ArrayList<List<Integer>>();
        for (IMethodCalls methodCall : allMethodCalls) {
            methodNames.addAll(methodCall.getMethodNames());
            methodSources.addAll(methodCall.getMethodSources());
            methodSequences.addAll(methodCall.getSequences());
            sequences.addAll(getCallsIndexes(methodCall.getSequences()));
        }
        SequenceMatch sequenceMatch = new SequenceMatch(sequences, config.getMinSeq(), config.getMaxDistance(),
                progressUpdate);
        List<ConcernClone> concernClones = new ArrayList<ConcernClone>();
        for (MatchesData matchesData : sequenceMatch.getMatches()) {
            for (int i = 0; i < matchesData.groupsMatched.size(); i++) {
                int matchedIndex = matchesData.groupsMatched.get(i);
                ConcernClone clone = new ConcernClone();
                clone.methods = Arrays.asList(methodNames.get(matchesData.groupIndex), methodNames.get(matchedIndex));
                clone.sources = Arrays.asList(methodSources.get(matchesData.groupIndex),
                        methodSources.get(matchedIndex));
                clone.sequences = Arrays.asList(methodSequences.get(matchesData.groupIndex),
                        methodSequences.get(matchedIndex));
                clone.duplications = matchesData.sequencesMatches.get(i);
                concernClones.add(clone);
            }
        }
        return concernClones;
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
                        ConcernClone clone = getConcernClone(allClasses.get(i), allClasses.get(j + i + 1), k, l, dups);
                        clones.add(clone);
                    }
                }
            }
        }
        return clones;
    }

    private List<String> getSequences(ConcernClone clone, int sourceIndex) {
        List<String> list = new ArrayList<String>();
        int[] lastPrintedA = new int[2];
        List<List<Integer>> indexes = clone.duplications;
        List<List<String>> sequencesA = clone.sequences;
        int dupNo = 1;
        for (int k = 0; k < indexes.size(); k += 2) {
            int indexA = 0; //indexes.get(k + sourceIndex);
            while (lastPrintedA[sourceIndex] < indexA) {
                String seqAIndex = sequencesA.get(sourceIndex).get(lastPrintedA[sourceIndex]++);
                String strA = ".. [**]" + ":" + seqAIndex;
                list.add(strA);
            }
            lastPrintedA[sourceIndex] = indexA + 1;

            String seqAIndex = sequencesA.get(sourceIndex).get(indexA);
            String strA = dupNo++ + " :" + seqAIndex;
            list.add(strA);
        }
        return list;
    }

    public ConcernClone getConcernClone(IMethodCalls concernCollectionA, IMethodCalls concernCollectionB,
            int methodAIndex, int methodBIndex, int[] indexes) {
        ConcernClone clone = new ConcernClone();
        clone.methods = Arrays.asList(concernCollectionA.getMethodNames().get(methodAIndex), concernCollectionB
                .getMethodNames().get(methodBIndex));
        clone.sources = Arrays.asList(concernCollectionA.getMethodSources().get(methodAIndex), concernCollectionB
                .getMethodSources().get(methodBIndex));
        clone.sequences = Arrays.asList(concernCollectionA.getSequences().get(methodAIndex), concernCollectionB
                .getSequences().get(methodBIndex));
        clone.duplications = null; //indexes;
        return clone;
    }

    public void _showSequences(ConcernClone clone) {
        System.out.println();
        for (int i = 0; i < 2; i++) {
            List<String> sequences = getSequences(clone, i);
            for (String item : sequences) {
                System.out.println(item);
            }
            System.out.println("===== " + i + " =====");
            System.out.println(clone.sources.get(i));
            System.out.println("===== " + i + " =====");
            System.out.println();
        }
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
