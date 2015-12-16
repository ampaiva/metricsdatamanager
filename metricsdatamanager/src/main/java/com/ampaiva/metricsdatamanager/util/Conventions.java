package com.ampaiva.metricsdatamanager.util;

public class Conventions {
    private static final String WINDOWS_SLASH = "\\";
    private static final String UNIX_SLASH = "/";
    private static final String EMPTY = "";

    public static String fileNameInRepository(String repository, String fileFullPath) {
        String fileNameConvention = fileFullPath.replace(WINDOWS_SLASH, UNIX_SLASH)
                .replace(repository.replace(WINDOWS_SLASH, UNIX_SLASH), EMPTY);
        if (fileNameConvention.startsWith(UNIX_SLASH)) {
            fileNameConvention = fileNameConvention.replaceFirst(UNIX_SLASH, EMPTY);
        }
        return fileNameConvention;
    }
}
