package com.android.presentation.app.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class ToolUtil {
    /**
     * 全盘扫描
     *
     * @param path
     * @return
     */
    public static List<File> scanAllVideo(String path) {
        File f = new File(path);
        List<File> files = new ArrayList<File>();
        File[] fileList = f.listFiles(new FileFilter() {

            @Override
            public boolean accept(File arg0) {
                return (arg0.getAbsolutePath().endsWith(".yc")
                        || arg0.getAbsolutePath().endsWith(".mp4")
                        || arg0.getAbsolutePath().endsWith(".mpg")
                        || arg0.getAbsolutePath().endsWith(".mkv") || arg0
                        .isDirectory());
            }
        });
        for (File file : fileList) {
            if (file.isFile())
                files.add(file);
            else
                files.addAll(scanAllVideo(file.getAbsolutePath()));
        }
        return files;
    }
}
