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
import com.ampaiva.metricsdatamanager.CloneGroup;
import com.ampaiva.metricsdatamanager.CloneSnippet;
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

    public static String format(List<CloneSnippet> fileSnippets, String source, int beglin, int endlin,
            boolean onlyDiff) {
        SourceHandler sourceHandler = new SourceHandler(source, beglin, 0, endlin, 0);
        String[] lines = sourceHandler.getLines();
        StringBuilder sb = new StringBuilder();
        int fileSnippetsIndex = 0;
        for (int line = 0; line < lines.length; line++) {
            boolean lineBetween = sourceHandler.isLineBetween(beglin, endlin, line + 1);
            if (onlyDiff && !lineBetween) {
                continue;
            }
            CloneSnippet currentSnippet = fileSnippets.get(fileSnippetsIndex);
            while (fileSnippetsIndex + 1 < fileSnippets.size() && currentSnippet.endlin < line + 1) {
                currentSnippet = fileSnippets.get(++fileSnippetsIndex);
            }
            lineBetween = currentSnippet.endlin < line + 1 ? false
                    : sourceHandler.isLineBetween(currentSnippet.beglin, currentSnippet.endlin, line + 1);
            sb.append("<font color=\"");
            sb.append(lineBetween ? "red" : "black");
            sb.append("\">");
            sb.append("<b>");
            for (int j = 0; j < (int) (4 - Math.log10(line + 2)); j++) {
                sb.append("&nbsp;");
            }
            String linex = lines[line];
            if (linex.length() > 100) {
                linex = linex.substring(0, 100) + "...";
            }
            String linestr = linex.replace("<", "&lt;").replace(">", "&gt;").replace(" ", "&nbsp;").replace("\t",
                    "&nbsp;&nbsp;&nbsp;&nbsp;");
            sb.append((line + 1) + ".</b>&nbsp;&nbsp;&nbsp;&nbsp;" + linestr + "</br>");
            sb.append("</font>");
        }
        return sb.toString();
    }

    public static String ToString(CloneGroup clone) {
        String name = clone.toId();
        if (name.length() >= 80) {
            name = name.substring(0, 80) + name.hashCode();
        }
        return name;
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
        Writer out = new OutputStreamWriter(new FileOutputStream(htmlFolder + File.separator + "clones.html"));
        FreeMarker.run("index.ftl", root, out);
        out.close();
        File cssFile = new File("target/classes/ftl/clones.css");
        File cssFolder = new File(htmlFolder.getAbsolutePath() + File.separator + "stylesheets");
        cssFolder.mkdirs();
        Files.copy(cssFile.toPath(),
                new File(cssFolder.getAbsolutePath() + File.separator + cssFile.getName()).toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    public static class FormatSnippet {
        String source;
    }

    public static List<String> getFormattedSource(CloneSnippet[] snippets, boolean onlyDiff) {
        List<String> list = new ArrayList<>();
        String filePath = snippets[0].key;
        String source = snippets[0].source;
        int beglin = snippets[0].beglin;
        int endlin = snippets[0].endlin;
        List<CloneSnippet> fileSnippets = new ArrayList<>();
        fileSnippets.add(snippets[0]);
        for (int i = 1; i < snippets.length; i++) {
            CloneSnippet snippet = snippets[i];
            if (!snippet.key.equals(filePath)) {
                list.add(format(fileSnippets, source, beglin, endlin, onlyDiff));

                filePath = snippet.key;
                source = snippet.source;
                beglin = snippet.beglin;
                fileSnippets.clear();
            }
            fileSnippets.add(snippet);
            endlin = snippet.endlin;
        }
        list.add(format(fileSnippets, source, beglin, endlin, onlyDiff));
        return list;

    }

    public static List<CloneSnippet> getUniqueNames(CloneSnippet[] snippets) {
        List<CloneSnippet> list = new ArrayList<>();
        String filePath = snippets[0].key;
        List<CloneSnippet> fileSnippets = new ArrayList<>();
        fileSnippets.add(snippets[0]);
        for (int i = 1; i < snippets.length; i++) {
            CloneSnippet snippet = snippets[i];
            if (!snippet.key.equals(filePath)) {
                list.add(snippet);

                filePath = snippet.key;
                fileSnippets.clear();
            }
            fileSnippets.add(snippet);
        }
        list.add(fileSnippets.get(0));
        return list;

    }

    public static void saveClonesToHTML(String htmlFolderPath, Repository repository, String tool,
            List<CloneGroup> clones) throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();
        root.put("repository", repository);
        root.put("tool", tool);
        List<String> clonesList = new ArrayList<>();
        for (CloneGroup clone : clones) {
            clonesList.add(FreeMarker.ToString(clone));
        }
        root.put("clones", clonesList);
        File htmlFolder = new File(htmlFolderPath + File.separator + new File(repository.getLocation()).getName());
        htmlFolder.mkdirs();
        Writer out2 = new OutputStreamWriter(new FileOutputStream(htmlFolder + File.separator + tool + ".html"));
        FreeMarker.run("clones.ftl", root, out2);
        File htmlToolFolder = new File(htmlFolder.getAbsolutePath() + File.separator + tool);
        htmlToolFolder.mkdirs();
        for (CloneGroup clone : clones) {
            root.put("clone", FreeMarker.ToString(clone));
            root.put("snippets", getUniqueNames(clone.snippets));
            root.put("formattedSnippet", getFormattedSource(clone.snippets, true));
            root.put("formattedSource", getFormattedSource(clone.snippets, false));
            String fileName = htmlToolFolder + File.separator + FreeMarker.ToString(clone) + ".html";
            Writer out3 = new OutputStreamWriter(new FileOutputStream(fileName));
            FreeMarker.run("clone.ftl", root, out3);
            out3.close();
        }
        out2.close();
    }

}
