package com.android.presentation.app.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.presentation.app.R;

public class ToastUtil {
	private static Toast toast;
	private static View view;
	private static TextView tvToast;

	/**
	 * 显示Toast消息并且关闭
	 *
	 * @param context
	 * @param toastText
	 * @param isShort
	 */
	@SuppressLint("InflateParams")
	public static void showToastAndCancel(Context context, String toastText,
			boolean isShort) {
		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.toast_view,
					null);
			tvToast = (TextView) view.findViewById(R.id.tv_toast_text);
		}
		if (toast == null) {
			toast = new Toast(context);
			tvToast.setText(toastText);
			toast.setView(view);
			toast.setGravity(Gravity.BOTTOM, 0, 100);
		} else {
			tvToast.setText(toastText);
			toast.setView(view);
			if (isShort) {
				toast.setDuration(Toast.LENGTH_SHORT);
			} else {
				toast.setDuration(Toast.LENGTH_LONG);
			}
		}
		toast.show();
	}

	/**
	 * 显示Toast消息并且关闭
	 *
	 * @param context
	 * @param res
	 * @param isShort
	 */
	@SuppressLint("InflateParams")
	public static void showToastAndCancel(Context context, int res,
			boolean isShort) {
		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.toast_view,
					null);
			tvToast = (TextView) view.findViewById(R.id.tv_toast_text);
		}
		if (toast == null) {
			toast = new Toast(context);
			tvToast.setText(res);
			toast.setView(view);
			toast.setGravity(Gravity.BOTTOM, 0, 100);
		} else {
			tvToast.setText(res);
			toast.setView(view);
			if (isShort) {
				toast.setDuration(Toast.LENGTH_SHORT);
			} else {
				toast.setDuration(Toast.LENGTH_LONG);
			}
		}
		toast.show();
	}

	/**
	 * 显示Toast消息并且关闭(短时间)
	 *
	 * @param context
	 * @param toastText
	 */
	public static void showToastAndCancel(Context context, String toastText) {
		showToastAndCancel(context, toastText, true);
	}

	/**
	 * 显示Toast消息并且关闭(短时间)
	 *
	 * @param context
	 * @param res
	 */
	public static void showToastAndCancel(Context context, int res) {
		showToastAndCancel(context, res, true);
	}

	/**
	 * 显示Toast消息并且关闭(长时间)
	 *
	 * @param context
	 * @param toastText
	 */
	public static void showLongTimeToastAndCancel(Context context,
			String toastText) {
		showToastAndCancel(context, toastText, false);
	}
}
