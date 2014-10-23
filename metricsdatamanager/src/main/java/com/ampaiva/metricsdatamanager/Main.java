package com.ampaiva.metricsdatamanager;

import japa.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.ampaiva.hlo.cm.ConcernMetric;
import com.ampaiva.hlo.cm.ConcernMetricNode;
import com.ampaiva.hlo.cm.MetricsColector;
import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.controller.DataManager;
import com.ampaiva.metricsdatamanager.controller.IDataManager;
import com.ampaiva.metricsdatamanager.controller.MetricsManager;

public class Main {
    public MetricsColector getMetrics(List<String> sources) throws ParseException, FileNotFoundException, IOException {
        MetricsColector metricsColector = new MetricsColector(sources);
        return metricsColector;
    }

    private List<String> getFilesInZip(String zipFilePath) throws IOException {
        List<String> sources = new ArrayList<String>();
        ZipFile zipFile = new ZipFile(zipFilePath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory() || !entry.getName().toLowerCase().endsWith(".java")) {
                continue;
            }
            System.out.println(entry.getName());
            sources.add(Helper.convertInputStream2String(zipFile.getInputStream(entry)));
        }
        zipFile.close();
        return sources;
    }

    public void getMetricsofAllFiles(String folder) throws Exception {
        File[] files = new File(folder).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip");
            }
        });
        for (File zipFile : files) {
            MetricsColector metricsColector = getMetrics(getFilesInZip(zipFile.getAbsolutePath()));
            persist(zipFile.getName(), zipFile.getAbsolutePath(), metricsColector);
        }
    }

    public void persist(String projectName, String projectLocation, MetricsColector metricsColector)
            throws ParseException {
        IDataManager dataManager = new DataManager("metricsdatamanager");
        MetricsManager metricsManager = new MetricsManager(dataManager);
        Map<String, List<ConcernMetricNode>> metrics = new HashMap<String, List<ConcernMetricNode>>();
        HashMap<String, ConcernMetric> hash = metricsColector.getMetrics().getHash();
        for (Entry<String, ConcernMetric> entry : hash.entrySet()) {
            metrics.put(entry.getKey(), entry.getValue().getNodes());
        }
        metricsManager.persist(projectName, projectLocation, metrics);
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        String folder = "C:/opt/tools/target-projects";
        main.getMetricsofAllFiles(folder);
    }
}
