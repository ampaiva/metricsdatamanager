package com.ampaiva.metricsdatamanager;

import java.io.File;
import java.io.IOException;
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

public class Main {
    private static final Log LOG = LogFactory.getLog(Main.class);

    public static void main(String[] args) throws IOException, com.github.javaparser.ParseException {
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

        if (Boolean.parseBoolean(config.get("analysis.persist"))) {
            PersistDuplications persistDuplications = new PersistDuplications(dataManager,
                    Integer.parseInt(config.get("analysis.minseq")));
            persistDuplications.run(config.get("analysis.folder"),
                    Boolean.parseBoolean(config.get("analysis.searchzips")),
                    Boolean.parseBoolean(config.get("analysis.deleteall")));
        } else {
            String pmdCSVFile = config.get("pmd.csvfile");
            File csvFile = new File(pmdCSVFile);
            if (!csvFile.exists()) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("File " + csvFile + " does not exist.");
                }
                return;
            }
            ExtractClones extractClones = new ExtractClones(Integer.parseInt(config.get("analysis.minseq")));
            List<Repository> repositories = extractClones.run(config.get("analysis.folder"),
                    Boolean.parseBoolean(config.get("analysis.searchzips")));
            if (LOG.isInfoEnabled()) {
                for (Repository repository : repositories) {
                    LOG.info(repository);
                    CompareToolsNoDB compareTools = new CompareToolsNoDB();
                    String pmdResult = Helper.readFile(csvFile);
                    compareTools.comparePMDxMcSheep(repository, pmdResult);
                    compareTools.compareMcSheepxPMD(repository, pmdResult);

                    BasicConfigurator.resetConfiguration();

                }
            }
        }
        if (LOG.isInfoEnabled()) {
            Date end = new Date();
            LOG.info(start + " - " + end + " Elapsed " + end.compareTo(start));
        }
        BasicConfigurator.resetConfiguration();
    }

}
