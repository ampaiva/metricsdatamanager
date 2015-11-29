package com.ampaiva.metricsdatamanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.ampaiva.hlo.cm.ConcernCollection;
import com.ampaiva.hlo.cm.ConcernMetricNode;
import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.cm.IConcernMetric;
import com.ampaiva.hlo.cm.IMethodCalls;
import com.ampaiva.hlo.cm.IMetricsSource;
import com.ampaiva.hlo.cm.MetricsColector;
import com.ampaiva.metricsdatamanager.controller.DataManager;
import com.ampaiva.metricsdatamanager.controller.EOcurrencyType;
import com.ampaiva.metricsdatamanager.controller.IDataManager;
import com.ampaiva.metricsdatamanager.controller.MetricsManager;
import com.ampaiva.metricsdatamanager.util.ZipUtil;
import com.github.javaparser.ParseException;

public class Main {
    private final IDataManager dataManager = new DataManager("metricsdatamanager");
    private final MetricsManager metricsManager = new MetricsManager(dataManager);
    private final List<IMethodCalls> concernCollections = new ArrayList<IMethodCalls>();

    private File[] getZipFilesFrom(String folder) throws Exception {
        File[] files = new File(folder).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip");
            }
        });

        return files;
    }

    void getMetricsofAllFiles(IMetricsSource metricsSource, String folder, boolean shouldPersist) throws Exception {
        File[] files = getZipFilesFrom(folder);
        if (shouldPersist) {
            metricsManager.deleteAllData();
        }
        for (File zipFile : files) {
            System.out.println(zipFile.getName());
            ZipUtil zipUtil = new ZipUtil(zipFile.getAbsolutePath());
            MetricsColector metricsColector = new MetricsColector(metricsSource, zipUtil);
            if (shouldPersist) {
                persist(getProjectKey(zipFile), zipFile.getAbsolutePath(), metricsColector);
            }
            persistConcernCollection(metricsColector);
        }
    }

    public List<IMethodCalls> getConcernCollectionofAllFiles(IMetricsSource metricsSource,
            List<ICodeSource> codeSources) throws Exception {
        for (ICodeSource codeSource : codeSources) {
            MetricsColector metricsColector = new MetricsColector(metricsSource, codeSource);
            persistConcernCollection(metricsColector);
        }

        return concernCollections;
    }

    private void persist(String projectKey, String projectLocation, MetricsColector metricsColector)
            throws ParseException, IOException {
        Map<String, List<ConcernMetricNode>> metrics = new HashMap<String, List<ConcernMetricNode>>();
        HashMap<String, List<IConcernMetric>> hash = metricsColector.getMetrics().getHash();
        for (Entry<String, List<IConcernMetric>> entry : hash.entrySet()) {
            for (IConcernMetric concernMetric : entry.getValue()) {
                if (!(concernMetric instanceof ConcernCollection)) {
                    metrics.put(entry.getKey(), concernMetric.getNodes());
                }
            }
        }
        metricsManager.persist(projectKey, projectLocation, EOcurrencyType.EXCEPTION_HANDLING, metrics);
    }

    private void persistConcernCollection(MetricsColector metricsColector) throws ParseException, IOException {
        HashMap<String, List<IConcernMetric>> hash = metricsColector.getMetrics().getHash();
        for (Entry<String, List<IConcernMetric>> entry : hash.entrySet()) {
            for (IConcernMetric concernMetric : entry.getValue()) {
                if (concernMetric instanceof ConcernCollection) {
                    concernCollections.add((ConcernCollection) concernMetric);
                }
            }
        }
    }

    private String getProjectKey(String propFileName) throws IOException {
        InputStream inputStream = new FileInputStream(propFileName);
        Properties prop = new Properties();
        prop.load(inputStream);
        return prop.getProperty("sonar.projectKey");

    }

    public String getProjectKey(File zipFile) throws IOException {
        String propFileName = zipFile.getAbsolutePath();
        propFileName = propFileName.substring(0, propFileName.length() - 4) + File.separatorChar
                + "sonar-project.properties";
        try {
            return getProjectKey(propFileName);
        } catch (FileNotFoundException ex) {
        }
        return zipFile.getName();
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        //        String folder = "C:/opt/tools/target-projects";
        String folder = "C:/temp";
        IMetricsSource metricsSource = new IMetricsSource() {

            @Override
            public List<IConcernMetric> getConcernMetrics() {
                return Arrays.asList((IConcernMetric) new ConcernCollection());
            }
        };
        main.getMetricsofAllFiles(metricsSource, folder, false);
    }
}
