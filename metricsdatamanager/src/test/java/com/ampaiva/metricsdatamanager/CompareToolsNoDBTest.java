package com.ampaiva.metricsdatamanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.github.javaparser.ParseException;

public class CompareToolsNoDBTest {

    private void assertListValues(List<CloneGroup> clonePairs, int expectedFound, int expectedNotFound) {
        assertNotNull(clonePairs);
        assertEquals(expectedFound + expectedNotFound, clonePairs.size());
        int found = 0, notFound = 0;
        for (CloneGroup clonePair : clonePairs) {
            if (clonePair.found) {
                found++;
            } else {
                notFound++;
            }
        }
        assertEquals(expectedFound, found);
        assertEquals(expectedNotFound, notFound);
    }

    private void testCompareMcSheepxPMD02(String system, String folder, int expectedPMDFound, int expectedPMDNotFound,
            int expectedMcSheepFound, int expectedMcSheepNotFound) throws IOException, ParseException {
        ExtractClones extractClones = new ExtractClones(10, 10);
        List<Repository> repositories = extractClones.run("target/test-classes/" + system, false);
        File csvFile = new File("target/test-classes/pmd/" + system + ".csv");
        String pmdResult = Helper.readFile(csvFile);
        CompareToolsNoDB compareTools = new CompareToolsNoDB();
        Repository repository = repositories.get(0);
        repository.setLocation("c:/temp/extracted/" + folder);
        List<CloneGroup> clonesPMD = compareTools.comparePMDxMcSheep(repository, pmdResult);
        assertListValues(clonesPMD, expectedPMDFound, expectedPMDNotFound);
        List<CloneGroup> clonesMcSheep = compareTools.compareMcSheepxPMD(repository, pmdResult);
        assertListValues(clonesMcSheep, expectedMcSheepFound, expectedMcSheepNotFound);
        System.out.println("PMD");
        for (CloneGroup clonePair : clonesPMD) {
            if (clonePair.found) {
                System.out.println(clonePair);
            }
        }
        System.out.println("McSheep");
        for (CloneGroup clonePair : clonesMcSheep) {
            if (clonePair.found) {
                System.out.println(clonePair);
            }
        }
    }

    @Test
    public void testCompareMcSheepxPMD02_ecommerce() throws IOException, ParseException {
        //testCompareMcSheepxPMD02("Petstore", "PetStore-petstore-1.3.1_02", 3, 25, 3, 3);
        //testCompareMcSheepxPMD02("maven", "maven-master", 10, 42, 9, 6);
        //testCompareMcSheepxPMD02("02-ecommerce", "02-ecommerce", 45, 64, 41, 68);
    }

}
