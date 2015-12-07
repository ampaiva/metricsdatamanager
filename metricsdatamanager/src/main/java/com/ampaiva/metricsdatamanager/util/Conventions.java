package com.ampaiva.metricsdatamanager.util;

public class Conventions {
    private static final String WINDOWS_SLASH = "\\";
    private static final String UNIX_SLASH = "/";
    private static final String EMPTY = "";

    public static String fileNameInRepository(String repository, String fileFullPath) {
        String fileNameConvetion = fileFullPath.replace(repository, EMPTY).replace(WINDOWS_SLASH, UNIX_SLASH);
        if (fileNameConvetion.startsWith(UNIX_SLASH)) {
            fileNameConvetion = fileNameConvetion.replaceFirst(UNIX_SLASH, EMPTY);
        }
        return fileNameConvetion;
    }
}
