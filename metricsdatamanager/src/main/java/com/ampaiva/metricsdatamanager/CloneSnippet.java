package com.ampaiva.metricsdatamanager;

import java.io.File;

public class CloneSnippet implements Comparable<CloneSnippet> {
    public static final String ID_SEPARATOR = "-";

    public final String name;

    public final int beglin;

    public final int endlin;

    public final String source;

    public CloneSnippet(String name, int beglin, int endlin, String source) {
        this.name = name;
        this.beglin = beglin;
        this.endlin = endlin;
        this.source = source;
    }

    @Override
    public int compareTo(CloneSnippet other) {
        int compare = name.compareTo(other.name);
        if (compare == 0) {
            compare = beglin - other.beglin;
            if (compare == 0) {
                compare = endlin - other.endlin;
            }
        }
        return compare;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return getFileName(name);
    }

    private static String getFileName(String filePath) {
        return new File(filePath).getName().replaceFirst("(.*)\\.java", "$1");
    }

    public String toId() {
        return getFileName(name) + ID_SEPARATOR + beglin + ID_SEPARATOR + endlin;
    }

    @Override
    public String toString() {
        return "CloneSide [name=" + name + ", beglin=" + beglin + ", endlin=" + endlin + "]";
    }
}
