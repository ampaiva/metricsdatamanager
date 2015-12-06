package com.ampaiva.metricsdatamanager;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ampaiva.metricsdatamanager.controller.ConfigDataManager;
import com.ampaiva.metricsdatamanager.controller.IDataManager;
import com.ampaiva.metricsdatamanager.util.Config;

public class Main {

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
        final IDataManager dataManager = new ConfigDataManager(config);

        PersistDuplications persistDuplications = new PersistDuplications(dataManager,
                Integer.parseInt(config.get("analysis.minseq")));
        persistDuplications.run(config.get("analysis.folder"), Boolean.parseBoolean(config.get("analysis.searchzips")),
                Boolean.parseBoolean(config.get("analysis.deleteall")));
        BasicConfigurator.resetConfiguration();
    }

}
