package com.ampaiva.metricsdatamanager.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ampaiva.hlo.util.SourceHandler;
import com.ampaiva.metricsdatamanager.ClonePair;
import com.ampaiva.metricsdatamanager.model.Repository;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class FreeMarker {
    private static Configuration cfg;

    public static void configure(String templatesFolder) throws IOException {
        /*
         * -----------------------------------------------------------
         * -------------
         */
        /*
         * You should do this ONLY ONCE in the whole application
         * life-cycle:
         */

        /* Create and adjust the configuration singleton */
        cfg = new Configuration(Configuration.VERSION_2_3_22);
        cfg.setDirectoryForTemplateLoading(new File(templatesFolder));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public static void run(String template, Object root, Writer out) throws IOException, TemplateException {

        /*
         * -----------------------------------------------------------
         * -------------
         */
        /*
         * You usually do these for MULTIPLE TIMES in the application
         * life-cycle:
         */

        /* Get the template (uses cache internally) */
        Template temp = cfg.getTemplate(template);

        /* Merge data-model with template */
        temp.process(root, out);
        // Note: Depending on what `out` is, you may need to call `out.close()`.
        // This is usually the case for file output, but not for servlet output.
    }

    public static String format(String source, int beglin, int endlin, boolean onlyDiff) {
        SourceHandler sourceHandler = new SourceHandler(source, beglin, 0, endlin, 0);
        String[] lines = sourceHandler.getLines();
        StringBuilder sb = new StringBuilder();
        for (int line = 0; line < lines.length; line++) {
            boolean lineBetween = sourceHandler.isLineBetween(beglin, endlin, line + 1);
            if (onlyDiff && !lineBetween) {
                continue;
            }
            sb.append("<font color=\"");
            sb.append(lineBetween ? "red" : "black");
            sb.append("\">");
            sb.append("<b>");
            for (int j = 0; j < (int) (4 - Math.log10(line + 2)); j++) {
                sb.append("&nbsp;");
            }
            sb.append((line + 1) + ".</b>&nbsp;&nbsp;&nbsp;&nbsp;"
                    + lines[line].replace(" ", "&nbsp;").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;") + "</br>");
            sb.append("</font>");
        }
        return sb.toString();
    }

    public static String ToString(ClonePair clone) {
        return String.format("%s-%d-%d-%s-%d-%d-%s", clone.copy.name, clone.copy.beglin, clone.copy.endlin,
                clone.paste.name, clone.paste.beglin, clone.paste.endlin, clone.found);
    }

    public static void saveIndex(String htmlFolderPath) throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();
        File htmlFolder = new File(htmlFolderPath);
        htmlFolder.mkdirs();
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (!pathname.isDirectory()) {
                    return false;
                }
                if (pathname.listFiles().length != 4) {
                    return false;
                }
                return true;
            }
        };
        root.put("repositories", htmlFolder.listFiles(filter));
        Writer out = new OutputStreamWriter(new FileOutputStream(htmlFolder + File.separator + "index.html"));
        FreeMarker.run("index.ftl", root, out);
        out.close();
        File cssFile = new File("target/classes/ftl/clones.css");
        File cssFolder = new File(htmlFolder.getAbsolutePath() + File.separator + "stylesheets");
        cssFolder.mkdirs();
        Files.copy(cssFile.toPath(),
                new File(cssFolder.getAbsolutePath() + File.separator + cssFile.getName()).toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    public static void saveClonesToHTML(String htmlFolderPath, Repository repository, String tool,
            List<ClonePair> clones) throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();
        root.put("repository", repository);
        root.put("tool", tool);
        List<String> clonesList = new ArrayList<>();
        for (ClonePair clone : clones) {
            clonesList.add(FreeMarker.ToString(clone));
        }
        root.put("clones", clonesList);
        File htmlFolder = new File(htmlFolderPath + File.separator + new File(repository.getLocation()).getName());
        htmlFolder.mkdirs();
        Writer out2 = new OutputStreamWriter(new FileOutputStream(htmlFolder + File.separator + tool + ".html"));
        FreeMarker.run("clones.ftl", root, out2);
        File htmlToolFolder = new File(htmlFolder.getAbsolutePath() + File.separator + tool);
        htmlToolFolder.mkdirs();
        for (ClonePair clone : clones) {
            root.put("clone", FreeMarker.ToString(clone));
            root.put("copydiff", FreeMarker.format(clone.copy.source, clone.copy.beglin, clone.copy.endlin, true));
            root.put("pastediff", FreeMarker.format(clone.paste.source, clone.paste.beglin, clone.paste.endlin, true));
            root.put("copy", FreeMarker.format(clone.copy.source, clone.copy.beglin, clone.copy.endlin, false));
            root.put("paste", FreeMarker.format(clone.paste.source, clone.paste.beglin, clone.paste.endlin, false));
            Writer out3 = new OutputStreamWriter(
                    new FileOutputStream(htmlToolFolder + File.separator + FreeMarker.ToString(clone) + ".html"));
            FreeMarker.run("clone.ftl", root, out3);
            out3.close();
        }
        out2.close();
    }

}
