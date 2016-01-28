package com.ampaiva.metricsdatamanager.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.CloneGroup;
import com.ampaiva.metricsdatamanager.CloneSnippet;
import com.ampaiva.metricsdatamanager.model.Repository;

import freemarker.template.TemplateException;

public class FreeMarkerTest {

    private String generateLongName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append("X");
        }
        return sb.toString();
    }

    @Test
    public void testSave() throws IOException, TemplateException {
        BasicConfigurator.configure();
        Repository repositoryA = new Repository();
        repositoryA.setLocation("\\B\\A");
        FreeMarker.configure("target/classes/ftl");
        String htmlFolderPath = "/temp/html";
        String source = Helper.convertFile2String(new File("target/test-classes/snippet/SalesDetailReportView.java"));
        String source2 = "public void x(){\na();\n\nb();\nc();\nd();\n}";
        CloneGroup cloneGroup = new CloneGroup(new CloneSnippet[] { //
                new CloneSnippet("file1", "file1", 2, 4, source2), //
                new CloneSnippet("file1", "file1", 6, 6, source2), //
                new CloneSnippet(generateLongName(), generateLongName(), 46, 66, source) }, true);
        List<CloneGroup> clones = Arrays.asList(cloneGroup, cloneGroup, cloneGroup);
        FreeMarker.saveClonesToHTML(htmlFolderPath, repositoryA, "McSheep", clones);
        FreeMarker.saveClonesToHTML(htmlFolderPath, repositoryA, "PMD", clones);
        FreeMarker.saveIndex(htmlFolderPath);

        BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testSaveB() throws IOException, TemplateException {
        BasicConfigurator.configure();
        Repository repositoryB = new Repository();
        repositoryB.setLocation("\\B\\B");
        FreeMarker.configure("target/classes/ftl");
        String htmlFolderPath = "/temp/html";
        String source = "public void x(){\na();\n\nb();\nc();\nd();\n}";
        CloneGroup cloneGroup = new CloneGroup(new CloneSnippet[] { //
                new CloneSnippet("file1", "file1", 2, 4, source), //
                new CloneSnippet("file1", "file1", 6, 6, source), //
                new CloneSnippet("file2", "file2", 2, 4, source), //
                new CloneSnippet("file2", "file2", 6, 6, source) //
        }, true);
        List<CloneGroup> clones = Arrays.asList(cloneGroup, cloneGroup, cloneGroup);
        FreeMarker.saveClonesToHTML(htmlFolderPath, repositoryB, "McSheep", clones);
        FreeMarker.saveClonesToHTML(htmlFolderPath, repositoryB, "PMD", clones);
        FreeMarker.saveIndex(htmlFolderPath);

        BasicConfigurator.resetConfiguration();
    }
}