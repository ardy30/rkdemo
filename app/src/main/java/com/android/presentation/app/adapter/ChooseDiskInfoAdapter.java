package com.android.presentation.app.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.presentation.app.R;
import com.android.presentation.app.entity.MountEntity;
import com.android.presentation.app.entity.MountType;

public class ChooseDiskInfoAdapter extends BaseAdapter {
    private List<MountEntity> mMountEntities;
    private MountEntity mMountEntity;
    private Context mContext;

    public ChooseDiskInfoAdapter(List<MountEntity> mMountEntities,
                                 Activity mContext) {
        super();
        this.mMountEntities = mMountEntities;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mMountEntities.size();
    }

    @Override
    public Object getItem(int position) {
        return mMountEntities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        mMountEntity = mMountEntities.get(position);
        LayoutInflater inflater = null;
        ViewHolder holder = null;
        if (convertView == null) {
            inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.setting_choose_disk_item,
                    null);
            holder = new ViewHolder();
            holder.tvDiskName = (TextView) convertView
                    .findViewById(R.id.tv_disk_remind);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mMountEntity.type == MountType.SATA.getType()) {
            // 硬盘
            holder.tvDiskName.setText("硬盘");
        } else if (mMountEntity.type == MountType.USB.getType()) {
            // U盘
            holder.tvDiskName.setText("USB");
        } else if (mMountEntity.type == MountType.SDCARD.getType()) {
            // 内存卡
            holder.tvDiskName.setText("内部存储");
        } else {
            // 未知
            holder.tvDiskName.setText("未知");
        }
        convertView.setEnabled(false);
        return convertView;
    }

    class ViewHolder {
        TextView tvDiskName;
    }

    /**
     * 更新适配器
     *
     * @param mountEntities
     */
    public void updateAdapter(List<MountEntity> mountEntities) {
        this.mMountEntities = mountEntities;
        notifyDataSetChanged();
    }
}
