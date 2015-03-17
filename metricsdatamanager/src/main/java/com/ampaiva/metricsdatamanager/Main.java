package com.ampaiva.metricsdatamanager;

import japa.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.connectors.HttpClient4Connector;
import org.sonar.wsclient.services.Duplications;
import org.sonar.wsclient.services.Duplications.Block;
import org.sonar.wsclient.services.DuplicationsMgr;

import com.ampaiva.hlo.cm.ConcernCollection;
import com.ampaiva.hlo.cm.ConcernMetric;
import com.ampaiva.hlo.cm.ConcernMetricNode;
import com.ampaiva.hlo.cm.IMethodCalls;
import com.ampaiva.hlo.cm.MetricsColector;
import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.controller.ConcernCallsManager;
import com.ampaiva.metricsdatamanager.controller.DataManager;
import com.ampaiva.metricsdatamanager.controller.EOcurrencyType;
import com.ampaiva.metricsdatamanager.controller.IDataManager;
import com.ampaiva.metricsdatamanager.controller.MetricsManager;
import com.ampaiva.metricsdatamanager.model.Ocurrency;
import com.ampaiva.metricsdatamanager.util.HashArray;

public class Main {
    private final IDataManager dataManager = new DataManager("metricsdatamanager");
    private final MetricsManager metricsManager = new MetricsManager(dataManager);
    private final List<IMethodCalls> concernCollections = new ArrayList<IMethodCalls>();

    private MetricsColector getMetrics(Map<String, String> sources) throws ParseException, FileNotFoundException,
            IOException {
        MetricsColector metricsColector = new MetricsColector(sources);
        return metricsColector;
    }

    private Map<String, String> getFilesInZip(String zipFilePath) throws IOException {
        Map<String, String> sources = new HashMap<String, String>();
        ZipFile zipFile = new ZipFile(zipFilePath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory() || !entry.getName().toLowerCase().endsWith(".java")) {
                continue;
            }
            sources.put(entry.getName().substring(entry.getName().indexOf('/') + 1),
                    Helper.convertInputStream2String(zipFile.getInputStream(entry)));
        }
        zipFile.close();
        return sources;
    }

    private File[] getZipFilesFrom(String folder) throws Exception {
        File[] files = new File(folder).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip");
            }
        });

        return files;
    }

    private void getMetricsofAllFiles(String folder, boolean shouldPersist) throws Exception {
        File[] files = getZipFilesFrom(folder);
        if (shouldPersist) {
            metricsManager.deleteAllData();
        }
        for (File zipFile : files) {
            System.out.println(zipFile.getName());
            MetricsColector metricsColector = getMetrics(getFilesInZip(zipFile.getAbsolutePath()));
            if (shouldPersist) {
                persist(getProjectKey(zipFile), zipFile.getAbsolutePath(), metricsColector);
            }
            persistConcernCollection(zipFile.getAbsolutePath(), metricsColector);
        }
    }

    private void persist(String projectKey, String projectLocation, MetricsColector metricsColector)
            throws ParseException {
        Map<String, List<ConcernMetricNode>> metrics = new HashMap<String, List<ConcernMetricNode>>();
        HashMap<String, List<ConcernMetric>> hash = metricsColector.getMetrics().getHash();
        for (Entry<String, List<ConcernMetric>> entry : hash.entrySet()) {
            for (ConcernMetric concernMetric : entry.getValue()) {
                if (!(concernMetric instanceof ConcernCollection)) {
                    metrics.put(entry.getKey(), concernMetric.getNodes());
                }
            }
        }
        metricsManager.persist(projectKey, projectLocation, EOcurrencyType.EXCEPTION_HANDLING, metrics);
    }

    private void persistConcernCollection(String projectLocation, MetricsColector metricsColector)
            throws ParseException {
        HashMap<String, List<ConcernMetric>> hash = metricsColector.getMetrics().getHash();
        for (Entry<String, List<ConcernMetric>> entry : hash.entrySet()) {
            for (ConcernMetric concernMetric : entry.getValue()) {
                if (concernMetric instanceof ConcernCollection) {
                    concernCollections.add((ConcernCollection) concernMetric);
                }
            }
        }
    }

    private void persist(String projectKey, List<Duplications> duplicationsList) {
        for (Duplications duplications : duplicationsList) {
            List<Block> blocks = duplications.getBlocks();
            for (Block block : blocks) {
                System.out.println(block);
                String stBlock = block.toString().substring(1, block.toString().lastIndexOf(']'));
                persistDuplicationBlock(projectKey, duplications, stBlock);
            }
        }
    }

    public void persistDuplicationBlock(String projectKey, Duplications duplications, String stBlock) {
        List<Integer> ocurrencies = new ArrayList<Integer>();
        while (stBlock.length() > 0) {
            String[] duplication2 = stBlock.substring(stBlock.indexOf('[') + 1, stBlock.indexOf(']')).split(",");
            stBlock = stBlock.substring(stBlock.indexOf(']') + 1);
            org.sonar.wsclient.services.Duplications.File file = duplications.getFile(duplication2[0]
                    .substring(duplication2[0].indexOf('=') + 1));
            StringTokenizer stFile = new StringTokenizer(file.toString(), "=,");
            // discard key
            stFile.nextToken();
            stFile.nextToken();
            stFile.nextToken();
            String resourceName = stFile.nextToken();
            resourceName = resourceName.substring(resourceName.indexOf('/') + 1);
            System.out.println(resourceName);
            int from = Integer.parseInt(duplication2[1].substring(duplication2[1].indexOf('=') + 1));
            int size = Integer.parseInt(duplication2[2].substring(duplication2[2].indexOf('=') + 1));

            Ocurrency ocurrency = metricsManager.persist(projectKey, resourceName, EOcurrencyType.DUPLICATION, from, 0,
                    from + size, 0);
            ocurrencies.add(ocurrency.getId());
        }
        int copy = ocurrencies.remove(0).intValue();
        System.out.println("Duplication: copy=" + copy + " paste=" + ocurrencies);
        metricsManager.persist(copy, ocurrencies);
    }

    private List<Duplications> getDuplicationsofProject(String projectName) throws Exception {
        String url = "http://localhost:9000";
        String login = "admin";
        String password = "admin";
        Sonar sonar = new Sonar(new HttpClient4Connector(new Host(url, login, password)));
        DuplicationsMgr duplicationsMgr = new DuplicationsMgr(sonar);
        List<Duplications> duplicationsList = duplicationsMgr.getDuplicationsforResources(projectName);
        return duplicationsList;
    }

    private String getProjectKey(String propFileName) throws IOException {
        InputStream inputStream = new FileInputStream(propFileName);
        Properties prop = new Properties();
        prop.load(inputStream);
        return prop.getProperty("sonar.projectKey");

    }

    private List<String> getDuplicationsofConcernMetrics() {
        ConcernCallsManager concernCallsManager = new ConcernCallsManager(new HashArray());
        concernCallsManager.setCallsHash(concernCollections);
        return concernCallsManager.getDuplications(concernCollections);
    }

    private void getDuplicationsofAllFiles(String folder) throws Exception {
        File[] files = getZipFilesFrom(folder);
        for (File zipFile : files) {
            String projectKey = getProjectKey(zipFile);
            List<Duplications> duplicationsList = getDuplicationsofProject(projectKey);
            persist(projectKey, duplicationsList);
        }
    }

    public String getProjectKey(File zipFile) throws IOException {
        String propFileName = zipFile.getAbsolutePath();
        propFileName = propFileName.substring(0, propFileName.length() - 4) + File.separatorChar
                + "sonar-project.properties";
        String projectKey = getProjectKey(propFileName);
        return projectKey;
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        //        String folder = "C:/opt/tools/target-projects";
        String folder = "C:/Temp";
        main.getMetricsofAllFiles(folder, false);
        //main.getDuplicationsofAllFiles(folder);
        for (String string : main.getDuplicationsofConcernMetrics()) {
            String[] dup = string.split(ConcernCallsManager.SEPARATOR);
            for (String string2 : dup) {
                System.out.println(string2);
            }
            System.out.println();
        }
        System.out.println(main.getDuplicationsofConcernMetrics().size());
    }
}
