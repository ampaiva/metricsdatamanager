package com.ampaiva.metricsdatamanager.controller;

import japa.parser.ParseException;

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
import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressUpdate;
import com.ampaiva.metricsdatamanager.config.IConcernCallsConfig;
import com.ampaiva.metricsdatamanager.model.Call;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.util.MatchesData;
import com.ampaiva.metricsdatamanager.util.SequenceMatch;
import com.ampaiva.metricsdatamanager.util.SequencesInt;
import com.ampaiva.metricsdatamanager.util.SequencesMap;

public class ConcernCallsManager {
    public static final String SEPARATOR = "#";
    private SequencesMap sequencesMap;
    private final SequencesInt sequencesInt;

    public ConcernCallsManager(SequencesInt sequencesInt) {
        this.sequencesInt = sequencesInt;
    }

    public ConcernCallsManager() {
        this(null);
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

    public Repository createRepository(List<ICodeSource> codeSources, String location, List<Sequence> sequences)
            throws FileNotFoundException, IOException, ParseException {
        Repository repository = new Repository();
        repository.setLocation(location);
        List<Method> methods = getMethodCodes(sequences, codeSources);
        for (Method method : methods) {
            method.setRepositoryBean(repository);
        }
        repository.setMethods(methods);
        return repository;
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
                for (int order = 0; order < seq.size(); order++) {
                    String sequenceName = seq.get(order);
                    if (sequenceName.length() > 255) {
                        continue;
                    }
                    Call call = new Call();
                    call.setPosition(order);
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

    public List<MatchesData> getSequenceMatches(IConcernCallsConfig config) {
        SequenceMatch sequenceMatch = new SequenceMatch(sequencesInt.getSequencesInt(), config.getMinSeq());
        if (sequencesMap == null) {
            sequencesMap = new SequencesMap(sequencesInt.getSequencesInt());
        }
        return sequenceMatch.getMatches(sequencesMap.getMap());
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
                clone.sequences = Arrays.asList(
                        SequencesInt.callsToStringList(methodCodes.get(matchesData.groupIndex).getCalls()),
                        SequencesInt.callsToStringList(methodCodes.get(matchedIndex).getCalls()));
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

}
