package com.ampaiva.metricsdatamanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

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
import com.ampaiva.metricsdatamanager.model.Repository;
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
        String[] minSeqs = config.get("analysis.minseq").split(",");
        String[] totSeqs = config.get("analysis.totseq").split(",");
        String htmlFolderPath = config.get("html.folderpath");
        if (minSeqs.length != totSeqs.length) {
            throw new IllegalArgumentException("analysis.minseq should have the same size of analysis.totseq");
        }
        String pmdResultsFolder = config.get("pmd.results");
        File file = new File(rootFolder);
        FreeMarker.configure("target/classes/ftl");
        for (File projectFile : file.listFiles()) {
            if (!projectFile.isDirectory()) {
                continue;
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Project: " + projectFile);
            }
            File csvFile = runPMD(pmdResultsFolder, config.get("pmd.cpdfile"),
                    Integer.parseInt(config.get("pmd.minimumtokens")), projectFile);
            for (int i = 0; i < minSeqs.length; i++) {
                int minSeq = Integer.parseInt(minSeqs[i]);
                int totSeq = Integer.parseInt(totSeqs[i]);
                extractClones(config, htmlFolderPath, dataManager, rootFolder, minSeq, totSeq, csvFile, projectFile);
            }
        }
        FreeMarker.saveIndex(htmlFolderPath);
        if (LOG.isInfoEnabled()) {
            Date end = new Date();
            LOG.info(start + " - " + end + " Elapsed " + (end.getTime() - start.getTime()) + " ms");
        }
        BasicConfigurator.resetConfiguration();
    }

    private static void extractClones(Config config, String htmlFolderPath, final IDataManager dataManager,
            String rootFolder, int minSeq, int totSeq, File csvFile, File projectFile)
                    throws IOException, com.github.javaparser.ParseException, TemplateException {
        String appendTotMin = File.separator + totSeq + "-" + minSeq;
        String resultsFolder = config.get("analysis.results") + appendTotMin;
        String htmlClonesFolderPath = htmlFolderPath + appendTotMin;
        if (Boolean.parseBoolean(config.get("analysis.persist"))) {
            PersistDuplications persistDuplications = new PersistDuplications(dataManager, minSeq, totSeq);
            persistDuplications.run(rootFolder, Boolean.parseBoolean(config.get("analysis.searchzips")),
                    Boolean.parseBoolean(config.get("analysis.deleteall")));
        } else {
            ExtractClones extractClones = new ExtractClones(minSeq, totSeq);
            List<Repository> repositories = extractClones.run(projectFile.getAbsolutePath(),
                    Boolean.parseBoolean(config.get("analysis.searchzips")));
            if (LOG.isInfoEnabled()) {
                for (Repository repository : repositories) {
                    LOG.info(repository);
                    CompareToolsNoDB compareTools = new CompareToolsNoDB();
                    String pmdResult = Helper.readFile(csvFile);
                    List<CloneGroup> clonesPMD = compareTools.comparePMDxMcSheep(repository, pmdResult);
                    compareTools.saveClones(resultsFolder, "pmd-" + csvFile.getName(), clonesPMD);
                    List<CloneGroup> clonesMcSheep = compareTools.compareMcSheepxPMD(repository, pmdResult);
                    compareTools.saveClones(resultsFolder, "mcsheep-" + csvFile.getName(), clonesMcSheep);
                    FreeMarker.saveClonesToHTML(htmlClonesFolderPath, repository, "McSheep", clonesMcSheep);
                    FreeMarker.saveClonesToHTML(htmlClonesFolderPath, repository, "PMD", clonesPMD);
                }
            }
        }
    }

    private static File runPMD(String pmdResultsFolder, String cpdFile, int minimumTokens, File projectFile)
            throws IOException {
        String pmdCSVFile = pmdResultsFolder + File.separator + String.valueOf(minimumTokens) + File.separator
                + projectFile.getName() + ".csv";

        File csvFile = new File(pmdCSVFile);
        if (csvFile.exists()) {
            csvFile.delete();
        }
        csvFile.getParentFile().mkdirs();
        runPMD(cpdFile, minimumTokens, projectFile.getAbsolutePath(), csvFile);
        if (!csvFile.exists()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("File " + csvFile + " does not exist.");
            }
            return null;
        }
        return csvFile;
    }
}
