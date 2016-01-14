package com.ampaiva.metricsdatamanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.controller.ConfigDataManager;
import com.ampaiva.metricsdatamanager.controller.IDataManager;
import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.model.Repository;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd.PmdClone;
import com.ampaiva.metricsdatamanager.util.Config;
import com.ampaiva.metricsdatamanager.util.FreeMarker;

import freemarker.template.TemplateException;

public class Main {
    private static final Log LOG = LogFactory.getLog(Main.class);

    private static void runPMD(String cpdFile, int minimumTokens, String projectFile, File csvFile) throws IOException {
        String[] strings = new String[] { "-classpath * net.sourceforge.pmd.cpd.CPD",
                "--minimum-tokens " + minimumTokens, " --files " + projectFile, " --format csv" };
        String command = cpdFile;
        for (String string : strings) {
            command += " " + string;
        }
        Process process = Runtime.getRuntime().exec(command, new String[0], new File("C:\\tools\\pmd-bin-5.4.0\\lib"));

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        FileWriter fileWriter = new FileWriter(csvFile);
        String line;
        while ((line = br.readLine()) != null) {
            fileWriter.write(line + "\r\n");
        }
        fileWriter.close();
    }

    public static void main(String[] args) throws IOException, com.github.javaparser.ParseException, TemplateException {
        // create the command line parser
        CommandLineParser parser = new DefaultParser();
        // create the Options
        Options options = new Options();
        Option optConfig = Option.builder("c").longOpt("config").argName("configuration file").hasArg()
                .desc("configuration file.").build();
        Option optHelp = Option.builder("h").longOpt("help").desc("help with application parameters.").build();
        options.addOption(optConfig);
        options.addOption(optHelp);
        // parse the command line arguments
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }

        if (line == null || line.hasOption(optHelp.getOpt()))

        {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Main", options);
            return;
        }

        // validate that block-size has been set
        String propertiesFile = "config.properties";
        if (line.hasOption(optConfig.getOpt()))

        {
            // print the value of block-size
            propertiesFile = line.getOptionValue(optConfig.getOpt());
        }

        Config config = new Config(propertiesFile);

        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        Date start = new Date();
        final IDataManager dataManager = new ConfigDataManager(config);

        String rootFolder = config.get("analysis.folder");
        if (Boolean.parseBoolean(config.get("analysis.persist"))) {
            PersistDuplications persistDuplications = new PersistDuplications(dataManager,
                    Integer.parseInt(config.get("analysis.minseq")));
            persistDuplications.run(rootFolder, Boolean.parseBoolean(config.get("analysis.searchzips")),
                    Boolean.parseBoolean(config.get("analysis.deleteall")));
        } else {
            File file = new File(rootFolder);
            FreeMarker.configure("target/classes/ftl");
            List<Repository> repositories2 = new ArrayList<>();
            for (File projectFile : file.listFiles()) {
                if (!projectFile.isDirectory()) {
                    continue;
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info("Project: " + projectFile);
                }
                String pmdCSVFile = config.get("pmd.results") + File.separator + projectFile.getName() + ".csv";
                File csvFile = new File(pmdCSVFile);
                if (csvFile.exists()) {
                    csvFile.delete();
                }
                csvFile.getParentFile().mkdirs();
                runPMD(config.get("pmd.cpdfile"), Integer.parseInt(config.get("pmd.minimumtokens")),
                        projectFile.getAbsolutePath(), csvFile);
                if (!csvFile.exists()) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("File " + csvFile + " does not exist.");
                    }
                    return;
                }
                ExtractClones extractClones = new ExtractClones(Integer.parseInt(config.get("analysis.minseq")));
                List<Repository> repositories = extractClones.run(projectFile.getAbsolutePath(),
                        Boolean.parseBoolean(config.get("analysis.searchzips")));
                repositories2.addAll(repositories);
                if (LOG.isInfoEnabled()) {
                    for (Repository repository : repositories) {
                        LOG.info(repository);
                        CompareToolsNoDB compareTools = new CompareToolsNoDB();
                        String pmdResult = Helper.readFile(csvFile);
                        List<PmdClone> pmdFound = new ArrayList<>();
                        List<PmdClone> pmdNotFound = new ArrayList<>();
                        compareTools.comparePMDxMcSheep(repository, pmdResult, pmdFound, pmdNotFound);
                        System.out.println("Found: " + pmdFound.size());
                        System.err.println();
                        System.err.println("Not found: " + pmdNotFound.size());
                        compareTools.saveClones(config.get("analysis.results"), "pmd-" + csvFile.getName(), pmdFound,
                                pmdNotFound);
                        List<Clone> mcsheepFound = new ArrayList<>();
                        List<Clone> mcsheepNotFound = new ArrayList<>();
                        compareTools.compareMcSheepxPMD(repository, pmdResult, mcsheepFound, mcsheepNotFound);
                        List<ClonePair> clones = compareTools.saveClones(config.get("analysis.results"),
                                "mcsheep-" + csvFile.getName(), mcsheepFound, mcsheepNotFound);
                        saveClonesToHTML(repository, clones);
                    }
                }
            }
            saveRepositoriesToHTML(repositories2);
        }
        if (LOG.isInfoEnabled()) {
            Date end = new Date();
            LOG.info(start + " - " + end + " Elapsed " + (end.getTime() - start.getTime()) + " ms");
        }
        BasicConfigurator.resetConfiguration();
    }

    private static void saveRepositoriesToHTML(List<Repository> repositories) throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();
        root.put("repositories", repositories);
        File htmlFolder = new File("/temp/html");
        htmlFolder.mkdirs();
        Writer out = new OutputStreamWriter(new FileOutputStream(htmlFolder + File.separator + "index.html"));
        FreeMarker.run("index.ftl", root, out);
        out.close();
    }

    private static void saveClonesToHTML(Repository repository, List<ClonePair> clones)
            throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();
        root.put("repository", repository);
        root.put("clones", clones);
        File htmlFolder = new File("/temp/html");
        Writer out2 = new OutputStreamWriter(new FileOutputStream(
                htmlFolder + File.separator + new File(repository.getLocation()).getName() + ".html"));
        FreeMarker.run("clones.ftl", root, out2);
        for (ClonePair clone : clones) {
            root.put("clone", clone);
            root.put("copydiff", FreeMarker.format(clone.copy.source, clone.copy.beglin, clone.copy.endlin, true));
            root.put("pastediff", FreeMarker.format(clone.paste.source, clone.paste.beglin, clone.paste.endlin, true));
            root.put("copy", FreeMarker.format(clone.copy.source, clone.copy.beglin, clone.copy.endlin, false));
            root.put("paste", FreeMarker.format(clone.paste.source, clone.paste.beglin, clone.paste.endlin, false));
            Writer out3 = new OutputStreamWriter(new FileOutputStream(htmlFolder + File.separator
                    + new File(repository.getLocation()).getName() + "-" + clone + ".html"));
            FreeMarker.run("clone.ftl", root, out3);
            out3.close();
        }
        out2.close();
    }
}
