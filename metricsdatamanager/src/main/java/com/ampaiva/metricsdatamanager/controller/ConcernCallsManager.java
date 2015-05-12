package com.ampaiva.metricsdatamanager.controller;

import japa.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
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
import com.ampaiva.hlo.util.Helper;
import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressUpdate;
import com.ampaiva.metricsdatamanager.config.IConcernCallsConfig;
import com.ampaiva.metricsdatamanager.model.Call;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.util.HashArray;
import com.ampaiva.metricsdatamanager.util.IHashArray;
import com.ampaiva.metricsdatamanager.util.MatchesData;
import com.ampaiva.metricsdatamanager.util.SequenceMatch;
import com.ampaiva.metricsdatamanager.util.ZipStreamUtil;

public class ConcernCallsManager {
    public static final String SEPARATOR = "#";

    public ConcernCallsManager() {
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

    public Repository createRepository(File file, List<Sequence> sequences) throws FileNotFoundException, IOException,
            ParseException {
        Repository repository = new Repository();
        repository.setLocation(file.getName());
        List<Method> methods = getMethodCodes(sequences, file);
        for (Method method : methods) {
            method.setRepositoryBean(repository);
        }
        repository.setMethods(methods);
        return repository;
    }

    private List<Method> getMethodCodes(List<Sequence> sequences, File file) throws FileNotFoundException, IOException,
            ParseException {
        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(file.toString(), Helper.convertFile2InputStream(new File(file
                .getAbsolutePath())));
        List<ICodeSource> codeSources = Arrays.asList((ICodeSource) zipStreamUtil);
        List<Method> methodCodes = getMethodCodes(sequences, codeSources);
        return methodCodes;
    }

    public List<Method> getMethodCodes(List<Sequence> sequences, List<ICodeSource> codeSources) throws IOException,
            ParseException {
        IMetricsSource metricsSource = new IMetricsSource() {

            @Override
            public List<IConcernMetric> getConcernMetrics() {
                return Arrays.asList((IConcernMetric) new ConcernCollection());
            }
        };
        List<IMethodCalls> allMethodCalls = getConcernCollectionofAllFiles(metricsSource, codeSources);
        List<Method> methodCodes = new ArrayList<Method>();
        for (IMethodCalls methodCall : allMethodCalls) {
            for (int i = 0; i < methodCall.getMethodNames().size(); i++) {
                Method method = new Method(methodCall.getMethodNames().get(i), methodCall.getMethodSources().get(i));
                method.setCalls(new ArrayList<Call>());
                List<String> seq = methodCall.getSequences().get(i);
                for (final String sequenceName : seq) {
                    Call call = new Call();

                    Sequence sequence = null;
                    for (Sequence sequenceT : sequences) {
                        if (sequenceT.getName().equals(sequenceName)) {
                            sequence = sequenceT;
                            break;
                        }

                    }
                    if (sequence == null) {
                        sequence = new Sequence(sequenceName);
                        sequences.add(sequence);
                    }
                    call.setSequence(sequence);
                    call.setMethodBean(method);
                    method.getCalls().add(call);
                }
                methodCodes.add(method);
            }
        }
        return methodCodes;
    }

    private List<List<String>> getSequences(List<Method> methods) {
        List<List<String>> sequencesStr = new ArrayList<List<String>>();
        for (Method method : methods) {
            List<String> callNames = callsToStringList(method.getCalls());
            sequencesStr.add(callNames);
        }
        return sequencesStr;
    }

    private List<String> callsToStringList(List<Call> calls) {

        List<String> callNames = new ArrayList<String>();
        for (Call call : calls) {
            callNames.add(call.getSequence().getName());
        }
        return callNames;
    }

    private List<List<Integer>> getSequencesInt(IHashArray hashArray, List<Method> methods) {
        List<List<String>> sequencesStr = getSequences(methods);
        //        syncHashArray(hashArray, methodCodes);

        List<List<Integer>> sequencesInt = getCallsIndexes(hashArray, sequencesStr);
        return sequencesInt;
    }

    public List<MatchesData> getSequenceMatches(List<Sequence> sequences, List<Method> methods,
            IConcernCallsConfig config) {
        IHashArray hashArray = new HashArray();
        for (int i = 0; i < sequences.size(); i++) {
            hashArray.put(sequences.get(i).getName());
        }
        List<List<Integer>> sequencesInt = getSequencesInt(hashArray, methods);
        SequenceMatch sequenceMatch = new SequenceMatch(sequencesInt, config.getMinSeq(), config.getMaxDistance());
        return sequenceMatch.getMatches();
    }

    public List<ConcernClone> getConcernClones(List<MatchesData> sequenceMatches, List<Method> methodCodes) {
        List<ConcernClone> concernClones = new ArrayList<ConcernClone>();
        for (MatchesData matchesData : sequenceMatches) {
            for (int i = 0; i < matchesData.groupsMatched.size(); i++) {
                int matchedIndex = matchesData.groupsMatched.get(i);
                ConcernClone clone = new ConcernClone();
                clone.methods = Arrays.asList(methodCodes.get(matchesData.groupIndex).getName(),
                        methodCodes.get(matchedIndex).getName());
                clone.sources = Arrays.asList(methodCodes.get(matchesData.groupIndex).getSource(),
                        methodCodes.get(matchedIndex).getSource());
                clone.sequences = Arrays.asList(callsToStringList(methodCodes.get(matchesData.groupIndex).getCalls()),
                        callsToStringList(methodCodes.get(matchedIndex).getCalls()));
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

    private List<List<Integer>> getCallsIndexes(IHashArray hashArray, List<List<String>> sequences) {
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
}
