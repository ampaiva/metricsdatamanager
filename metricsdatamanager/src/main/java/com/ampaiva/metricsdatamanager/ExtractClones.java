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
import com.ampaiva.metricsdatamanager.model.Call;
import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.model.Sequence;
import com.ampaiva.metricsdatamanager.model.Unit;
import com.ampaiva.metricsdatamanager.util.CloneInfo;
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
        List<Method> methods = getAllMethods(repository);
        List<CloneInfo> cloneInfos = MatchesData.merge(matchesDataList);
        saveClones(repository, methods, cloneInfos);
    }

    protected void saveClones(Repository repository, List<Method> methods, List<CloneInfo> cloneInfos) {
        IProgressUpdate update3 = ProgressUpdate.start("Saving matches", cloneInfos.size());
        for (CloneInfo cloneInfo : cloneInfos) {
            update3.beginIndex(cloneInfo);
            Analyse analyse = new Analyse();
            analyse.setRepositoryBean(repository);
            repository.getAnalysis().add(analyse);
            analyse.setClones(new ArrayList<Clone>());
            for (int i = 0; i < cloneInfo.methods.size(); i++) {
                final Method method = methods.get(cloneInfo.methods.get(i));
                final List<Integer> calls = cloneInfo.calls.get(i);
                Call begin = method.getCalls().get(calls.get(0));
                int size = 1;
                for (int j = 1; j < calls.size(); j++) {
                    if (calls.get(j) == begin.getPosition() + size) {
                        size++;
                    } else {
                        Clone clone = new Clone();
                        clone.setBegin(begin);
                        clone.setSize(size);
                        analyse.getClones().add(clone);

                        begin = method.getCalls().get(calls.get(j));
                        size = 1;
                    }
                }
                Clone clone = new Clone();
                clone.setBegin(begin);
                clone.setSize(size);
                analyse.getClones().add(clone);
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
