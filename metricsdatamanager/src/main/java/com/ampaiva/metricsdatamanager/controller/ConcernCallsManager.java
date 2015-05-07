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
import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressUpdate;
import com.ampaiva.metricsdatamanager.config.IConcernCallsConfig;
import com.ampaiva.metricsdatamanager.model.MethodCode;
import com.ampaiva.metricsdatamanager.util.IHashArray;
import com.ampaiva.metricsdatamanager.util.MatchesData;
import com.ampaiva.metricsdatamanager.util.SequenceMatch;

public class ConcernCallsManager {
    public static final String SEPARATOR = "#";
    private final IHashArray hashArray;
    private final IConcernCallsConfig config;

    public ConcernCallsManager(IConcernCallsConfig config, IHashArray hashArray) {
        this.config = config;
        this.hashArray = hashArray;
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
        IProgressUpdate update = ProgressUpdate.start("Processing code source", codeSources.size());
        for (ICodeSource codeSource : codeSources) {
            update.beginIndex(codeSource);
            MetricsColector metricsColector = new MetricsColector(metricsSource, codeSource);
            persistConcernCollection(metricsColector, concernCollections);
        }

        return concernCollections;
    }

    public List<MethodCode> getMethodCodes(List<ICodeSource> codeSources) throws IOException, ParseException {
        IMetricsSource metricsSource = new IMetricsSource() {

            @Override
            public List<IConcernMetric> getConcernMetrics() {
                return Arrays.asList((IConcernMetric) new ConcernCollection());
            }
        };
        List<IMethodCalls> allMethodCalls = getConcernCollectionofAllFiles(metricsSource, codeSources);
        List<MethodCode> methodCodes = new ArrayList<MethodCode>();
        for (IMethodCalls methodCall : allMethodCalls) {
            for (int i = 0; i < methodCall.getMethodNames().size(); i++) {
                methodCodes.add(new MethodCode(methodCall.getMethodNames().get(i),
                        methodCall.getMethodSources().get(i), methodCall.getSequences().get(i)));
            }
        }
        return methodCodes;
    }

    private List<List<Integer>> initHashWithSequences(List<MethodCode> methodCodes) {
        List<List<Integer>> sequencesInt = new ArrayList<List<Integer>>();
        List<List<String>> sequencesStr = new ArrayList<List<String>>();
        hashArray.clear();
        for (MethodCode methodCode : methodCodes) {
            sequencesStr.add(methodCode.methodSequences);
            for (String sequence : methodCode.methodSequences) {
                hashArray.put(sequence);
            }
        }
        sequencesInt.addAll(getCallsIndexes(sequencesStr));
        return sequencesInt;
    }

    public List<ConcernClone> getConcernClones(List<MethodCode> methodCodes) {
        List<List<Integer>> sequences = initHashWithSequences(methodCodes);
        SequenceMatch sequenceMatch = new SequenceMatch(sequences, config.getMinSeq(), config.getMaxDistance());
        List<ConcernClone> concernClones = new ArrayList<ConcernClone>();
        for (MatchesData matchesData : sequenceMatch.getMatches()) {
            for (int i = 0; i < matchesData.groupsMatched.size(); i++) {
                int matchedIndex = matchesData.groupsMatched.get(i);
                ConcernClone clone = new ConcernClone();
                clone.methods = Arrays.asList(methodCodes.get(matchesData.groupIndex).methodName,
                        methodCodes.get(matchedIndex).methodName);
                clone.sources = Arrays.asList(methodCodes.get(matchesData.groupIndex).methodSource,
                        methodCodes.get(matchedIndex).methodSource);
                clone.sequences = Arrays.asList(methodCodes.get(matchesData.groupIndex).methodSequences,
                        methodCodes.get(matchedIndex).methodSequences);
                clone.duplications = matchesData.sequencesMatches.get(i);
                concernClones.add(clone);
            }
        }
        return concernClones;
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

    public void setCallsHash(List<IMethodCalls> methodCalls) {
        for (IMethodCalls methodCall : methodCalls) {
            setCallsHash2(methodCall.getSequences());
        }
    }

    private void setCallsHash2(List<List<String>> sequences) {
        for (List<String> sequencesList : sequences) {
            for (String sequence : sequencesList) {
                hashArray.put(sequence);
            }
        }
    }
}
