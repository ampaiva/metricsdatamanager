package com.ampaiva.metricsdatamanager.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.util.Helper;

public class ZipUtil implements ICodeSource {
    private final String zipFilePath;

    public ZipUtil(String zipFilePath) {
        this.zipFilePath = zipFilePath;
    }

    @Override
    public Map<String, String> getCodeSource() throws IOException {
        Map<String, String> sources = new HashMap<String, String>();
        ZipFile zipFile = new ZipFile(zipFilePath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory() || !entry.getName().toLowerCase().endsWith(".java")) {
                continue;
            }
            sources.put(entry.getName().substring(entry.getName().indexOf('/') + 1),
                    Helper.convertInputStream2String(zipFile.getInputStream(entry)));
        }
        zipFile.close();
        return sources;
    }
}
