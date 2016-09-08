package com.android.presentation.app.holder.popupwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.android.presentation.app.R;
import com.android.presentation.app.adapter.ChooseDiskInfoAdapter;
import com.android.presentation.app.entity.MountEntity;
import com.android.presentation.app.holder.DeviceManager;
import com.android.presentation.app.util.ToastUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class ChooseApkDiskPopupHolder implements OnItemClickListener,
        OnDismissListener {
    private Activity mActivity;
    private LayoutInflater mLayoutInflater;
    private PopupWindow mDiskPopupWindow;
    private View chooseDiskView;
    private ListView lvDisks;
    private ChooseDiskInfoAdapter mDiskInfoAdapter;
    private DeviceManager mDeviceManager;
    private List<MountEntity> mMountEntities;
    private MountEntity mMountEntity;
    private String mLocalRomPath;

    public ChooseApkDiskPopupHolder(Activity activity) {
        mActivity = activity;
        mDeviceManager = new DeviceManager(activity);
        initView();
    }

    @SuppressLint("InflateParams")
    private void initView() {
        mLayoutInflater = (LayoutInflater) mActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        chooseDiskView = mLayoutInflater.inflate(R.layout.choose_disk_view,
                null, false);
        lvDisks = (ListView) chooseDiskView.findViewById(R.id.lv_disks);
        mMountEntities = mDeviceManager.getAllMountedDevices();
        mDiskInfoAdapter = new ChooseDiskInfoAdapter(mMountEntities, mActivity);
        lvDisks.setAdapter(mDiskInfoAdapter);
        lvDisks.setOnItemClickListener(this);
    }

    public void showTonePopupWindow(View v) {
        if (mDeviceManager != null) {
            mMountEntities = mDeviceManager.getAllMountedDevices();
            mDiskInfoAdapter.updateAdapter(mMountEntities);
        }
        if (mDiskPopupWindow == null) {
            mDiskPopupWindow = new PopupWindow(chooseDiskView, 445, 222, true);
            mDiskPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mDiskPopupWindow
                    .setAnimationStyle(R.style.popup_anim_from_centre_style);
            mDiskPopupWindow.setOnDismissListener(this);
        }
        mDiskPopupWindow.showAtLocation((View) v.getParent().getParent(),
                Gravity.CENTER, 0, 0);
    }

    @Override
    public void onDismiss() {
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        mMountEntity = mMountEntities.get(arg2);
        mLocalRomPath = mMountEntity.path;
        // 本地更新apk
        File dir = new File(mLocalRomPath);
        File[] list = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isFile();
            }
        });
        boolean exist = false;
        if (list.length > 0) {
            for (File f :
                    list) {
                if (new File(f, "update.zip").exists()) {
                    exist = true;
                    ToastUtil.showToastAndCancel(mActivity, "即将更新ROM，稍后将重启！",
                            false);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateRom(f.getAbsolutePath());
                    break;
                }
            }
        }
        if (!exist) {
            ToastUtil.showToastAndCancel(mActivity, "没发现升级包，请检查后重试！",
                    true);
        }
        if (mDiskPopupWindow != null && mDiskPopupWindow.isShowing()) {
            mDiskPopupWindow.dismiss();
        }
    }

    /**
     * 升级ROM
     */
    private void updateRom(String path) {
        Intent intent = new Intent("softwinner.intent.action.autoupdate");
        intent.putExtra("path", path + "/update.zip"); //路径可以更改
        mActivity.startActivity(intent);
    }
}
