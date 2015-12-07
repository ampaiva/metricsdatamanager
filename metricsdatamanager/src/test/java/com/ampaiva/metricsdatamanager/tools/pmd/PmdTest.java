package com.ampaiva.metricsdatamanager.tools.pmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.ampaiva.hlo.util.Helper;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd.PmdClone;
import com.ampaiva.metricsdatamanager.tools.pmd.Pmd.PmdClone.PmdOcurrency;

public class PmdTest {

    private static final String REPOSITORY = "c:\\Temp\\extracted";

    @Test
    public void testParse() throws IOException {
        String pmdResult = Helper.readFile(new File("src/test/resources/pmd/generic.csv"));

        List<PmdClone> clones = Pmd.parse(REPOSITORY, pmdResult);
        assertNotNull(clones);
        assertEquals(2, clones.size());
        final PmdClone clone0 = clones.get(0);
        assertEquals(19, clone0.lines);
        assertEquals(98, clone0.tokens);
        assertNotNull(clone0.ocurrencies);
        assertEquals(2, clone0.ocurrencies.size());
        final PmdOcurrency ocurrency0_0 = clone0.ocurrencies.get(0);
        assertNotNull(ocurrency0_0);
        assertEquals(13, ocurrency0_0.line);
        assertEquals("generic/target/CodeCloneType1.java", ocurrency0_0.file);
        final PmdOcurrency ocurrency0_1 = clone0.ocurrencies.get(1);
        assertNotNull(ocurrency0_1);
        assertEquals(13, ocurrency0_1.line);
        assertEquals("generic/target/OriginalCode.java", ocurrency0_1.file);
    }

}
