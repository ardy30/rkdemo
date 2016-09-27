package com.android.presentation.app.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * 工具类
 * Created by Jeremy on 2016/9/6.
 */
public class PresentationUtil {
	/**
	 * 是否显示下面的导航栏
	 *
	 * @param hideNaviBar 是否隐藏
	 */
	public static void hideNaviBar(Context context, boolean hideNaviBar) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.hideNaviBar");
		intent.putExtra("hide", hideNaviBar);
		context.sendBroadcast(intent);
	}

	/**
	 * 重启设备
	 */
	public static void rebootDevice(Context context) {
		boolean b = false;
		long time = 0;
		long currentTime = System.currentTimeMillis();
		while (!b) {
			time = System.currentTimeMillis() - currentTime;
			if (time > 1000)
				b = true;
		}
		System.out.println("reboot");
		Intent intent = new Intent();
		intent.setAction("android.com.ynh.reboot");
		context.sendBroadcast(intent);
	}

	/**
	 * OTA升级系统
	 */
	public static void updateSystemByOta(Context context) {
		Intent intent = new Intent(
				"softwinner.intent.action.autoupdate");
		intent.putExtra("path", "/mnt/sdcard/update.zip"); //路径可以更改
		context.startActivity(intent);
//		ChooseApkDiskPopupHolder holder = new ChooseApkDiskPopupHolder(this);
//		holder.showTonePopupWindow(mBtnOtaUpdate);
	}

	/**
	 * OTA升级软件
	 */
	public static void updateApkByOta(Context context) {
		// need to put your apk in /mnt/sdcard folder, then click this button for apk update
		Intent intent = new Intent();
		intent.setAction("com.ynh.update_apk");
		// abc.apk name
		intent.putExtra("apkname", "PresentationActivity.apk");
		// com.xxx.xxx is the package name of your apk
		intent.putExtra("packagename", "com.android.presentation.app");
		// com.xxx.xxx.xxx is the first activity name of your apk
		intent.putExtra("activityname",
				"com.android.presentation.app.PresentationActivity");
		context.sendBroadcast(intent);
	}

	/**
	 * 关机
	 */
	public static void powerOffDevice(Context context) {
		System.out.println("power off");
		Intent intent = new Intent();
		intent.setAction("android.com.ynh.power_off");
		context.sendBroadcast(intent);
	}

	/**
	 * 切换触摸屏串口
	 *
	 * @param i
	 */
	public static void switchTouchCom(Context context, int i) {
		Intent intent = new Intent("com.ynh.set_uart");
		intent.putExtra("port", i);
		context.sendBroadcast(intent);
	}

	/**
	 * 从文件中获取数据，只读第一行
	 *
	 * @param filePath 文件路径
	 * @return 读出来的数据
	 */
	public static int getIntegerValueFromFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists())
			return -1;
		try {
			BufferedReader i_stream = new BufferedReader(new FileReader(file));
			String des_str = i_stream.readLine().trim();
			i_stream.close();
			return Integer.parseInt(des_str);
		} catch (IOException e) {
			Log.e("IOException", e.getMessage());
			return -1;
		}
	}

	/**
	 * 从文件中获取数据，只读第一行
	 *
	 * @param filePath 文件路径
	 * @return 读出来的数据
	 */
	public static String getStringValueFromFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists())
			return null;
		try {
			BufferedReader i_stream = new BufferedReader(new FileReader(file));
			String des_str = i_stream.readLine().trim();
			i_stream.close();
			return des_str;
		} catch (IOException e) {
			Log.e("IOException", e.getMessage());
			return null;
		}
	}

	/**
	 * 切换HDMI透传
	 *
	 * @param context
	 * @param open    是否打开，true表示打开，false表示关闭
	 */
	public static void switchHdmiPass(Context context, boolean open) {
		Intent intent = new Intent();
		intent.setAction("com.ynh.set_hdmi_pass_on_off");
		if (open) {
			intent.putExtra("pass_on", 1);
			switchAudioMode(context, 2);
		} else {
			intent.putExtra("pass_on", 0);
			switchAudioMode(context, 0);
		}
		context.sendBroadcast(intent);
	}

	/**
	 * 切换spdif透传
	 *
	 * @param context
	 * @param open    是否打开，true表示打开，false表示关闭
	 */
	public static void switchSpdifPass(Context context, boolean open) {
		Intent intent = new Intent();
		intent.setAction("com.ynh.set_spdif_pass_on_off");
		if (open) {
			intent.putExtra("pass_on", 1);
			switchAudioMode(context, 1);
		} else {
			intent.putExtra("pass_on", 0);
			switchAudioMode(context, 0);
		}
		context.sendBroadcast(intent);
	}

	/**
	 * 切换音频输出模式
	 *
	 * @param outputMode 0: 默认输出 1:spdif源码输出 2:HDMI源码输出
	 */
	private static void switchAudioMode(Context context, int outputMode) {
		Intent intent = new Intent();
		intent.setAction("com.android.audio_mode");
		intent.putExtra("audio_mode", outputMode);
		context.sendBroadcast(intent);
	}
}
