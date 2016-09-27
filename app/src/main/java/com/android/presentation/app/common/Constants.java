package com.android.presentation.app.common;

import android.os.Environment;

/**
 * Created by Jeremy on 2016/8/12.
 */
public class Constants {
	public static final int HDMI_1080P_60 = 16;
	public static final int HDMI_1080P_50 = 31;
	public static final int HDMI_720P_60 = 4;
	public static final int HDMI_720P_50 = 19;
	public static final int VGA_1280X720 = 3;
	public static final int VGA_1280X800 = 1;
	public static final int VGA_1440X900 = 0;
	public static final int VGA_1920X1080 = 2;
	public static final int COM_0 = 0;
	public static final int COM_TOP = 4;
	public static final int COM_BOTTOM = 1;
	public static final int COM_RJ45 = 3;

	/**
	 * 当前HDMI分辨率文件
	 */
	public static final String FILE_HDMI_RESOLUTION = "/data/hdmi_mode";
	/**
	 * 当前VGA分辨率文件
	 */
	public static final String FILE_VGA_RESOLUTION = "/data/vga_mode";
	/**
	 * 当前HDMI声音开关文件
	 */
	public static final String FILE_HDMI_OUTPUT_SWITCH = "/data/hdmi_on_off";
	/**
	 * 当前HDMI声音透传文件
	 */
	public static final String FILE_HDMI_OUTPUT_PASS_SWITCH = "/data/hdmi_pass_on_off";
	/**
	 * 当前SPDIF声音文件
	 */
	public static final String FILE_SPDIF_OUTPUT_SWITCH = "/data/spdif_on_off";
	/**
	 * 当前SPDIF声音透传文件
	 */
	public static final String FILE_SPDIF_OUTPUT_PASS_SWITCH = "/data/spdif_pass_on_off";
	/**
	 * 当前触摸屏串口文件
	 */
	public static final String FILE_TOUCH_PORT = "/data/COM.TXT";

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
	/**
	 * 电视家app的包名
	 */
	public static final String TV_PACKAGE = "com.elinkway.tvlive2";

}
