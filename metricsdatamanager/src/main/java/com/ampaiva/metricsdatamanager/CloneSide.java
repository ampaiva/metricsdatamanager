package com.ampaiva.metricsdatamanager;

public class CloneSide implements Comparable<CloneSide> {
    public final String name;

    public final int beglin;

    public final int endlin;

    public final String source;

    public CloneSide(String name, int beglin, int endlin, String source) {
        this.name = name;
        this.beglin = beglin;
        this.endlin = endlin;
        this.source = source;
    }

    @Override
    public int compareTo(CloneSide other) {
        int compare = name.compareTo(other.name);
        //        if (compare == 0) {
        //            if (compare == 0) {
        //                compare = beglin - other.beglin;
        //                if (compare == 0) {
        //                    compare = endlin - other.endlin;
        //                }
        //            }
        //        }
        return compare;
    }

    @Override
    public String toString() {
        return "CloneSide [name=" + name + ", beglin=" + beglin + ", endlin=" + endlin + "]";
    }
}
