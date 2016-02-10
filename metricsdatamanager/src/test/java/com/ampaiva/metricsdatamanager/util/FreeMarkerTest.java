package com.ampaiva.metricsdatamanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Ignore;
import org.junit.Test;

import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.CloneGroup;
import com.ampaiva.metricsdatamanager.CloneSnippet;
import com.ampaiva.metricsdatamanager.model.Repository;

import freemarker.template.TemplateException;

public class FreeMarkerTest {
    private static final String htmlFolderPath = "/temp/html";
    private static final String htmlClonesFolderPath = htmlFolderPath + "/26-13";

    private String generateLongName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append("X");
        }
        return sb.toString();
    }

    @Ignore
    public void testSave() throws IOException, TemplateException {
        BasicConfigurator.configure();
        Repository repositoryA = new Repository();
        repositoryA.setLocation("\\B\\A");
        FreeMarker.configure("target/classes/ftl");
        String source = Helper.convertFile2String(new File("target/test-classes/snippet/SalesDetailReportView.java"));
        String source2 = "public void x(){\na();\n\nb();\nc();\nd();\n}";
        CloneGroup cloneGroup = new CloneGroup(new CloneSnippet[] { //
                new CloneSnippet("file1", "file1", 2, 2, 4, source2), //
                new CloneSnippet("file1", "file1", 1, 6, 6, source2), //
                new CloneSnippet(generateLongName(), generateLongName(), 20, 46, 66, source) }, true);
        List<CloneGroup> clones = Arrays.asList(cloneGroup, cloneGroup, cloneGroup);
        FreeMarker.saveClonesToHTML(htmlClonesFolderPath, repositoryA, "McSheep", clones);
        FreeMarker.saveClonesToHTML(htmlClonesFolderPath, repositoryA, "PMD", clones);

        BasicConfigurator.resetConfiguration();
    }

    @Ignore
    public void testSaveB() throws IOException, TemplateException {
        BasicConfigurator.configure();
        Repository repositoryB = new Repository();
        repositoryB.setLocation("\\B\\B");
        FreeMarker.configure("target/classes/ftl");
        String source = "public void x(){\na();\n\nb();\nc();\nd();\n}";
        CloneGroup cloneGroup = new CloneGroup(new CloneSnippet[] { //
                new CloneSnippet("file1", "file1", 2, 2, 4, source), //
                new CloneSnippet("file1", "file1", 1, 6, 6, source), //
                new CloneSnippet("file2", "file2", 2, 2, 4, source), //
                new CloneSnippet("file2", "file2", 1, 6, 6, source) //
        }, true);
        List<CloneGroup> clones = Arrays.asList(cloneGroup, cloneGroup, cloneGroup);
        FreeMarker.saveClonesToHTML(htmlClonesFolderPath, repositoryB, "McSheep", clones);
        FreeMarker.saveClonesToHTML(htmlClonesFolderPath, repositoryB, "PMD", clones);

        BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testgetUniqueNames() throws IOException, TemplateException {
        String source = "public void x(){\na();\n\nb();\nc();\nd();\n}";
        CloneSnippet[] snippets = new CloneSnippet[] { //
                new CloneSnippet("file1", "file1", 2, 2, 4, source), //
                new CloneSnippet("file1", "file1", 1, 6, 6, source), //
                new CloneSnippet("file2", "file2", 2, 2, 4, source), //
                new CloneSnippet("file2", "file2", 1, 6, 6, source) //
        };
        List<CloneSnippet> names = FreeMarker.getUniqueNames(snippets);
        assertNotNull(names);
        assertEquals(2, names.size());
        assertEquals("file1", names.get(0).name);
        assertEquals("file2", names.get(1).name);
    }

    @Test
    public void testSaveIndex() throws IOException, TemplateException {
        BasicConfigurator.configure();
        FreeMarker.configure("target/classes/ftl");
        FreeMarker.saveIndex(htmlFolderPath);
        BasicConfigurator.resetConfiguration();
    }

}