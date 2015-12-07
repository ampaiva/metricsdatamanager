package com.ampaiva.metricsdatamanager.tools.pmd;

import java.util.ArrayList;
import java.util.List;

import com.ampaiva.metricsdatamanager.tools.pmd.Pmd.PmdClone.PmdOcurrency;

public class Pmd {
    public static class PmdClone {
        public static class PmdOcurrency {
            int line;
            String file;

            @Override
            public String toString() {
                return "PmdOcurrency [line=" + line + ", file=" + file + "]";
            }
        }

        int lines;
        int tokens;
        List<PmdOcurrency> ocurrencies;

        @Override
        public String toString() {
            return "PmdClone [lines=" + lines + ", tokens=" + tokens + ", ocurrencies=" + ocurrencies + "]";
        }
    }

    public static List<PmdClone> parse(String repository, String pmdResult) {
        List<PmdClone> clones = new ArrayList<>();
        String[] lines = pmdResult.replace("\r\n", "\n").split("\n");
        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(",");
            final PmdClone pmdClone = new PmdClone();
            pmdClone.lines = Integer.parseInt(values[0]);
            pmdClone.tokens = Integer.parseInt(values[1]);
            int ocurrences = Integer.parseInt(values[2]);
            pmdClone.ocurrencies = new ArrayList<>();
            for (int j = 0; j < ocurrences; j++) {
                PmdOcurrency ocurrency = new PmdOcurrency();
                ocurrency.line = Integer.parseInt(values[3 + 2 * j]);
                ocurrency.file = values[3 + 2 * j + 1].replace(repository, "");
                pmdClone.ocurrencies.add(ocurrency);
            }
            clones.add(pmdClone);
        }
        return clones;
    }
}