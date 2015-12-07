package com.ampaiva.metricsdatamanager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd.PmdClone;

public class CompareTools {

    public CompareTools() {

    }

    public void compare(String repository, String pmdResult) {
        List<PmdClone> pmdClones = Pmd.parse(repository, pmdResult);
        for (int i = 0; i < pmdClones.size(); i++) {
            System.out.println(pmdClones.get(i));
        }
    }

    public static void main(String[] args) throws IOException {
        CompareTools compareTools = new CompareTools();
        String pmdResult = Helper.readFile(new File("src/test/resources/pmd/generic.csv"));
        compareTools.compare("c:\\Temp\\generic\\generic\\src\\main\\java\\target\\", pmdResult);
    }

}
