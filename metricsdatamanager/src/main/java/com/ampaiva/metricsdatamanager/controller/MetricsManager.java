package com.ampaiva.metricsdatamanager.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ampaiva.hlo.cm.ConcernMetricNode;
import com.ampaiva.metricsdatamanager.model.Duplication;
import com.ampaiva.metricsdatamanager.model.Ocurrency;
import com.ampaiva.metricsdatamanager.model.Project;
import com.ampaiva.metricsdatamanager.model.Resource;

public class MetricsManager {
    private final IDataManager dataManager;

    public MetricsManager(IDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void deleteAllData() {
        dataManager.open();
        dataManager.removeAll(Duplication.class);
        dataManager.removeAll(Project.class);
        dataManager.close();
    }

    public Project persist(String projectName, String projectLocation, EOcurrencyType ocurrencyType,
            Map<String, List<ConcernMetricNode>> metrics) {
        Project project = new Project();
        project.setName(projectName);
        project.setLocation(projectLocation);
        List<Resource> resources = new ArrayList<Resource>();
        for (Entry<String, List<ConcernMetricNode>> entry : metrics.entrySet()) {
            Resource resource = new Resource();
            resource.setProjectBean(project);
            resource.setName(entry.getKey());
            List<Ocurrency> ocurrencies = new ArrayList<Ocurrency>();
            for (ConcernMetricNode concernMetricNode : entry.getValue()) {
                Ocurrency ocurrency = new Ocurrency();
                ocurrency.setType(ocurrencyType.ordinal());
                ocurrency.setResourceBean(resource);
                ocurrency.setBeginline(concernMetricNode.getBeginLine());
                ocurrency.setBegincolumn(concernMetricNode.getBeginColumn());
                ocurrency.setEndline(concernMetricNode.getEndLine());
                ocurrency.setEndcolumn(concernMetricNode.getEndColumn());
                ocurrencies.add(ocurrency);
            }
            resource.setOcurrencies(ocurrencies);
            resources.add(resource);
        }
        project.setResources(resources);
        dataManager.open();
        dataManager.persist(project);
        dataManager.close();
        return project;
    }
}
