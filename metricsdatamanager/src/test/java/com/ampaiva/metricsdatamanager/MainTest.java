package com.ampaiva.metricsdatamanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.ampaiva.googledrive.DriveManager;
import com.ampaiva.googledrive.DriveService;
import com.ampaiva.hlo.cm.ConcernCollection;
import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.cm.IConcernMetric;
import com.ampaiva.hlo.cm.IMethodCalls;
import com.ampaiva.hlo.cm.IMetricsSource;
import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.config.IConcernCallsConfig;
import com.ampaiva.metricsdatamanager.controller.ConcernCallsManager;
import com.ampaiva.metricsdatamanager.controller.ConcernClone;
import com.ampaiva.metricsdatamanager.util.HashArray;
import com.ampaiva.metricsdatamanager.util.IHashArray;
import com.ampaiva.metricsdatamanager.util.ZipStreamUtil;
import com.google.api.services.drive.Drive;

public class MainTest {

    @Test
    public void testMain() throws Exception {
        Main main = new Main();
        IMetricsSource metricsSource = new IMetricsSource() {

            @Override
            public List<IConcernMetric> getConcernMetrics() {
                return Arrays.asList((IConcernMetric) new ConcernCollection());
            }
        };
        main.getMetricsofAllFiles(metricsSource, "src/test/resources/com/ampaiva/metricsdatamanager/util", false);
        List<String> duplicationsofConcernMetrics = main.getDuplicationsofConcernMetrics();
        for (String string : duplicationsofConcernMetrics) {
            String[] dups = string.split(ConcernCallsManager.SEPARATOR);
            for (String dup : dups) {
                System.out.println(dup);
            }
            System.out.println();
        }
        System.out.println(duplicationsofConcernMetrics.size());
    }

    @Test
    public void testDuplicationsofConcernMetrics() throws Exception {
        Main main = new Main();
        IMetricsSource metricsSource = new IMetricsSource() {

            @Override
            public List<IConcernMetric> getConcernMetrics() {
                return Arrays.asList((IConcernMetric) new ConcernCollection());
            }
        };

        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(new File(
                "src/test/resources/com/ampaiva/metricsdatamanager/util/ZipTest1.zip")));
        List<ICodeSource> codeSources = Arrays.asList((ICodeSource) zipStreamUtil);
        main.getConcernCollectionofAllFiles(metricsSource, codeSources);
        List<String> duplicationsofConcernMetrics = main.getDuplicationsofConcernMetrics();
        for (String string : duplicationsofConcernMetrics) {
            String[] dups = string.split(ConcernCallsManager.SEPARATOR);
            for (String dup : dups) {
                System.out.println(dup);
            }
            System.out.println();
        }
        System.out.println(duplicationsofConcernMetrics.size());
    }

    @Test
    public void testDuplicationsofConcernMetrics2() throws Exception {
        Main main = new Main();
        IMetricsSource metricsSource = new IMetricsSource() {

            @Override
            public List<IConcernMetric> getConcernMetrics() {
                return Arrays.asList((IConcernMetric) new ConcernCollection());
            }
        };

        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(new File(
                "src/test/resources/com/ampaiva/metricsdatamanager/util/ZipTest2.zip")));
        List<ICodeSource> codeSources = Arrays.asList((ICodeSource) zipStreamUtil);
        List<IMethodCalls> allClasses = main.getConcernCollectionofAllFiles(metricsSource, codeSources);
        IConcernCallsConfig config = new IConcernCallsConfig() {

            @Override
            public int getMinSeq() {
                return 5;
            }
        };
        IHashArray hashArray = new HashArray();
        ConcernCallsManager concernCallsManager = new ConcernCallsManager(config, hashArray);
        List<List<List<List<int[]>>>> dupAllClasses = main.getDuplicationsofConcernMetrics2(concernCallsManager,
                allClasses);
        assertNotNull(dupAllClasses);
        // n classes => n-1 list of duplications
        // class 0 is compared against 1..n
        // class 1 is compared against 2..n
        // ...
        // class n-1 is compared against n
        assertEquals(allClasses.size() - 1, dupAllClasses.size());
        for (int i = 0; i < dupAllClasses.size(); i++) {
            List<List<List<int[]>>> duplications = dupAllClasses.get(i);
            assertNotNull(duplications);
            // Each method of class i is compared against all methods of all other classes
            // So, duplications has size equals the number of classes after i
            assertEquals(dupAllClasses.size() - i, duplications.size());
            for (int j = 0; j < duplications.size(); j++) {
                List<List<int[]>> methodsA = duplications.get(j);
                assertNotNull(methodsA);
                assertEquals(allClasses.get(i).getMethodNames().size(), methodsA.size());
                for (int k = 0; k < methodsA.size(); k++) {
                    List<int[]> methodsB = methodsA.get(k);
                    assertNotNull(methodsB);
                    assertEquals(allClasses.get(j + i + 1).getMethodNames().size(), methodsB.size());
                    for (int l = 0; l < methodsB.size(); l++) {
                        int[] dups = methodsB.get(l);
                        if (dups == null) {
                            continue;
                        }
                        // dups is odd
                        assertTrue((dups.length % 2) == 0);
                        ConcernClone clone = concernCallsManager.convert2(allClasses.get(i), allClasses.get(j + i + 1),
                                k, l, dups);
                        assertNotNull(clone);
                    }
                }
            }
        }
    }

    @Ignore
    public void testDuplicationsofConcernMetricsFromGoogleDrive() throws Exception {
        Main main = new Main();
        IMetricsSource metricsSource = new IMetricsSource() {

            @Override
            public List<IConcernMetric> getConcernMetrics() {
                return Arrays.asList((IConcernMetric) new ConcernCollection());
            }
        };

        DriveService driveService = new DriveService("ampaiva@gmail.com",
                "177572291168-b24dvtscvk8dpivteq16c000rd77enm6@developer.gserviceaccount.com",
                "../googledrive/src/main/resources/concernclone-f58a967ec27b.p12");
        Drive service = driveService.getService();
        DriveManager driveManager = new DriveManager(service);
        com.google.api.services.drive.model.File folder = driveManager.getFileByTitle("ConcernClone");
        assertNotNull(folder);
        List<com.google.api.services.drive.model.File> zipFiles = driveManager.getFilesInFolder(folder.getId(),
                "application/zip");
        assertNotNull(zipFiles);
        assertEquals(9, zipFiles.size());
        List<ICodeSource> codeSources = new ArrayList<ICodeSource>();
        for (com.google.api.services.drive.model.File zipFile : zipFiles) {
            final Map<String, String> zipMap = driveManager.getFilesInZip(zipFile, ".java");
            assertNotNull(zipMap);
            codeSources.add(new ICodeSource() {
                @Override
                public Map<String, String> getCodeSource() throws IOException {
                    return zipMap;
                }
            });
        }

        main.getConcernCollectionofAllFiles(metricsSource, codeSources);
        List<String> duplicationsofConcernMetrics = main.getDuplicationsofConcernMetrics();
        for (String string : duplicationsofConcernMetrics) {
            String[] dups = string.split(ConcernCallsManager.SEPARATOR);
            for (String dup : dups) {
                System.out.println(dup);
            }
            System.out.println();
        }
        System.out.println(duplicationsofConcernMetrics.size());
    }
}
