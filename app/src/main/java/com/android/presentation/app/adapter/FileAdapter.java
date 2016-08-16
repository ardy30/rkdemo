package com.android.presentation.app.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.android.presentation.app.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class FileAdapter extends BaseAdapter {
	private Context mContext;
	private List<File> mFiles = new ArrayList<File>();
	private LayoutInflater mInflater = null;

	public FileAdapter(Context mContext, List<File> mFiles) {
		super();
		this.mContext = mContext;
		this.mFiles = mFiles;
		mInflater = LayoutInflater.from(mContext);
	}

	public void updateAdapter(List<File> files) {
		if (files != null && files.size() > 0) {
			this.mFiles = files;
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return mFiles.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mFiles.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		ViewHolder holder = null;
		if (arg1 == null) {
			arg1 = mInflater.inflate(R.layout.adapter_of_files, null);
			holder = new ViewHolder();
			holder.tvFileName = (TextView) arg1.findViewById(R.id.tv_file_name);
			arg1.setTag(holder);
		} else {
			holder = (ViewHolder) arg1.getTag();
		}
		holder.tvFileName.setText(mFiles.get(arg0).getAbsolutePath());
		return arg1;
	}

	private class ViewHolder {
		TextView tvFileName;
	}
}
