package com.ampaiva.metricsdatamanager;

import java.util.ArrayList;
import java.util.List;

import com.ampaiva.metricsdatamanager.model.Analyse;
import com.ampaiva.metricsdatamanager.model.Call;
import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd.PmdClone;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd.PmdClone.PmdOcurrency;
import com.ampaiva.metricsdatamanager.util.Conventions;

public class CompareToolsNoDB {
    /*
     * Finds all McSheeps clones that are inside pmdClone
     * 
     * @return list of McSheeps clone Ids
     */
    private List<Clone> getMcSheepClones(Repository repository, String unit1, int unit1beglin, int unit1endlin,
            String unit2) {
        String unit1Convention = Conventions.fileNameInRepository(repository.getLocation(), unit1);
        String unit2Convention = Conventions.fileNameInRepository(repository.getLocation(), unit2);
        List<Clone> results = new ArrayList<>();
        for (Analyse analyse : repository.getAnalysis()) {
            for (Clone clone : analyse.getClones()) {
                String unit1Clone = clone.getCopy().getMethodBean().getUnitBean().getName();
                String unit2Clone = clone.getPaste().getMethodBean().getUnitBean().getName();
                Call copyCall = null;
                if (unit1Convention.equals(unit1Clone) && unit2Convention.equals(unit2Clone)) {
                    copyCall = clone.getCopy();
                } else if (unit2Convention.equals(unit1Clone) && unit1Convention.equals(unit2Clone)) {
                    copyCall = clone.getPaste();
                }

                if (copyCall != null) {
                    int beglinCopy = copyCall.getBeglin();
                    int endlinCopy = copyCall.getMethodBean().getCalls()
                            .get(copyCall.getPosition() + clone.getAnalyseBean().getMinSeq() - 1).getEndlin();
                    if (beglinCopy <= unit1endlin && endlinCopy >= unit1beglin) {
                        results.add(clone);
                    }
                }
            }
        }

        return results;
    }

    public void comparePMDxMcSheep(Repository repository, String pmdResult) {
        List<PmdClone> pmdClones = Pmd.parse(repository.getLocation(), pmdResult);
        int found = 0, notFound = 0;
        for (PmdClone pmdClone : pmdClones) {
            boolean hasAllOcurrencies = true;
            for (int i = 0; i < pmdClone.ocurrencies.size(); i++) {
                final PmdOcurrency pmdOcurrency1 = pmdClone.ocurrencies.get(i);
                final String file1 = pmdOcurrency1.file;
                for (int j = i + 1; j < pmdClone.ocurrencies.size(); j++) {
                    final PmdOcurrency pmdOcurrency2 = pmdClone.ocurrencies.get(j);
                    final String file2 = pmdOcurrency2.file;
                    List<Clone> mcSheepClones = getMcSheepClones(repository, file1, pmdOcurrency1.line,
                            pmdOcurrency1.line + pmdClone.lines - 1, file2);
                    if (mcSheepClones.size() == 0) {
                        hasAllOcurrencies = false;
                    }
                }
            }
            if (found + notFound == 0) {
                System.out.println();
                System.out.println("Results found by PMD and not found by McSheep");
                System.out.println("=============================================");
            }
            if (!hasAllOcurrencies) {
                System.err.println("Clone not found by McSheep: " + pmdClone);
                notFound++;
            } else {
                System.out.println("Clone found by McSheep: " + pmdClone);
                found++;
            }
        }
        System.err.println("Not found: " + notFound);
        System.out.println("Found: " + found);
    }

    public void compareMcSheepxPMD(Repository repository, String pmdResult) {
        List<PmdClone> pmdClones = Pmd.parse(repository.getLocation(), pmdResult);
        int found = 0, notFound = 0;
        for (Analyse analyse : repository.getAnalysis()) {
            for (Clone clone : analyse.getClones()) {
                String unit1 = clone.getCopy().getMethodBean().getUnitBean().getName();
                String unit2 = clone.getPaste().getMethodBean().getUnitBean().getName();
                int beglinCopy = clone.getCopy().getBeglin();
                int endlinCopy = clone.getCopy().getMethodBean().getCalls()
                        .get(clone.getCopy().getPosition() + clone.getAnalyseBean().getMinSeq() - 1).getEndlin();
                int beglinPaste = clone.getPaste().getBeglin();
                int endlinPaste = clone.getPaste().getMethodBean().getCalls()
                        .get(clone.getPaste().getPosition() + clone.getAnalyseBean().getMinSeq() - 1).getEndlin();
                boolean pmdCloneFound = false;
                for (PmdClone pmdClone : pmdClones) {
                    for (int i = 0; i < pmdClone.ocurrencies.size(); i++) {
                        final PmdOcurrency pmdOcurrency1 = pmdClone.ocurrencies.get(i);
                        final String file1 = Conventions.fileNameInRepository(repository.getLocation(),
                                pmdOcurrency1.file);
                        for (int j = i + 1; j < pmdClone.ocurrencies.size(); j++) {
                            final PmdOcurrency pmdOcurrency2 = pmdClone.ocurrencies.get(j);
                            final String file2 = Conventions.fileNameInRepository(repository.getLocation(),
                                    pmdOcurrency2.file);
                            /*
                             * (u1.name='generic/target/
                             * CodeCloneType1. java' and
                             * u2.name='generic/target/CodeCloneType4.
                             * java' and ca1.beglin <=24 &&
                             * ca1_end.endlin>=23) or
                             * (u2.name='generic/target/
                             * CodeCloneType1. java' and
                             * u1.name='generic/target/CodeCloneType4.
                             * java' and ca2.beglin <=24 &&
                             * ca2_end.endlin>=23)
                             */

                            if ((file1.equals(unit1) && //
                                    file2.equals(unit2) && //
                                    pmdOcurrency1.line <= endlinCopy
                                    && pmdOcurrency1.line + pmdClone.lines - 1 >= beglinCopy)//
                                    || //
                                    (file2.equals(unit1) && //
                                            file1.equals(unit2) && //
                                            pmdOcurrency2.line <= endlinPaste
                                            && pmdOcurrency2.line + pmdClone.lines - 1 >= beglinPaste)) {
                                pmdCloneFound = true;
                            }
                        }

                    }
                }
                if (found + notFound == 0) {
                    System.out.println();
                    System.out.println("Results found by McSheep and not found by PMD");
                    System.out.println("=============================================");
                }

                String cloneStr = unit1 + ":[" + beglinCopy + "-" + endlinCopy + "] " + unit2 + ":[" + beglinPaste + "-"
                        + endlinPaste + "] " + clone;
                if (!pmdCloneFound) {
                    System.err.println("Clone not found by PMD: " + cloneStr);
                    notFound++;
                } else {
                    System.out.println("Clone found by PMD: " + cloneStr);
                    found++;
                }
            }
        }

        System.err.println("Not found: " + notFound);
        System.out.println("Found: " + found);
    }
}
