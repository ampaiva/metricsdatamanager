package com.ampaiva.metricsdatamanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.ampaiva.hlo.util.Helper;

public class ZipStreamUtilTest {

    @Test
    public void testGetFilesInZip() throws IOException {
        ZipStreamUtil zipStreamUtil = new ZipStreamUtil(Helper.convertFile2InputStream(new File(
                "src/test/resources/com/ampaiva/metricsdatamanager/util/ZipTest1.zip")));
        Map<String, String> files = zipStreamUtil.getCodeSource();
        assertNotNull(files);
        assertEquals(2, files.size());
    }
}
