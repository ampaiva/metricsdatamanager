package com.ampaiva.metricsdatamanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.util.Helper;
import com.ampaiva.hlo.util.view.IProgressReport;
import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressReport;
import com.ampaiva.hlo.util.view.ProgressUpdate;
import com.ampaiva.metricsdatamanager.controller.ConcernCallsManager;
import com.ampaiva.metricsdatamanager.model.Analyse;
import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.model.Unit;
import com.ampaiva.metricsdatamanager.util.Duplications;
import com.ampaiva.metricsdatamanager.util.Duplications.DuplicationInfo;
import com.ampaiva.metricsdatamanager.util.FolderUtil;
import com.ampaiva.metricsdatamanager.util.MatchesData;
import com.ampaiva.metricsdatamanager.util.SequencesInt;
import com.ampaiva.metricsdatamanager.util.ZipStreamUtil;
import com.github.javaparser.ParseException;

public class ExtractClones {

    final int minSeq;

    public ExtractClones(int minSeq) {
        this.minSeq = minSeq;
    }

    private List<Repository> createRepositories(List<File> files, Map<String, Sequence> sequencesMap)
            throws FileNotFoundException, IOException, ParseException {
        IProgressUpdate update = ProgressUpdate.start("Processing file", files.size());
        List<Repository> repositories = new ArrayList<Repository>();
        for (File file : files) {
            update.beginIndex(file);
            Repository repository = processFile(file, sequencesMap);
            repositories.add(repository);
        }
        return repositories;
    }

    private Repository processFile(File file, Map<String, Sequence> sequencesMap)
            throws FileNotFoundException, IOException, ParseException {
        String location = file.isDirectory() ? file.getAbsolutePath() : file.getName();
        ConcernCallsManager concernCallsManager = new ConcernCallsManager();
        List<ICodeSource> codeSources = new ArrayList<ICodeSource>();
        if (file.isDirectory()) {
            FolderUtil folderUtil = new FolderUtil(location);
            codeSources.add(folderUtil);
        } else {
            ZipStreamUtil zipStreamUtil = new ZipStreamUtil(file.toString(),
                    Helper.convertFile2InputStream(new File(file.getAbsolutePath())));
            codeSources.add(zipStreamUtil);
        }
        Repository repository = concernCallsManager.createRepository(codeSources, location, sequencesMap);

        return repository;
    }

    private Analyse getAnalysisByRepoAndConfig(Repository repository, int size) {
        for (Analyse analyse : repository.getAnalysis()) {
            if (analyse.getMinSeq() == size) {
                return analyse;
            }
        }
        return null;
    }

    private void processAnalysis(Repository repository, Map<String, Sequence> sequencesMap) {
        SequencesInt sequencesInt = new SequencesInt(sequencesMap, repository.getUnits());
        ConcernCallsManager concernCallsManager = new ConcernCallsManager(sequencesInt);
        List<MatchesData> matchesDataList = getSequenceMatches(concernCallsManager);
        IProgressUpdate update3 = ProgressUpdate.start("Saving matches", matchesDataList.size());
        for (MatchesData matchesData : matchesDataList) {
            update3.beginIndex(matchesData);
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
                Duplications duplications = new Duplications(matchesData.sequencesMatches.get(i));
                DuplicationInfo duplicationInfo = duplications.next();
                while (duplicationInfo != null) {
                    if (duplicationInfo.count >= minSeq) {
                        saveAnalysis(repository, method0, method1, duplicationInfo.count, duplicationInfo.position0,
                                duplicationInfo.position1);
                    }
                    duplicationInfo = duplications.next();
                }
                update4.endIndex();
            }
        }
    }

    public void saveAnalysis(Repository repository, final Method method0, final Method method1, int count,
            int position0, int position1) {
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
    }

    private List<MatchesData> getSequenceMatches(ConcernCallsManager concernCallsManager) {
        List<MatchesData> sequenceMatches = concernCallsManager.getSequenceMatches();
        return sequenceMatches;
    }

    private void analyseRepositories(Map<String, Sequence> sequencesMap, List<Repository> repositories) {
        for (Repository repository : repositories) {
            processAnalysis(repository, sequencesMap);
        }
    }

    public List<Repository> run(String folder, boolean searchZips) throws IOException, ParseException {
        List<File> files = searchZips ? Helper.getFilesRecursevely(folder, ".zip") : Arrays.asList(new File(folder));
        IProgressReport report = new ProgressReport();
        IProgressUpdate update = ProgressUpdate.start(report, "Run over " + folder, 2);
        Map<String, Sequence> sequencesMap = new HashMap<>();
        update.beginIndex("Creating repositories");
        List<Repository> repositories = createRepositories(files, sequencesMap);
        update.beginIndex("Analysing repositories");
        analyseRepositories(sequencesMap, repositories);
        update.endIndex();

        return repositories;
    }
}
