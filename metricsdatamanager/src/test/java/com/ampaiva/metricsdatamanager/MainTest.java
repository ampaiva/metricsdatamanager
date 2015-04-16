package com.ampaiva.metricsdatamanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

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

public class MainTest {

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
                "src/test/resources/com/ampaiva/metricsdatamanager/util/ZipTest3.zip")));
        List<ICodeSource> codeSources = Arrays.asList((ICodeSource) zipStreamUtil);
        List<IMethodCalls> allClasses = main.getConcernCollectionofAllFiles(metricsSource, codeSources);
        IConcernCallsConfig config = new IConcernCallsConfig() {

            @Override
            public int getMinSeq() {
                return 1;
            }

            @Override
            public int getMaxDistance() {
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
                    assertEquals(methodsB.size(), allClasses.get(j + i + 1).getMethodSources().size());
                    for (int l = 0; l < methodsB.size(); l++) {
                        int[] dups = methodsB.get(l);
                        if (dups == null) {
                            continue;
                        }
                        // dups is odd
                        assertTrue((dups.length % 2) == 0);
                        ConcernClone clone = concernCallsManager.getConcernClone(allClasses.get(i),
                                allClasses.get(j + i + 1), k, l, dups);
                        assertNotNull(clone);
                    }
                }
            }
        }
    }
}
