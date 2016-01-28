package com.ampaiva.metricsdatamanager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ampaiva.hlo.util.Helper;
import com.ampaiva.hlo.util.view.IProgressReport;
import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressReport;
import com.ampaiva.hlo.util.view.ProgressUpdate;
import com.ampaiva.metricsdatamanager.controller.MethodCallsManager;
import com.ampaiva.metricsdatamanager.controller.IDataManager;
import com.ampaiva.metricsdatamanager.model.Analyse;
import com.ampaiva.metricsdatamanager.model.Call;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.model.Unit;
import com.ampaiva.metricsdatamanager.util.CloneInfo;
import com.ampaiva.metricsdatamanager.util.MatchesData;
import com.ampaiva.metricsdatamanager.util.SequencesInt;
import com.github.javaparser.ParseException;

public class PersistDuplications extends ExtractClones {

    final IDataManager dataManager;

    public PersistDuplications(IDataManager dataManager, int minSeq) {
        super(minSeq);
        this.dataManager = dataManager;
    }

    private void commitRepository(Repository repository) {
        List<Unit> units = repository.getUnits();
        repository.setUnits(Collections.<Unit> emptyList());
        commit(repository);
        IProgressUpdate update = ProgressUpdate.start("Persisting units", units.size());
        for (Unit unit : units) {
            update.beginIndex(unit);
            List<Method> methods = unit.getMethods();
            unit.setMethods(Collections.<Method> emptyList());
            commit(unit);
            for (Method method : methods) {
                List<Call> calls = method.getCalls();
                method.setCalls(Collections.<Call> emptyList());
                dataManager.open();
                method.setUnitBean(dataManager.getSingleResult(Unit.class, "Unit.findById", unit.getId()));
                dataManager.persist(method);
                dataManager.close();
                for (Call call : calls) {
                    dataManager.open();
                    call.setMethodBean(dataManager.getSingleResult(Method.class, "Method.findById", method.getId()));
                    Sequence sequence = dataManager.getSingleResult(Sequence.class, "Sequence.findByName",
                            call.getSequenceBean().getName());
                    if (sequence != null) {
                        call.setSequenceBean(sequence);
                    }
                    dataManager.persist(call);
                    dataManager.close();
                }
            }

        }

    }

    private void processAnalysis(int repositoryId, Map<String, Sequence> sequencesMap) {
        dataManager.open();
        Repository repository2 = dataManager.find(Repository.class, repositoryId);
        //        final List<Unit> units2 = dataManager.getResultList(Unit.class, "Unit.findByRepository", repository2);
        SequencesInt sequencesInt = new SequencesInt(sequencesMap, repository2.getUnits());
        MethodCallsManager concernCallsManager = new MethodCallsManager(sequencesInt);
        List<MatchesData> matchesDataList = getSequenceMatches(concernCallsManager);
        dataManager.close();
        IProgressUpdate update3 = ProgressUpdate.start("Saving matches", matchesDataList.size());
        for (MatchesData matchesData : matchesDataList) {
            update3.beginIndex(matchesData);
            dataManager.open();
            Repository repository = dataManager.getSingleResult(Repository.class, "Repository.findById", repositoryId);
            List<Method> methods = getAllMethods(repository);
            List<CloneInfo> cloneInfos = MatchesData.merge(matchesDataList);
            saveClones(repository, methods, cloneInfos);
            dataManager.close();
        }
    }

    private List<MatchesData> getSequenceMatches(MethodCallsManager concernCallsManager) {
        List<MatchesData> sequenceMatches = concernCallsManager.getSequenceMatches();
        return sequenceMatches;
    }

    private void deleteAllAnalysis() {
        dataManager.open();
        dataManager.removeAll(Analyse.class);
        dataManager.close();
    }

    private void analyseRepositories(Map<String, Sequence> sequencesMap, List<Repository> repositories) {
        for (Repository repository : repositories) {
            processAnalysis(repository.getId(), sequencesMap);
        }
    }

    private Map<String, Sequence> getSequences(IDataManager dataManager) {
        dataManager.open();
        List<Sequence> sequences = (List<Sequence>) dataManager.findAll(Sequence.class);
        Map<String, Sequence> sequencesMap = new HashMap<>();
        for (Sequence sequence : sequences) {
            sequencesMap.put(sequence.getName(), sequence);
        }
        dataManager.close();
        return sequencesMap;
    }

    private void commit(Object entity) {
        IProgressUpdate update = ProgressUpdate.start("Persisting entity", 1);
        update.beginIndex(entity);
        dataManager.open();
        dataManager.persist(entity);
        dataManager.close();
        update.endIndex(entity);
    }

    public void run(String folder, boolean searchZips, boolean deleteAllAnalysis) throws IOException, ParseException {
        if (deleteAllAnalysis) {
            deleteAllAnalysis();
        }
        List<File> files = searchZips ? Helper.getFilesRecursevely(folder, ".zip") : Arrays.asList(new File(folder));
        IProgressReport report = new ProgressReport();
        IProgressUpdate update = ProgressUpdate.start(report, "Run over " + folder, 2);
        Map<String, Sequence> sequencesMap = getSequences(dataManager);
        update.beginIndex("Creating repositories");
        List<Repository> repositories = createRepositories(files, sequencesMap);
        for (Repository repository : repositories) {
            commitRepository(repository);
        }
        update.beginIndex("Analysing repositories");
        analyseRepositories(sequencesMap, repositories);
        update.endIndex();
    }
}
