package com.android.presentation.app.entity;

/**
 * @auth 俊
 * Created by 俊 on 2016/8/20.
 */
public enum MountType {

    SDCARD(0), SATA(1), USB(2);
    /**
     * 类型
     */
    private int type = -1;

    MountType(int type) {
        this.type = type;
    }

    /**
     * 获取挂载设备类型
     *
     * @return
     */
    public int getType() {
        return type;
    }
}
