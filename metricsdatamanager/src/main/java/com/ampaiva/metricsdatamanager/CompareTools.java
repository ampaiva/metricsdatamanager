package com.ampaiva.metricsdatamanager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.controller.ConfigDataManager;
import com.ampaiva.metricsdatamanager.controller.IDataManager;
import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd.PmdClone;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd.PmdClone.PmdOcurrency;
import com.ampaiva.metricsdatamanager.util.Config;
import com.ampaiva.metricsdatamanager.util.Conventions;
import com.github.javaparser.ParseException;

public class CompareTools {

    final IDataManager dataManager;

    public CompareTools(IDataManager dataManager) {
        this.dataManager = dataManager;
    }

    /*
     * Finds all McSheeps clones of a repository
     * 
     * @return list of McSheeps clone Ids
     */
    private List<Integer> getMcSheepClonesbyRepository(String repository) throws SQLException {
        dataManager.open();
        String sqlString = "SELECT c.ID " + "FROM clones c " + "inner join analysis a on a.id=analyse "
                + "inner join repositories r on r.id=a.repository " + "inner join calls ca1 on ca1.id=c.copy "
                + "inner join calls ca2 on ca2.id=c.paste " + "inner join methods m1 on m1.id=ca1.method "
                + "inner join units u1 on u1.id=m1.unit " + "inner join methods m2 on m2.id=ca2.method "
                + "inner join units u2 on u2.id=m2.unit "
                + "inner join calls ca1_end on ca1_end.method=m1.id and ca1_end.position=ca1.position+a.MINSEQ-1 "
                + "inner join calls ca2_end on ca2_end.method=m2.id and ca2_end.position=ca2.position+a.MINSEQ-1 "
                + "where (r.location like '" + repository.replace("\\", "\\\\\\\\") + "')";

        Connection con = dataManager.getEM().unwrap(java.sql.Connection.class);
        Statement stmt = con.createStatement();
        List<Integer> results = new ArrayList<>();
        try {
            ResultSet rs = stmt.executeQuery(sqlString);
            boolean hasElements;
            for (hasElements = rs.first(); hasElements; hasElements = rs.next()) {
                results.add(rs.getInt(1));
            }
        } finally {
            stmt.close();
        }

        dataManager.close();
        return results;
    }

    /*
     * Finds all McSheeps clones that are inside pmdClone
     * 
     * @return list of McSheeps clone Ids
     */
    private List<Integer> getMcSheepClones(String unit1, int unit1beglin, int unit1endlin, String unit2)
            throws SQLException {
        dataManager.open();
        String sqlString = "SELECT c.ID " + "FROM clones c " + "inner join analysis a on a.id=analyse "
                + "inner join calls ca1 on ca1.id=c.copy " + "inner join calls ca2 on ca2.id=c.paste "
                + "inner join methods m1 on m1.id=ca1.method " + "inner join units u1 on u1.id=m1.unit "
                + "inner join methods m2 on m2.id=ca2.method " + "inner join units u2 on u2.id=m2.unit "
                + "inner join calls ca1_end on ca1_end.method=m1.id and ca1_end.position=ca1.position+a.MINSEQ-1 "
                + "inner join calls ca2_end on ca2_end.method=m2.id and ca2_end.position=ca2.position+a.MINSEQ-1 "
                + "where (u1.name='" + unit1 + "' and u2.name='" + unit2 + "' and ca1.beglin >=" + unit1beglin
                + " && ca1_end.endlin<=" + unit1endlin + ") " + "or    (u2.name='" + unit1 + "' and u1.name='" + unit2
                + "' and ca2.beglin >=" + unit1beglin + " && ca2_end.endlin<=" + unit1endlin + ")";

        Connection con = dataManager.getEM().unwrap(java.sql.Connection.class);
        Statement stmt = con.createStatement();
        List<Integer> results = new ArrayList<>();
        try {
            ResultSet rs = stmt.executeQuery(sqlString);
            boolean hasElements;
            for (hasElements = rs.first(); hasElements; hasElements = rs.next()) {
                results.add(rs.getInt(1));
            }
        } finally {
            stmt.close();
        }

        dataManager.close();
        return results;
    }

    public void comparePMDxMcSheep(String repository, String pmdResult) throws SQLException {
        List<PmdClone> pmdClones = Pmd.parse(repository, pmdResult);
        int found = 0, notFound = 0;
        for (PmdClone pmdClone : pmdClones) {
            boolean hasAllOcurrencies = true;
            for (int i = 0; i < pmdClone.ocurrencies.size(); i++) {
                final PmdOcurrency pmdOcurrency1 = pmdClone.ocurrencies.get(i);
                final String file1 = pmdOcurrency1.file;
                for (int j = i + 1; j < pmdClone.ocurrencies.size(); j++) {
                    final PmdOcurrency pmdOcurrency2 = pmdClone.ocurrencies.get(j);
                    final String file2 = pmdOcurrency2.file;
                    List<Integer> mcSheepClones = getMcSheepClones(file1, pmdOcurrency1.line,
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

    public void compareMcSheepxPMD(String repository, String pmdResult) throws SQLException {
        List<PmdClone> pmdClones = Pmd.parse(repository, pmdResult);
        int found = 0, notFound = 0;
        List<Integer> mcSheepClones = getMcSheepClonesbyRepository(repository);
        dataManager.open();
        for (Integer id : mcSheepClones) {
            Clone clone = dataManager.find(Clone.class, id);
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
                    final String file1 = Conventions.fileNameInRepository(repository, pmdOcurrency1.file);
                    for (int j = i + 1; j < pmdClone.ocurrencies.size(); j++) {
                        final PmdOcurrency pmdOcurrency2 = pmdClone.ocurrencies.get(j);
                        final String file2 = Conventions.fileNameInRepository(repository, pmdOcurrency2.file);
                        if ((file1.equals(unit1) && file2.equals(unit2) && pmdOcurrency1.line <= beglinCopy
                                && pmdOcurrency1.line + pmdClone.lines - 1 >= endlinCopy)
                                || (file1.equals(unit2) && file2.equals(unit1) && pmdOcurrency2.line <= beglinPaste
                                        && pmdOcurrency2.line + pmdClone.lines - 1 >= endlinPaste)) {
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
        dataManager.close();

        System.err.println("Not found: " + notFound);
        System.out.println("Found: " + found);
    }

    public static void main(String[] args) throws IOException, ParseException, SQLException {
        String propertiesFile = "target/classes/config.properties";
        Config config = new Config(propertiesFile);

        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        final IDataManager dataManager = new ConfigDataManager(config);
        CompareTools compareTools = new CompareTools(dataManager);
        String pmdResult = Helper.readFile(new File("src/test/resources/pmd/generic.csv"));
        compareTools.comparePMDxMcSheep("c:\\temp\\extracted", pmdResult);
        compareTools.compareMcSheepxPMD("c:\\temp\\extracted", pmdResult);

        BasicConfigurator.resetConfiguration();
    }

}
