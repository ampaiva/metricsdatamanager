package com.ampaiva.metricsdatamanager;

import japa.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ampaiva.hlo.util.Helper;
import com.ampaiva.hlo.util.view.IProgressReport;
import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressReport;
import com.ampaiva.hlo.util.view.ProgressUpdate;
import com.ampaiva.metricsdatamanager.config.IConcernCallsConfig;
import com.ampaiva.metricsdatamanager.controller.ConcernCallsManager;
import com.ampaiva.metricsdatamanager.controller.DataManager;
import com.ampaiva.metricsdatamanager.controller.IDataManager;
import com.ampaiva.metricsdatamanager.model.Analyse;
import com.ampaiva.metricsdatamanager.model.Call;
import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.model.CloneCall;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.util.MatchesData;

public class PersistDuplications {

    final int MIN_SEQ;
    final int MAX_SEQ;
    final IDataManager dataManager = new DataManager("metricsdatamanager");

    private PersistDuplications(int mIN_SEQ, int mAX_SEQ) {
        MIN_SEQ = mIN_SEQ;
        MAX_SEQ = mAX_SEQ;
    }

    private List<Repository> createRepositories(List<File> files, List<Sequence> sequences)
            throws FileNotFoundException, IOException, ParseException {
        IProgressUpdate update = ProgressUpdate.start("Processing file", files.size());
        List<Repository> repositories = new ArrayList<Repository>();
        for (File file : files) {
            update.beginIndex(file);
            Repository repository = processFile(file, sequences);
            repositories.add(repository);
        }
        return repositories;
    }

    private Repository processFile(File file, List<Sequence> sequences) throws FileNotFoundException, IOException,
            ParseException {
        String location = file.getName();
        Repository repository = getRepositoryByLocation(location);
        if (repository == null) {
            ConcernCallsManager concernCallsManager = new ConcernCallsManager();
            repository = concernCallsManager.createRepository(file, sequences);
            commitRepository(repository);
        }
        return repository;
    }

    private void commitRepository(Repository repository) {
        List<Method> methods = repository.getMethods();
        repository.setMethods(Collections.<Method> emptyList());
        commit(repository);
        IProgressUpdate update = ProgressUpdate.start("Persisting methods", methods.size());
        for (Method method : methods) {
            update.beginIndex(method);
            List<Call> calls = method.getCalls();
            method.setCalls(Collections.<Call> emptyList());
            dataManager.open();
            method.setRepositoryBean(dataManager.getSingleResult(Repository.class, "Repository.findById",
                    repository.getId()));
            dataManager.persist(method);
            dataManager.close();
            for (Call call : calls) {
                dataManager.open();
                call.setMethodBean(dataManager.getSingleResult(Method.class, "Method.findById", method.getId()));
                Sequence sequence = dataManager.getSingleResult(Sequence.class, "Sequence.findByName", call
                        .getSequence().getName());
                if (sequence != null) {
                    call.setSequence(sequence);
                }
                dataManager.persist(call);
                dataManager.close();
            }
        }

    }

    private Repository getRepositoryByLocation(String location) {
        dataManager.open();
        Repository repository = dataManager.getSingleResult(Repository.class, "Repository.findByLocation", location);
        dataManager.close();
        return repository;
    }

    private Analyse getAnalysisByRepoAndConfig(Repository repository, int minSeq) {
        //TODO: create a query
        for (Analyse analyse : repository.getAnalysis()) {
            if (analyse.getMinSeq() == minSeq) {
                return analyse;
            }
        }
        return null;
    }

    private void processAnalysis(int repositoryId) {
        IProgressUpdate update = ProgressUpdate.start("Processing sequence", MAX_SEQ - MIN_SEQ + 1);
        List<Sequence> sequences = getSequences(dataManager);
        for (int minSeq = MIN_SEQ; minSeq <= MAX_SEQ; minSeq++) {
            update.beginIndex("minSeq=" + minSeq);
            dataManager.open();
            Repository repository = dataManager.getSingleResult(Repository.class, "Repository.findById", repositoryId);
            Analyse analyse = getAnalysisByRepoAndConfig(repository, minSeq);
            if (analyse != null) {
                continue;
            }
            analyse = new Analyse(minSeq);
            analyse.setClones(new ArrayList<Clone>());
            analyse.setRepositoryBean(repository);
            repository.getAnalysis().add(analyse);
            List<Method> methods = repository.getMethods();
            List<MatchesData> sequenceMatches = getSequenceMatches(sequences, methods, minSeq);
            IProgressUpdate update3 = ProgressUpdate.start("Saving matches", sequenceMatches.size());
            for (MatchesData matchesData : sequenceMatches) {
                update3.beginIndex(matchesData);
                IProgressUpdate update4 = ProgressUpdate.start("Saving groups", matchesData.groupsMatched.size());
                for (int i = 0; i < matchesData.groupsMatched.size(); i++) {
                    update4.beginIndex();
                    int groupMatched = matchesData.groupsMatched.get(i);
                    Clone clone = new Clone();
                    clone.setCopy(methods.get(matchesData.groupIndex));
                    clone.setPaste(methods.get(groupMatched));
                    clone.setAnalyseBean(analyse);
                    clone.setCalls(new ArrayList<CloneCall>());
                    analyse.getClones().add(clone);

                    List<List<Integer>> duplications = matchesData.sequencesMatches.get(i);
                    IProgressUpdate update5 = ProgressUpdate.start("Saving duplications", duplications.size());
                    for (List<Integer> duplication : duplications) {
                        update5.beginIndex(duplication);
                        CloneCall cloneCall = new CloneCall();
                        cloneCall.setCopy(clone.getCopy().getCalls().get(duplication.get(0)));
                        cloneCall.setPaste(clone.getPaste().getCalls().get(duplication.get(1)));
                        cloneCall.setClone(clone);
                        clone.getCalls().add(cloneCall);
                    }
                }
                IProgressUpdate updateCommit = ProgressUpdate.startSingle("Commiting", analyse);
                dataManager.persist(analyse);
                dataManager.close();
                updateCommit.endIndex(analyse);

                if (analyse.getClones().size() == 0) {
                    return;
                }
            }
        }
    }

    private List<MatchesData> getSequenceMatches(List<Sequence> sequences, List<Method> methods, final int minSeq) {
        ConcernCallsManager concernCallsManager = new ConcernCallsManager();
        IConcernCallsConfig config = new IConcernCallsConfig() {

            @Override
            public int getMinSeq() {
                return minSeq;
            }
        };
        List<MatchesData> sequenceMatches = concernCallsManager.getSequenceMatches(sequences, methods, config);
        return sequenceMatches;
    }

    private void run(String folder) throws IOException, ParseException {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        List<File> files = Helper.getFilesRecursevely(folder, ".zip");
        IProgressReport report = new ProgressReport(5);
        IProgressUpdate update = ProgressUpdate.start(report, "Run over " + folder, 1);
        List<Sequence> sequences = getSequences(dataManager);
        List<Repository> repositories = createRepositories(files, sequences);
        for (Repository repository : repositories) {
            processAnalysis(repository.getId());
        }

        update.endIndex();
        BasicConfigurator.resetConfiguration();
    }

    private List<Sequence> getSequences(IDataManager dataManager) {
        dataManager.open();
        List<Sequence> sequences = (List<Sequence>) dataManager.findAll(Sequence.class);
        dataManager.close();
        return sequences;
    }

    private void commit(Object entity) {
        IProgressUpdate update = ProgressUpdate.start("Persisting entity", 1);
        update.beginIndex(entity);
        dataManager.open();
        dataManager.persist(entity);
        dataManager.close();
        update.endIndex(entity);
    }

    public static void main(String[] args) throws IOException, ParseException {
        BasicConfigurator.configure();
        String folder = "/temp";
        PersistDuplications persistDuplications = new PersistDuplications(1, 100);
        persistDuplications.run(folder);
    }

}
