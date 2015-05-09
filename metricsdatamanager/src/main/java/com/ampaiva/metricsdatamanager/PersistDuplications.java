package com.ampaiva.metricsdatamanager;

import japa.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.util.Helper;
import com.ampaiva.hlo.util.view.IProgressReport;
import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressReport;
import com.ampaiva.hlo.util.view.ProgressUpdate;
import com.ampaiva.metricsdatamanager.config.IConcernCallsConfig;
import com.ampaiva.metricsdatamanager.controller.ConcernCallsManager;
import com.ampaiva.metricsdatamanager.controller.ConcernClone;
import com.ampaiva.metricsdatamanager.controller.DataManager;
import com.ampaiva.metricsdatamanager.controller.IDataManager;
import com.ampaiva.metricsdatamanager.model.Analyse;
import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.model.CloneCall;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.util.HashArray;
import com.ampaiva.metricsdatamanager.util.IHashArray;
import com.ampaiva.metricsdatamanager.util.MatchesData;
import com.ampaiva.metricsdatamanager.util.ZipStreamUtil;

public class PersistDuplications {

    final int MIN_SEQ;
    final int MAX_SEQ;
    final int MAX_DISTANCE;

    public PersistDuplications(int mIN_SEQ, int mAX_SEQ, int mAX_DISTANCE) {
        MIN_SEQ = mIN_SEQ;
        MAX_SEQ = mAX_SEQ;
        MAX_DISTANCE = mAX_DISTANCE;
    }

    private List<Repository> processFiles(List<File> files, List<Sequence> sequences, IHashArray hashArray,
            List<Clone> clones) throws FileNotFoundException, IOException, ParseException {
        IProgressUpdate update = ProgressUpdate.start("Processing file", files.size());
        List<Repository> repositories = new ArrayList<Repository>();
        for (File file : files) {
            update.beginIndex(file);
            Repository repository = processFile(file, sequences, hashArray, clones);
            repositories.add(repository);
        }
        return repositories;
    }

    private Repository processFile(File file, List<Sequence> sequences, IHashArray hashArray, List<Clone> clones2)
            throws FileNotFoundException, IOException, ParseException {
        Repository repository = new Repository();
        repository.setLocation(file.getName());
        List<Method> methods = getMethodCodes(sequences, hashArray, file);
        for (Method method : methods) {
            method.setRepositoryBean(repository);
        }
        repository.setMethods(methods);
        repository.setAnalysis(new ArrayList<Analyse>());

        processAnalysis(hashArray, repository, methods);
        return repository;
    }

    private void processAnalysis(IHashArray hashArray, Repository repository, List<Method> methods) {
        IProgressUpdate update = ProgressUpdate.start("Processing sequence", MAX_SEQ - MIN_SEQ + 1);
        for (int minSeq = MIN_SEQ; minSeq <= MAX_SEQ; minSeq++) {
            update.beginIndex();
            IProgressUpdate update2 = ProgressUpdate.start("Processing distance", MAX_DISTANCE);
            for (int maxDist = 0; maxDist < MAX_DISTANCE; maxDist++) {
                update2.beginIndex();
                Analyse analyse = new Analyse(minSeq, maxDist);
                analyse.setRepositoryBean(repository);
                analyse.setClones(new ArrayList<Clone>());
                repository.addAnalyse(analyse);
                List<MatchesData> sequenceMatches = getSequenceMatches(hashArray, methods, minSeq, maxDist);
                for (MatchesData matchesData : sequenceMatches) {
                    for (int i = 0; i < matchesData.groupsMatched.size(); i++) {
                        int groupMatched = matchesData.groupsMatched.get(i);
                        Clone clone = new Clone();
                        clone.setCopy(methods.get(matchesData.groupIndex));
                        clone.setPaste(methods.get(groupMatched));
                        clone.setCalls(new ArrayList<CloneCall>());
                        clone.setAnalyseBean(analyse);
                        analyse.getClones().add(clone);

                        List<List<Integer>> duplications = matchesData.sequencesMatches.get(i);
                        for (List<Integer> duplication : duplications) {
                            CloneCall cloneCall = new CloneCall();
                            cloneCall.setCopy(clone.getCopy().getCalls().get(duplication.get(0)));
                            cloneCall.setPaste(clone.getCopy().getCalls().get(duplication.get(1)));
                            cloneCall.setClone(clone);
                            clone.getCalls().add(cloneCall);
                        }
                    }
                }
            }
        }
    }

    private List<MatchesData> getSequenceMatches(IHashArray hashArray, List<Method> methodCodes, final int minSeq,
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
        List<MatchesData> sequenceMatches = concernCallsManager.getSequenceMatches(hashArray, methodCodes, config);
        return sequenceMatches;
    }

    public List<ConcernClone> getConcernClones(List<MatchesData> sequenceMatches, List<Method> methodCodes) {
        ConcernCallsManager concernCallsManager = new ConcernCallsManager();
        List<ConcernClone> duplications = concernCallsManager.getConcernClones(sequenceMatches, methodCodes);
        return duplications;
    }

    private List<Method> getMethodCodes(List<Sequence> sequences, IHashArray hashArray, File file)
            throws FileNotFoundException, IOException, ParseException {
        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(file.toString(), Helper.convertFile2InputStream(new File(file
                .getAbsolutePath())));
        List<ICodeSource> codeSources = Arrays.asList((ICodeSource) zipStreamUtil);
        ConcernCallsManager concernCallsManager = new ConcernCallsManager();
        List<Method> methodCodes = concernCallsManager.getMethodCodes(sequences, hashArray, codeSources);
        ConcernCallsManager.getHashArray(hashArray, methodCodes);
        return methodCodes;
    }

    public void run(String folder) throws IOException, ParseException {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        List<File> files = Helper.getFilesRecursevely(folder, ".zip");
        IProgressReport report = new ProgressReport();
        IProgressUpdate update = ProgressUpdate.start(report, "Run over " + folder, 1);
        final IDataManager dataManager = new DataManager("metricsdatamanager");
        List<Sequence> sequences = getSequences(dataManager);
        int sequencesSize = sequences.size();
        IHashArray hashArray = new HashArray();
        for (int i = 0; i < sequences.size(); i++) {
            hashArray.put(sequences.get(i).getName());
        }
        List<Clone> clones = new ArrayList<Clone>();
        List<Repository> repositories = processFiles(files, sequences, hashArray, clones);
        for (int i = sequencesSize; i < hashArray.size(); i++) {
            Sequence sequence = new Sequence();
            sequence.setName(hashArray.getByIndex(i));
            sequences.add(sequence);
        }
        persist(dataManager, sequences.subList(sequencesSize, sequences.size()), repositories);
        update.endIndex();
        BasicConfigurator.resetConfiguration();
    }

    private List<Sequence> getSequences(IDataManager dataManager) {
        dataManager.open();
        List<Sequence> sequences = (List<Sequence>) dataManager.findAll(Sequence.class);
        dataManager.close();
        return sequences;
    }

    private void deleteAllData(IDataManager dataManager) {
        dataManager.open();
        dataManager.removeAll(Repository.class);
        dataManager.close();
    }

    private void persist(IDataManager dataManager, List<Sequence> sequences, List<Repository> repositories) {
        IProgressUpdate update = ProgressUpdate.start("Persisting data", 4);
        update.beginIndex("Delete");
        deleteAllData(dataManager);
        dataManager.open();
        update.beginIndex("Persisting sequences", sequences.size());
        for (Sequence sequence : sequences) {
            dataManager.persist(sequence);
        }
        update.beginIndex("Persisting repositories", repositories.size());
        for (Repository repository : repositories) {
            dataManager.persist(repository);
        }
        update.beginIndex("Commit");
        dataManager.close();
        update.endIndex("Commit");
    }

    public static void main(String[] args) throws IOException, ParseException {
        String folder = "/temp";
        PersistDuplications persistDuplications = new PersistDuplications(4, 10, 2);
        persistDuplications.run(folder);
    }

}
