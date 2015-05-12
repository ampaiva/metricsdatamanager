package com.ampaiva.metricsdatamanager;

import japa.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.model.CloneCall;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.util.MatchesData;

public class PersistDuplications {

    final int MIN_SEQ;
    final int MAX_SEQ;
    final int MAX_DISTANCE;
    final IDataManager dataManager = new DataManager("metricsdatamanager");

    private PersistDuplications(int mIN_SEQ, int mAX_SEQ, int mAX_DISTANCE) {
        MIN_SEQ = mIN_SEQ;
        MAX_SEQ = mAX_SEQ;
        MAX_DISTANCE = mAX_DISTANCE;
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
            commit(repository);
        }
        return repository;
    }

    private Repository getRepositoryByLocation(String location) {
        dataManager.open();
        Repository repository = dataManager.getSingleResult(Repository.class, "Repository.findByLocation", location);
        dataManager.close();
        return repository;
    }

    private Analyse getAnalysisByRepoAndConfig(Repository repository, int minSeq, int maxDist) {
        //TODO: create a query
        for (Analyse analyse : repository.getAnalysis()) {
            if (analyse.getMinSeq() == minSeq && analyse.getMaxDist() == maxDist) {
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
            IProgressUpdate update2 = ProgressUpdate.start("Processing distance", MAX_DISTANCE);
            for (int maxDist = 0; maxDist < MAX_DISTANCE; maxDist++) {
                update2.beginIndex();
                dataManager.open();
                Repository repository = dataManager.getSingleResult(Repository.class, "Repository.findById",
                        repositoryId);
                Analyse analyse = getAnalysisByRepoAndConfig(repository, minSeq, maxDist);
                if (analyse != null) {
                    continue;
                }
                analyse = new Analyse(minSeq, maxDist);
                analyse.setClones(new ArrayList<Clone>());
                analyse.setRepositoryBean(repository);
                repository.getAnalysis().add(analyse);
                List<Method> methods = repository.getMethods();
                List<MatchesData> sequenceMatches = getSequenceMatches(sequences, methods, minSeq, maxDist);
                for (MatchesData matchesData : sequenceMatches) {
                    for (int i = 0; i < matchesData.groupsMatched.size(); i++) {
                        int groupMatched = matchesData.groupsMatched.get(i);
                        Clone clone = new Clone();
                        clone.setCopy(methods.get(matchesData.groupIndex));
                        clone.setPaste(methods.get(groupMatched));
                        clone.setAnalyseBean(analyse);
                        clone.setCalls(new ArrayList<CloneCall>());
                        analyse.getClones().add(clone);

                        List<List<Integer>> duplications = matchesData.sequencesMatches.get(i);
                        for (List<Integer> duplication : duplications) {
                            CloneCall cloneCall = new CloneCall();
                            cloneCall.setCopy(clone.getCopy().getCalls().get(duplication.get(0)));
                            cloneCall.setPaste(clone.getPaste().getCalls().get(duplication.get(1)));
                            cloneCall.setClone(clone);
                            clone.getCalls().add(cloneCall);
                        }
                    }
                }
                dataManager.persist(analyse);
                dataManager.close();
            }
        }
    }

    private List<MatchesData> getSequenceMatches(List<Sequence> sequences, List<Method> methods, final int minSeq,
            final int maxDistance) {
        ConcernCallsManager concernCallsManager = new ConcernCallsManager();
        IConcernCallsConfig config = new IConcernCallsConfig() {

            @Override
            public int getMinSeq() {
                return minSeq;
            }

            @Override
            public int getMaxDistance() {
                return maxDistance;
            }
        };
        List<MatchesData> sequenceMatches = concernCallsManager.getSequenceMatches(sequences, methods, config);
        return sequenceMatches;
    }

    private void run(String folder) throws IOException, ParseException {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        List<File> files = Helper.getFilesRecursevely(folder, ".zip");
        IProgressReport report = new ProgressReport();
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
        PersistDuplications persistDuplications = new PersistDuplications(375, 375, 1);
        persistDuplications.run(folder);
    }

}
