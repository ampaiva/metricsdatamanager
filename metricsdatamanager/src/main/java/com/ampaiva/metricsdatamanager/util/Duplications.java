package com.ampaiva.metricsdatamanager.util;

import java.util.List;

public class Duplications {
    final List<List<Integer>> duplications;
    private int index = 0;

    public Duplications(List<List<Integer>> duplications) {
        this.duplications = duplications;
    }

    public DuplicationInfo next() {
        if (index >= duplications.size()) {
            return null;
        }
        int count = 1;
        int position0 = duplications.get(index).get(0);
        int position1 = duplications.get(index).get(1);
        index++;
        while (index < duplications.size() && duplications.get(index).get(0) == (duplications.get(index - 1).get(0) + 1)
                && duplications.get(index).get(1) == (duplications.get(index - 1).get(1) + 1)) {
            count++;
            index++;
        }
        return new DuplicationInfo(count, position0, position1);
    }
}
