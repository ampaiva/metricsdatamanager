package com.ampaiva.metricsdatamanager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    Properties serverProperties;

    public Config(String propertiesFile) throws IOException {
        serverProperties = loadserverProperties(propertiesFile);
    }

    private Properties loadserverProperties(String propertiesFile) throws IOException {
        Properties properties = new Properties();
        InputStream stream = new FileInputStream(new File(propertiesFile));
        properties.load(stream);
        return properties;
    }

    public String get(String key) {
        return serverProperties.getProperty(key);
    }
}
