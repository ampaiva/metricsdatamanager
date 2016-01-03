package com.ampaiva.metricsdatamanager;

public class CloneSide implements Comparable<CloneSide> {
    final String name;

    final int beglin;

    final int endlin;

    public CloneSide(String name, int beglin, int endlin) {
        this.name = name;
        this.beglin = beglin;
        this.endlin = endlin;
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
