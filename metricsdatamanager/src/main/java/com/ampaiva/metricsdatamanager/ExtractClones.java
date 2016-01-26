package com.ampaiva.metricsdatamanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.ampaiva.metricsdatamanager.util.DuplicationInfo;
import com.ampaiva.metricsdatamanager.util.Duplications;
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

    protected List<Repository> createRepositories(List<File> files, Map<String, Sequence> sequencesMap)
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

    protected List<Method> getAllMethods(Repository repository) {
        List<Method> methods = new ArrayList<>();
        for (Unit unit : repository.getUnits()) {
            methods.addAll(unit.getMethods());
        }
        return methods;
    }

    private void processAnalysis(Repository repository, Map<String, Sequence> sequencesMap) {
        SequencesInt sequencesInt = new SequencesInt(sequencesMap, repository.getUnits());
        ConcernCallsManager concernCallsManager = new ConcernCallsManager(sequencesInt);
        List<MatchesData> matchesDataList = concernCallsManager.getSequenceMatches();
        IProgressUpdate update3 = ProgressUpdate.start("Saving matches", matchesDataList.size());
        List<Method> methods = getAllMethods(repository);
        Map<Clone, Analyse> hash = new HashMap<>();
        for (MatchesData matchesData : matchesDataList) {
            update3.beginIndex(matchesData);
            final Method method0 = methods.get(matchesData.methodIndex);
            for (int i = 0; i < matchesData.methodsMatched.size(); i++) {
                final Method method1 = methods.get(matchesData.methodsMatched.get(i));
                List<List<Integer>> groupMatched = matchesData.callsMatched.get(i);
                createAnalysis(hash, method0, method1, groupMatched);
            }
        }
        assignClones(repository, hash);
    }

    protected void assignClones(Repository repository, Map<Clone, Analyse> hash) {
        for (Entry<Clone, Analyse> entry : hash.entrySet()) {
            Analyse analyse = entry.getValue();
            if (countSize(analyse) >= minSeq) {
                analyse.setRepositoryBean(repository);
                repository.getAnalysis().add(analyse);
            }
        }
    }

    protected int countSize(Analyse analyse) {
        Clone cloneBase = analyse.getClones().get(0);
        int total = 0;
        for (Clone clone : analyse.getClones()) {
            if (clone.getBegin().getMethodBean().equals(cloneBase.getBegin().getMethodBean())) {
                total += clone.getSize();
            }
        }
        return total;
    }

    protected void createAnalysis(Map<Clone, Analyse> hash, final Method method0, final Method method1,
            List<List<Integer>> groupMatched) {
        Duplications duplications = new Duplications(groupMatched);
        DuplicationInfo duplicationInfo = duplications.next();
        while (duplicationInfo != null) {
            Clone clone0 = new Clone();
            clone0.setBegin(method0.getCalls().get(duplicationInfo.position0));
            clone0.setSize(duplicationInfo.count);

            Clone clone1 = new Clone();
            clone1.setBegin(method1.getCalls().get(duplicationInfo.position1));
            clone1.setSize(duplicationInfo.count);

            Analyse analyse0 = hash.get(clone0);
            Analyse analyse1 = hash.get(clone1);
            if (analyse0 == null && analyse1 != null) {
                analyse1.getClones().add(clone0);
                clone0.setAnalyseBean(analyse1);
                hash.put(clone0, analyse1);
            } else if (analyse0 != null && analyse1 == null) {
                analyse0.getClones().add(clone1);
                clone1.setAnalyseBean(analyse0);
                hash.put(clone1, analyse0);
            } else {
                Analyse analyse = new Analyse();
                analyse.setClones(new ArrayList<Clone>());
                analyse.getClones().add(clone0);
                analyse.getClones().add(clone1);
                clone0.setAnalyseBean(analyse);
                clone1.setAnalyseBean(analyse);
                hash.put(clone0, analyse);
                hash.put(clone1, analyse);
            }

            duplicationInfo = duplications.next();
        }
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
