package com.android.presentation.app.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

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
                        || arg0.getAbsolutePath().endsWith(".mkv")
                        || arg0.getAbsolutePath().endsWith(".iso")|| arg0
                        .isDirectory());
            }
        });
        if (fileList != null)
            for (File file : fileList) {
                if (file.isFile())
                    files.add(file);
                else
                    files.addAll(scanAllVideo(file.getAbsolutePath()));
            }
        return files;
    }

    /**
     * 防止过快二次点击
     */
    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 300) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 打开第三方软件
     *
     * @param context
     * @param packageName
     */
    public static void openThirdApplication(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
        if (packageInfo != null) {
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(packageInfo.packageName);
            List<ResolveInfo> apps = packageManager.queryIntentActivities(
                    resolveIntent, 0);
            ResolveInfo resolveInfo = apps.iterator().next();
            if (resolveInfo != null) {
                String className = resolveInfo.activityInfo.name;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName(packageName, className);
                intent.setComponent(cn);
                context.startActivity(intent);
            }
        }
    }
}
