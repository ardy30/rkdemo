package com.android.presentation.app.entity;

import java.io.Serializable;

/**
 * @auth 俊
 * Created by 俊 on 2016/8/20.
 */
public class MountEntity implements Serializable {
    /**
     * 类型，取值为{@link MountType#SDCARD},{@link MountType#SATA},{@link MountType#USB}
     */
    public int type;
    /**
     * 路径
     */
    public String path;
    /**
     * 可用空间
     */
    public long usageSpace;
    /**
     * 已用空间
     */
    public long usedSpace;
    /**
     * 总空间
     */
    public long totalSpace;

}
