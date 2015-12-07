package com.ampaiva.metricsdatamanager.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConventionsTest {

    @Test
    public void testFileNameInRepository() {
        assertEquals("generic/Class.java",
                Conventions.fileNameInRepository("c:\\Temp", "c:\\Temp\\generic\\Class.java"));
    }

}
