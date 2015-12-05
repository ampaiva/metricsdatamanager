package com.ampaiva.metricsdatamanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.util.Helper;
import com.ampaiva.hlo.util.view.IProgressReport;
import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressReport;
import com.ampaiva.hlo.util.view.ProgressUpdate;
import com.ampaiva.metricsdatamanager.controller.ConcernCallsManager;
import com.ampaiva.metricsdatamanager.controller.IDataManager;
import com.ampaiva.metricsdatamanager.model.Analyse;
import com.ampaiva.metricsdatamanager.model.Call;
import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.model.Unit;
import com.ampaiva.metricsdatamanager.util.FolderUtil;
import com.ampaiva.metricsdatamanager.util.MatchesData;
import com.ampaiva.metricsdatamanager.util.SequencesInt;
import com.ampaiva.metricsdatamanager.util.ZipStreamUtil;
import com.github.javaparser.ParseException;

public class PersistDuplications {

    final int MIN_SEQ;
    final int MAX_SEQ;
    final IDataManager dataManager;

    public PersistDuplications(IDataManager dataManager, int mIN_SEQ, int mAX_SEQ) {
        this.dataManager = dataManager;
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

    private Repository processFile(File file, List<Sequence> sequences)
            throws FileNotFoundException, IOException, ParseException {
        String location = file.getName();
        Repository repository = getRepositoryByLocation(location);
        if (repository == null) {
            ConcernCallsManager concernCallsManager = new ConcernCallsManager();
            List<ICodeSource> codeSources = new ArrayList<ICodeSource>();
            if (file.isDirectory()) {
                FolderUtil folderUtil = new FolderUtil(file.getAbsolutePath());
                codeSources.add(folderUtil);
            } else {
                ZipStreamUtil zipStreamUtil = new ZipStreamUtil(file.toString(),
                        Helper.convertFile2InputStream(new File(file.getAbsolutePath())));
                codeSources.add(zipStreamUtil);
            }
            repository = concernCallsManager.createRepository(codeSources, file.getName(), sequences);
            commitRepository(repository);
        }
        return repository;
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

    private void processAnalysis(int repositoryId, List<Sequence> sequences) {
        dataManager.open();
        Repository repository2 = dataManager.getSingleResult(Repository.class, "Repository.findById", repositoryId);
        dataManager.close();
        SequencesInt sequencesInt = new SequencesInt(sequences, repository2.getUnits());
        ConcernCallsManager concernCallsManager = new ConcernCallsManager(sequencesInt);
        List<MatchesData> matchesDataList = getSequenceMatches(concernCallsManager);
        IProgressUpdate update3 = ProgressUpdate.start("Saving matches", matchesDataList.size());
        for (MatchesData matchesData : matchesDataList) {
            update3.beginIndex(matchesData);
            dataManager.open();
            Repository repository = dataManager.getSingleResult(Repository.class, "Repository.findById", repositoryId);
            List<Unit> units = repository.getUnits();
            List<Method> methods = new ArrayList<>();
            for (Unit unit : units) {
                methods.addAll(unit.getMethods());
            }
            IProgressUpdate update4 = ProgressUpdate.start("Saving clones", matchesData.groupsMatched.size());
            final Method method0 = methods.get(matchesData.groupIndex);
            for (int i = 0; i < matchesData.groupsMatched.size(); i++) {
                update4.beginIndex();
                final Method method1 = methods.get(matchesData.groupsMatched.get(i));
                List<List<Integer>> duplications = matchesData.sequencesMatches.get(i);
                int count = 1;
                int position0 = 0;
                int position1 = 0;
                for (int j = 1; j < duplications.size(); j++) {
                    if (duplications.get(j).get(0) == (duplications.get(j - 1).get(0) + 1)
                            && duplications.get(j).get(1) == (duplications.get(j - 1).get(1) + 1)) {
                        count++;
                    } else {
                        Analyse analyse = getAnalysisByRepoAndConfig(repository, count);
                        if (analyse == null) {
                            analyse = new Analyse(count);
                            analyse.setClones(new ArrayList<Clone>());
                            analyse.setRepositoryBean(repository);
                            repository.getAnalysis().add(analyse);
                        }
                        Clone clone = new Clone();
                        clone.setCopy(method0.getCalls().get(position0));
                        clone.setPaste(method1.getCalls().get(position1));
                        clone.setAnalyseBean(analyse);
                        analyse.getClones().add(clone);
                        dataManager.persist(analyse);

                        count = 1;
                        position0 = duplications.get(j).get(0);
                        position1 = duplications.get(j).get(1);
                    }
                }
                Analyse analyse = getAnalysisByRepoAndConfig(repository, count);
                if (analyse == null) {
                    analyse = new Analyse(count);
                    analyse.setClones(new ArrayList<Clone>());
                    analyse.setRepositoryBean(repository);
                    repository.getAnalysis().add(analyse);
                }
                Clone clone = new Clone();
                clone.setCopy(method0.getCalls().get(position0));
                clone.setPaste(method1.getCalls().get(position1));
                clone.setAnalyseBean(analyse);
                analyse.getClones().add(clone);
                dataManager.persist(analyse);
                update4.endIndex();
            }
            dataManager.close();
        }
    }

    private List<MatchesData> getSequenceMatches(ConcernCallsManager concernCallsManager) {
        List<MatchesData> sequenceMatches = concernCallsManager.getSequenceMatches();
        return sequenceMatches;
    }

    private void deleteAllAnalysis() {
        dataManager.open();
        dataManager.removeAll(Analyse.class);
        dataManager.close();
    }

    private void analyseRepositories(List<Sequence> sequences, List<Repository> repositories) {
        for (Repository repository : repositories) {
            processAnalysis(repository.getId(), sequences);
        }
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

    public void run(String folder, boolean searchZips, boolean deleteAllAnalysis) throws IOException, ParseException {
        if (deleteAllAnalysis) {
            deleteAllAnalysis();
            return;
        }
        List<File> files = searchZips ? Helper.getFilesRecursevely(folder, ".zip") : Arrays.asList(new File(folder));
        IProgressReport report = new ProgressReport();
        IProgressUpdate update = ProgressUpdate.start(report, "Run over " + folder, 2);
        List<Sequence> sequences = getSequences(dataManager);
        update.beginIndex("Creating repositories");
        List<Repository> repositories = createRepositories(files, sequences);
        update.beginIndex("Analysing repositories");
        analyseRepositories(sequences, repositories);
        update.endIndex();
    }
}
