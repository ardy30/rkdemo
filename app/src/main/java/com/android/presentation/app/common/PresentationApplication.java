package com.android.presentation.app.common;

import android.app.Application;

import com.android.presentation.app.holder.CrashHandler;

/**
 * Created by Jeremy on 2016/8/12.
 */
public class PresentationApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }

    /**
     * 退出。
     */
    public void exitAPP() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
