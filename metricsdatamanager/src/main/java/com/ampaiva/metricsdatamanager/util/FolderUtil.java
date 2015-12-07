package com.ampaiva.metricsdatamanager.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ampaiva.hlo.cm.ICodeSource;
import com.ampaiva.hlo.util.Helper;

public class FolderUtil implements ICodeSource {
    private final String folder;

    public FolderUtil(String folder) {
        this.folder = folder;
    }

    @Override
    public Map<String, String> getCodeSource() throws IOException {
        Map<String, String> sources = new HashMap<String, String>();
        List<File> files = Helper.getFilesRecursevely(folder);
        for (File file : files) {
            if (file.isDirectory() || !file.getName().toLowerCase().endsWith(".java")) {
                continue;
            }
            sources.put(Conventions.fileNameInRepository(folder, file.getAbsolutePath()),
                    Helper.convertFile2String(file));
        }
        return sources;
    }

    @Override
    public String toString() {
        return "FolderUtil [folder=" + folder + "]";
    }
}
