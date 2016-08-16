package com.android.presentation.app.common;

import android.os.Environment;

/**
 * Created by Jeremy on 2016/8/12.
 */
public class Constants {
    public static String SD_CARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String PATH_CRASH_ERROR = SD_CARD + "/TestDemoDir/";
    /**
     * HTTP服务器ip地址
     */
    public static String SERVER_IP = "http://192.168.1.251/";
    /**
     * HTTP播放地址前缀，使用时在后面加上媒体文件名字即可
     */
    public static String URL_PREFIX = SERVER_IP + "TEST/";

}
