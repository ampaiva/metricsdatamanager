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
            for (ConcernMetricNode concernMetricNode : entry.getValue()) {
                addOcurrency(resource, ocurrencyType, concernMetricNode.getBeginLine(),
                        concernMetricNode.getBeginColumn(), concernMetricNode.getEndLine(),
                        concernMetricNode.getEndColumn());
            }
            resources.add(resource);
        }
        project.setResources(resources);
        dataManager.open();
        dataManager.persist(project);
        dataManager.close();
        return project;
    }

    private Ocurrency addOcurrency(Resource resource, EOcurrencyType ocurrencyType, int beginline, int begincolumn,
            int endline, int endcolumn) {
        Ocurrency ocurrency = new Ocurrency();
        ocurrency.setType(ocurrencyType.ordinal());
        ocurrency.setResourceBean(resource);
        ocurrency.setBeginline(beginline);
        ocurrency.setBegincolumn(begincolumn);
        ocurrency.setEndline(endline);
        ocurrency.setEndcolumn(endcolumn);
        List<Ocurrency> ocurrenciesList = resource.getOcurrencies();
        if (ocurrenciesList == null) {
            resource.setOcurrencies(new ArrayList<Ocurrency>());
        }
        resource.addOcurrency(ocurrency);
        return ocurrency;
    }

    public Ocurrency persist(String projectName, String resourceName, EOcurrencyType ocurrencyType, int beginline,
            int begincolumn, int endline, int endcolumn) {
        dataManager.open();
        Resource resource = dataManager.getResourceByName(projectName, resourceName);
        Ocurrency ocurrency = addOcurrency(resource, ocurrencyType, beginline, begincolumn, endline, endcolumn);
        dataManager.persist(ocurrency);
        dataManager.close();
        return ocurrency;
    }

    public List<Duplication> persist(int copy, List<Integer> pastes) {
        dataManager.open();
        List<Duplication> duplications = new ArrayList<Duplication>();
        for (Integer paste : pastes) {
            Duplication duplication = new Duplication();
            duplication.setCopy(copy);
            duplication.setPaste(paste);
            dataManager.persist(duplication);
            duplications.add(duplication);
        }
        dataManager.close();
        return duplications;
    }
}
