package com.ampaiva.metricsdatamanager.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.util.Helper;

public class ZipStreamUtil implements ICodeSource {
    private final String id;
    private final InputStream in;

    public ZipStreamUtil(InputStream in) {
        this(null, in);
    }

    public ZipStreamUtil(String id, InputStream in) {
        this.in = in;
        this.id = id;
    }

    @Override
    public Map<String, String> getCodeSource() throws IOException {
        Map<String, String> sources = new HashMap<String, String>();
        ZipInputStream zipFile = new ZipInputStream(in);
        ZipEntry entry;
        while ((entry = zipFile.getNextEntry()) != null) {
            if (entry.isDirectory() || !entry.getName().toLowerCase().endsWith(".java")) {
                continue;
            }
            sources.put(entry.getName().substring(entry.getName().indexOf('/') + 1),
                    Helper.convertInputStream2String(zipFile));
        }
        zipFile.close();
        return sources;
    }

    @Override
    public String toString() {
        return "ZipStreamUtil [id=" + id + "]";
    }
}
