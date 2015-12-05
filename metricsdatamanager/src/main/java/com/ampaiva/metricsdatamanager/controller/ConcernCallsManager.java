package com.ampaiva.metricsdatamanager.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ampaiva.hlo.cm.ConcernCollection;
import com.ampaiva.hlo.cm.ConcernMetricNode;
import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.cm.IConcernMetric;
import com.ampaiva.hlo.cm.IMethodCalls;
import com.ampaiva.hlo.cm.IMetricsSource;
import com.ampaiva.hlo.cm.MetricsColector;
import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressUpdate;
import com.ampaiva.metricsdatamanager.model.Call;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.model.Unit;
import com.ampaiva.metricsdatamanager.util.MatchesData;
import com.ampaiva.metricsdatamanager.util.SequenceMatch;
import com.ampaiva.metricsdatamanager.util.SequencesInt;
import com.ampaiva.metricsdatamanager.util.SequencesMap;
import com.github.javaparser.ParseException;

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

    private void persistConcernCollection(MetricsColector metricsColector, Map<String, String> codeSourceMap,
            List<IMethodCalls> methodCalls) throws ParseException, IOException {
        Map<String, List<IConcernMetric>> hash = metricsColector.getMetrics(codeSourceMap).getHash();
        for (Entry<String, List<IConcernMetric>> entry : hash.entrySet()) {
            for (IConcernMetric concernMetric : entry.getValue()) {
                if (concernMetric instanceof ConcernCollection) {
                    methodCalls.add((ConcernCollection) concernMetric);
                }
            }
        }
    }

    private List<IMethodCalls> getConcernCollectionofAllFiles(IMetricsSource metricsSource,
            List<Map<String, String>> codeSourcesMaps) throws ParseException, IOException {
        final List<IMethodCalls> methodCalls = new ArrayList<IMethodCalls>();
        IProgressUpdate update = ProgressUpdate.start("Processing code source", codeSourcesMaps.size());
        for (Map<String, String> codeSource : codeSourcesMaps) {
            update.beginIndex(codeSource);
            MetricsColector metricsColector = new MetricsColector(metricsSource);
            persistConcernCollection(metricsColector, codeSource, methodCalls);
        }

        return methodCalls;
    }

    public Repository createRepository(List<ICodeSource> codeSources, String location, List<Sequence> sequences)
            throws FileNotFoundException, IOException, ParseException {
        Repository repository = new Repository();
        repository.setLocation(location);
        List<Unit> units = new ArrayList<>();
        List<Map<String, String>> codeSourceMaps = new ArrayList<>();
        for (ICodeSource codeSource : codeSources) {
            Map<String, String> codeSourceMap = codeSource.getCodeSource();
            codeSourceMaps.add(codeSourceMap);
        }
        for (Map<String, String> codeSourceMap : codeSourceMaps) {
            for (Entry<String, String> entry : codeSourceMap.entrySet()) {
                Unit unit = new Unit();
                unit.setRepositoryBean(repository);
                unit.setName(entry.getKey());
                unit.setSource(entry.getValue());
                units.add(unit);
                IConcernMetric concernMetric = new ConcernCollection();
                concernMetric.parse(unit.getSource());
                List<Method> methods = getMethods(sequences, (IMethodCalls) concernMetric);
                for (Method method : methods) {
                    method.setUnitBean(unit);
                }
                unit.setMethods(methods);
            }
        }
        repository.setUnits(units);
        return repository;
    }

    public List<Method> getMethodCodes(List<Sequence> sequences, List<Map<String, String>> codeSourcesMaps)
            throws IOException, ParseException {
        IMetricsSource metricsSource = new IMetricsSource() {

            @Override
            public List<IConcernMetric> getConcernMetrics() {
                return Arrays.asList((IConcernMetric) new ConcernCollection());
            }
        };
        List<IMethodCalls> allMethodCalls = getConcernCollectionofAllFiles(metricsSource, codeSourcesMaps);
        return getMethods(sequences, allMethodCalls);
    }

    private List<Method> getMethods(List<Sequence> sequences, List<IMethodCalls> allMethodCalls) {
        List<Method> methodCodes = new ArrayList<Method>();
        for (IMethodCalls methodCall : allMethodCalls) {
            for (int i = 0; i < methodCall.getMethodNames().size(); i++) {
                Method method = getMethod(sequences, methodCall, i);
                methodCodes.add(method);
            }
        }
        return methodCodes;
    }

    private List<Method> getMethods(List<Sequence> sequences, IMethodCalls methodCall) {
        List<Method> methodCodes = new ArrayList<Method>();
        for (int i = 0; i < methodCall.getMethodNames().size(); i++) {
            Method method = getMethod(sequences, methodCall, i);
            methodCodes.add(method);
        }
        return methodCodes;
    }

    private Method getMethod(List<Sequence> sequences, IMethodCalls methodCall, int i) {
        Method method = new Method();
        method.setName(methodCall.getMethodNames().get(i));
        method.setSource(methodCall.getMethodSources().get(i));
        method.setCalls(new ArrayList<Call>());
        List<String> seq = methodCall.getSequences().get(i);
        for (int order = 0; order < seq.size(); order++) {
            String sequenceName = seq.get(order);
            if (sequenceName.length() > 255) {
                continue;
            }
            Call call = new Call();
            call.setPosition(order);
            ConcernMetricNode concernMetricNode = ((ConcernCollection) methodCall).getNodes().get(order);
            call.setBeglin(concernMetricNode.getBeginLine());
            call.setEndlin(concernMetricNode.getEndLine());
            call.setBegcol(concernMetricNode.getBeginColumn());
            call.setEndcol(concernMetricNode.getEndColumn());
            Sequence sequence = null;
            for (Sequence sequenceT : sequences) {
                if (sequenceT.getName().equals(sequenceName)) {
                    sequence = sequenceT;
                    break;
                }

            }
            if (sequence == null) {
                sequence = new Sequence();
                sequence.setName(sequenceName);
                sequences.add(sequence);
            }
            call.setSequenceBean(sequence);
            call.setMethodBean(method);
            method.getCalls().add(call);
        }
        return method;
    }

    public List<MatchesData> getSequenceMatches() {
        SequenceMatch sequenceMatch = new SequenceMatch(sequencesInt.getSequencesInt());
        if (sequencesMap == null) {
            sequencesMap = new SequencesMap(sequencesInt.getSequencesInt());
        }
        return sequenceMatch.getMatches(sequencesMap.getMap());
    }

    public List<ConcernClone> getConcernClones(List<MatchesData> sequenceMatches, List<Unit> units) {
        List<Method> methodCodes = new ArrayList<>();
        for (Unit unit : units) {
            methodCodes.addAll(unit.getMethods());
        }
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
        clone.methods = Arrays.asList(concernCollectionA.getMethodNames().get(methodAIndex),
                concernCollectionB.getMethodNames().get(methodBIndex));
        clone.sources = Arrays.asList(concernCollectionA.getMethodSources().get(methodAIndex),
                concernCollectionB.getMethodSources().get(methodBIndex));
        clone.sequences = Arrays.asList(concernCollectionA.getSequences().get(methodAIndex),
                concernCollectionB.getSequences().get(methodBIndex));
        clone.duplications = null; //indexes;
        return clone;
    }

}
