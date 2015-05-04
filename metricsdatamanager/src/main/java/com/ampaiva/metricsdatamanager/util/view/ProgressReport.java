package com.ampaiva.metricsdatamanager.util.view;

public class ProgressReport implements IProgressReport {
    public static final int UNLIMITED = -1;
    private final int maxLevel;

    public ProgressReport(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public ProgressReport() {
        this(UNLIMITED);
    }

    private String getIdent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("   ");
        }
        return sb.toString();
    }

    @Override
    public void onChanged(Phase phase, String id, int index, int size, int level) {
        if (maxLevel == UNLIMITED || level <= maxLevel) {
            print(getIdent(level) + phase + " " + id + " " + index + "/" + size);
        }
    }

    public void print(String text) {
        System.out.println(text);
    }
}
