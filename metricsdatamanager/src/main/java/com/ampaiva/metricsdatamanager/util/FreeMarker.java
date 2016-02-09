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

    private static File[] getFoldersMatching(String folderPath, final String regEx) {
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (!pathname.isDirectory()) {
                    return false;
                }
                return pathname.getName().matches(regEx);
            }
        };
        return new File(folderPath).listFiles(filter);
    }

    private static File[] getFilesMatching(String folderPath, final String regEx) {
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (!pathname.isFile()) {
                    return false;
                }
                return pathname.getName().matches(regEx);
            }
        };
        return new File(folderPath).listFiles(filter);
    }

    private static File[] getResultFolders(String htmlFolderPath) throws IOException, TemplateException {
        return getFoldersMatching(htmlFolderPath, "\\d+-\\d+");
    }

    private static int countClones(File resultFolder, File repositoryFolder, int tool, boolean positives) {
        File folder = getFolder(resultFolder, repositoryFolder.getName());
        if (folder == null) {
            return -1;
        }
        File toolFolder = getFolder(folder, (tool == 0) ? "McSheep" : "PMD");
        if (toolFolder == null) {
            return -1;
        }
        return getFilesMatching(toolFolder.getAbsolutePath(), "^" + (positives ? "\\+" : "-") + ".*").length;
    }

    private static File getFolder(File rootFoolder, String name) {
        File[] folders = getFoldersMatching(rootFoolder.getAbsolutePath(), name);
        if (folders.length == 1) {
            return folders[0];
        }
        return null;
    }

    public static void saveIndex(String htmlFolderPath) throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();
        File htmlFolder = new File(htmlFolderPath);
        FileFilter filterCloneFolders = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (!pathname.isDirectory()) {
                    return false;
                }
                if (pathname.listFiles().length != 8) {
                    return false;
                }
                return true;
            }
        };
        File[] resultFolders = getResultFolders(htmlFolderPath);
        root.put("resultfolders", resultFolders);
        String[] tools = new String[] { "McSheep", "PMD" };
        root.put("tools", tools);
        List<File> repositoriesList = new ArrayList<>();
        for (File resultFolder : resultFolders) {
            File[] repositoryFolder = resultFolder.listFiles(filterCloneFolders);
            for (File file : repositoryFolder) {
                boolean found = false;
                for (File file2 : repositoriesList) {
                    if (file2.getName().equals(file.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    repositoriesList.add(file);
                }
            }
        }
        root.put("repositories", repositoriesList);
        int[][] values = new int[repositoriesList.size()][];
        for (int i = 0; i < values.length; i++) {
            values[i] = new int[resultFolders.length * 2 * 2];
            for (int j = 0; j < values[i].length; j++) {
                values[i][j] = countClones(resultFolders[(j % (resultFolders.length * 2)) / 2], repositoriesList.get(i),
                        j / (resultFolders.length * 2), j % 2 == 0);
            }
        }
        root.put("values", values);
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
        CloneSnippet current = snippets[0];
        for (int i = 1; i < snippets.length; i++) {
            CloneSnippet snippet = snippets[i];
            if (!snippet.key.equals(current.key)) {
                list.add(current);

                current = snippet;
            }
        }
        list.add(current);
        return list;
    }

    public static void saveClonesToHTML(String htmlFolderPath, Repository repository, String tool,
            List<CloneGroup> clones) throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();
        root.put("repository", repository);
        root.put("tool", tool);
        File htmlFolder = new File(htmlFolderPath + File.separator + new File(repository.getLocation()).getName());
        htmlFolder.mkdirs();
        for (int i = 0; i <= 2; i++) {
            List<String> clonesList = new ArrayList<>();
            for (CloneGroup cloneGroup : clones) {
                if ((i == 1 && !cloneGroup.found) || (i == 2 && cloneGroup.found)) {
                    continue;
                }
                clonesList.add(FreeMarker.ToString(cloneGroup));
            }
            root.put("clones", clonesList);

            Writer out2 = new OutputStreamWriter(new FileOutputStream(
                    htmlFolder + File.separator + tool + (i == 1 ? "+" : i == 2 ? "-" : "") + ".html"));
            FreeMarker.run("clones.ftl", root, out2);
            File htmlToolFolder = new File(htmlFolder.getAbsolutePath() + File.separator + tool);
            htmlToolFolder.mkdirs();
            for (CloneGroup cloneGroup : clones) {
                if ((i == 1 && !cloneGroup.found) || (i == 2 && cloneGroup.found)) {
                    continue;
                }
                root.put("clone", FreeMarker.ToString(cloneGroup));
                root.put("snippets", getUniqueNames(cloneGroup.snippets));
                root.put("formattedSnippet", getFormattedSource(cloneGroup.snippets, true));
                root.put("formattedSource", getFormattedSource(cloneGroup.snippets, false));
                String fileName = htmlToolFolder + File.separator + FreeMarker.ToString(cloneGroup) + ".html";
                Writer out3 = new OutputStreamWriter(new FileOutputStream(fileName));
                FreeMarker.run("clone.ftl", root, out3);
                out3.close();
            }
            out2.close();
        }
    }

}
