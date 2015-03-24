package com.ampaiva.metricsdatamanager;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ampaiva.hlo.cm.ConcernCollection;
import com.ampaiva.hlo.cm.IConcernMetric;
import com.ampaiva.hlo.cm.IMetricsSource;
import com.ampaiva.metricsdatamanager.controller.ConcernCallsManager;

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
            String[] dup = string.split(ConcernCallsManager.SEPARATOR);
            for (String string2 : dup) {
                System.out.println(string2);
            }
            System.out.println();
        }
        System.out.println(duplicationsofConcernMetrics.size());
    }

}
