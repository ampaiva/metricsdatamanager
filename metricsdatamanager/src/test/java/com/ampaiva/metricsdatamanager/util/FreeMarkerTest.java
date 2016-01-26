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

    @Test
    public void testSave() throws IOException, TemplateException {
        BasicConfigurator.configure();
        Repository repositoryA = new Repository();
        repositoryA.setLocation("\\B\\A");
        FreeMarker.configure("target/classes/ftl");
        String htmlFolderPath = "/temp/html";
        String source = Helper.convertFile2String(new File("target/test-classes/snippet/SalesDetailReportView.java"));
        CloneGroup clonePair = new CloneGroup(
                new CloneSnippet[] { new CloneSnippet("file1", 1, 3, "public void x(){\na();\n\nb();\n}"),
                        new CloneSnippet(generateLongName(), 46, 66, source) },
                true);
        List<CloneGroup> clones = Arrays.asList(clonePair, clonePair, clonePair);
        FreeMarker.saveClonesToHTML(htmlFolderPath, repositoryA, "McSheep", clones);
        FreeMarker.saveClonesToHTML(htmlFolderPath, repositoryA, "PMD", clones);
        FreeMarker.saveIndex(htmlFolderPath);

        BasicConfigurator.resetConfiguration();
    }

    private String generateLongName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append("X");
        }
        return sb.toString();
    }
}