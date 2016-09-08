package com.android.presentation.app.util;

import android.content.Context;
import android.content.Intent;

/**
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
}
