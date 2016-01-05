package com.ampaiva.metricsdatamanager.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import com.ampaiva.metricsdatamanager.ClonePair;
import com.ampaiva.metricsdatamanager.CloneSide;
import com.ampaiva.metricsdatamanager.model.Repository;

import freemarker.template.TemplateException;

public class FreeMarkerTest {

    @Test
    public void testRun() throws IOException, TemplateException {
        BasicConfigurator.configure();
        Repository repositoryA = new Repository();
        repositoryA.setLocation("\\B\\A");
        List<Repository> repositories = Arrays.asList(repositoryA);
        Map<String, Object> root = new HashMap<>();
        root.put("repositories", repositories);
        FreeMarker.configure("target/classes/ftl");
        File htmlFolder = new File("/temp/html");
        htmlFolder.mkdirs();
        Writer out = new OutputStreamWriter(new FileOutputStream(htmlFolder + File.separator + "index.html"));
        FreeMarker.run("index.ftl", root, out);
        out.close();
        ClonePair clonePair = new ClonePair(new CloneSide("file1", 11, 12), new CloneSide("file2", 15, 16), true);
        List<ClonePair> clones = Arrays.asList(clonePair);
        root.put("repository", repositories.get(0));
        root.put("clones", clones);
        Writer out2 = new OutputStreamWriter(new FileOutputStream(
                htmlFolder + File.separator + new File(repositories.get(0).getLocation()).getName() + ".html"));
        FreeMarker.run("clones.ftl", root, out2);
        out2.close();
        root.put("clone", clones.get(0));
        root.put("copy", "public void x();");
        root.put("paste", "public void y();");
        Writer out3 = new OutputStreamWriter(new FileOutputStream(htmlFolder + File.separator
                + new File(repositories.get(0).getLocation()).getName() + "-" + clones.get(0) + ".html"));
        FreeMarker.run("clone.ftl", root, out3);
        out3.close();
        BasicConfigurator.resetConfiguration();

    }

}