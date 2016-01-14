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

import com.ampaiva.hlo.util.Helper;
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
        String source = Helper.convertFile2String(new File("target/test-classes/snippet/SalesDetailReportView.java"));
        ClonePair clonePair = new ClonePair(new CloneSide("file1", 1, 3, "public void x(){\na();\n\nb();\n}"),
                new CloneSide("file2", 46, 66, source), true);
        List<ClonePair> clones = Arrays.asList(clonePair);
        root.put("repository", repositories.get(0));
        root.put("clones", clones);
        Writer out2 = new OutputStreamWriter(new FileOutputStream(
                htmlFolder + File.separator + new File(repositories.get(0).getLocation()).getName() + ".html"));
        FreeMarker.run("clones.ftl", root, out2);
        out2.close();
        ClonePair clone = clones.get(0);
        root.put("clone", clone);
        root.put("copydiff", FreeMarker.format(clone.copy.source, clone.copy.beglin, clone.copy.endlin, true));
        root.put("pastediff", FreeMarker.format(clone.paste.source, clone.paste.beglin, clone.paste.endlin, true));
        root.put("copy", FreeMarker.format(clone.copy.source, clone.copy.beglin, clone.copy.endlin, false));
        root.put("paste", FreeMarker.format(clone.paste.source, clone.paste.beglin, clone.paste.endlin, false));
        Writer out3 = new OutputStreamWriter(new FileOutputStream(htmlFolder + File.separator
                + new File(repositories.get(0).getLocation()).getName() + "-" + clone + ".html"));
        FreeMarker.run("clone.ftl", root, out3);
        out3.close();
        BasicConfigurator.resetConfiguration();

    }

}