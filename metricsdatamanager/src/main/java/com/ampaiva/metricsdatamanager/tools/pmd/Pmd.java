package com.ampaiva.metricsdatamanager.tools.pmd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.util.Conventions;

public class Pmd {
    public static List<PmdClone> parse(String repository, String pmdResult) throws IOException {
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
                String fileFullPath = values[3 + 2 * j + 1];
                PmdOccurrence ocurrency = new PmdOccurrence(pmdClone, Integer.parseInt(values[3 + 2 * j]),
                        Conventions.fileNameInRepository(repository, fileFullPath),
                        Helper.convertFile2String(new File(fileFullPath)));
                pmdClone.ocurrencies.add(ocurrency);
            }
            clones.add(pmdClone);
        }
        return clones;
    }
}
