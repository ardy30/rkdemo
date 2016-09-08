package com.android.presentation.app.common;

import android.app.Application;
import android.view.WindowManager;

import com.android.presentation.app.holder.CrashHandler;

/**
 * Created by Jeremy on 2016/8/12.
 */
public class PresentationApplication extends Application {
    private WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }

    public WindowManager.LayoutParams getWindowParams() {
        return windowParams;
    }

    /**
     * 退出。
     */
    public void exitAPP() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
