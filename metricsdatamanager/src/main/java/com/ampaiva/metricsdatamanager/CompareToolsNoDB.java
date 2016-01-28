package com.ampaiva.metricsdatamanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ampaiva.metricsdatamanager.model.Analyse;
import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd;
import com.ampaiva.metricsdatamanager.tools.pmd.PmdClone;
import com.ampaiva.metricsdatamanager.tools.pmd.PmdOccurrence;
import com.ampaiva.metricsdatamanager.util.Conventions;

public class CompareToolsNoDB {
    private static final String COMMA = ",";
    private static final String EOL = "\r\n";

    private boolean hasPmdClone(List<Analyse> mcSheepClones, PmdClone pmdClone) {
        for (Analyse mcSheepClone : mcSheepClones) {
            List<PmdOccurrence> snippets = new ArrayList<>();
            snippets.addAll(pmdClone.ocurrencies);
            for (PmdOccurrence snippet : pmdClone.ocurrencies) {
                CloneSnippet cloneSnippet = CloneGroup.getCloneSnippet(mcSheepClone.getRepositoryBean().getLocation(),
                        snippet);
                for (Clone mcSheepOcurrency : mcSheepClone.getClones()) {
                    final String file1 = Conventions.fileNameInRepository(
                            mcSheepClone.getRepositoryBean().getLocation(),
                            mcSheepOcurrency.getBegin().getMethodBean().getUnitBean().getName());
                    if (file1.equals(cloneSnippet.name) && //
                            mcSheepOcurrency.getBegin().getBeglin() <= cloneSnippet.endlin
                            && mcSheepOcurrency.getBegin().getMethodBean().getCalls()
                                    .get(mcSheepOcurrency.getBegin().getPosition() + mcSheepOcurrency.getSize() - 1)
                                    .getEndlin() >= cloneSnippet.beglin) {
                        snippets.remove(snippet);
                        if (snippets.isEmpty()) {
                            return true;
                        }
                        break;
                    }
                }
            }
        }
        return false;
    }

    public List<CloneGroup> comparePMDxMcSheep(Repository repository, String pmdResult) throws IOException {
        List<PmdClone> pmdClones = Pmd.parse(repository.getLocation(), pmdResult);
        List<PmdClone> pmdFound = new ArrayList<>();
        List<PmdClone> pmdNotFound = new ArrayList<>();
        int found = 0, notFound = 0;
        System.out.println();
        System.out.println("Results found by PMD and not found by McSheep");
        System.out.println("=============================================");
        List<Analyse> mcSheepClones = repository.getAnalysis();
        for (PmdClone cloneGroup : pmdClones) {
            if (hasPmdClone(mcSheepClones, cloneGroup)) {
                pmdFound.add(cloneGroup);
                System.out.println("Clone found by PMD: " + cloneGroup);
                found++;
            } else {
                pmdNotFound.add(cloneGroup);
                System.err.println("Clone not found by PMD: " + cloneGroup);
                notFound++;
            }
        }
        System.err.println("Not found: " + notFound);
        System.out.println("Found: " + found);
        return getCloneGroups(pmdFound, pmdNotFound);
    }

    private void removeAll(List<Clone> snippets, Method method) {
        List<Clone> toBeRemoved = new ArrayList<>();
        for (Clone clone : snippets) {
            if (clone.getBegin().getMethodBean().equals(method)) {
                toBeRemoved.add(clone);
            }
        }
        for (Clone clone : toBeRemoved) {
            snippets.remove(clone);
        }
    }

    private boolean hasMcSheepClone(List<PmdClone> pmdClones, Analyse mcSheepClone) {
        for (PmdClone pmdClone : pmdClones) {
            List<Clone> snippets = new ArrayList<>();
            snippets.addAll(mcSheepClone.getClones());
            for (Clone snippet : mcSheepClone.getClones()) {
                CloneSnippet cloneSnippet = CloneGroup.getCloneSnippet(snippet);
                for (PmdOccurrence pmdOcurrency : pmdClone.ocurrencies) {
                    final String file1 = Conventions
                            .fileNameInRepository(mcSheepClone.getRepositoryBean().getLocation(), pmdOcurrency.file);
                    if (file1.equals(cloneSnippet.name) && //
                            pmdOcurrency.line <= cloneSnippet.endlin
                            && pmdOcurrency.line + pmdClone.lines - 1 >= cloneSnippet.beglin) {
                        removeAll(snippets, snippet.getBegin().getMethodBean());
                        if (snippets.isEmpty()) {
                            return true;
                        }
                        break;
                    }
                }
            }
        }
        return false;
    }

    public List<CloneGroup> compareMcSheepxPMD(Repository repository, String pmdResult) throws IOException {
        List<PmdClone> pmdClones = Pmd.parse(repository.getLocation(), pmdResult);
        List<Analyse> mcsheepFound = new ArrayList<>();
        List<Analyse> mcsheepNotFound = new ArrayList<>();
        int found = 0, notFound = 0;
        System.out.println();
        System.out.println("Results found by McSheep and not found by PMD");
        System.out.println("=============================================");
        for (Analyse cloneGroup : repository.getAnalysis()) {
            if (hasMcSheepClone(pmdClones, cloneGroup)) {
                mcsheepFound.add(cloneGroup);
                System.out.println("Clone found by PMD: " + cloneGroup);
                found++;
            } else {
                mcsheepNotFound.add(cloneGroup);
                System.err.println("Clone not found by PMD: " + cloneGroup);
                notFound++;
            }
        }

        System.err.println("Not found: " + notFound);
        System.out.println("Found: " + found);
        return getCloneGroups(mcsheepFound, mcsheepNotFound);
    }

    private void writeClonePair(FileWriter fileWriter, CloneGroup clone) throws IOException {
        fileWriter.write((clone.found ? "+" : "-") + COMMA + clone.toId() + EOL);
    }

    private <T> List<CloneGroup> getCloneGroups(List<T> clonesFound, List<T> clonesNotFound) {
        List<CloneGroup> clones = new ArrayList<>();
        for (T clone : clonesFound) {
            clones.addAll(CloneGroup.getCloneGroups(clone, true));
        }
        for (T clone : clonesNotFound) {
            clones.addAll(CloneGroup.getCloneGroups(clone, false));
        }
        return FilterClonePair.getClonePairs(clones);
    }

    private <T> void writeClones(FileWriter fileWriter, List<CloneGroup> result) throws IOException {
        for (CloneGroup cloneData : result) {
            writeClonePair(fileWriter, cloneData);
        }
    }

    public void saveClones(String folderName, String fileName, List<CloneGroup> clones) throws IOException {
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File(folderName + File.separator + fileName));
            writeClones(fileWriter, clones);
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }
}
