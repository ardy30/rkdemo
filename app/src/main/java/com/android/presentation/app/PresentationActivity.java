package com.android.presentation.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.android.presentation.app.adapter.FileAdapter;
import com.android.presentation.app.common.Constants;
import com.android.presentation.app.common.PresentationApplication;
import com.android.presentation.app.dialog.MenuDialog;
import com.android.presentation.app.dialog.NfsDialog;
import com.android.presentation.app.dialog.PreviewDialog;
import com.android.presentation.app.entity.MountEntity;
import com.android.presentation.app.holder.DeviceManager;
import com.android.presentation.app.holder.ExtAudioRecorder;
import com.android.presentation.app.holder.HdmiPresentation;
import com.android.presentation.app.util.PresentationUtil;
import com.android.presentation.app.util.ToastUtil;
import com.android.presentation.app.util.ToolUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 *
 */
public class PresentationActivity extends Activity implements
		OnCheckedChangeListener, View.OnClickListener {
	private final String TAG = "PresentationActivity";
	public static PresentationActivity mPresentationActivity;
	private PresentationApplication mApplication;
	private int mChannelValue = 0, mAudioModeValue = 0, mHdmiModeValue = 16,
			mVgaModeValue = 0;
	private int trackNum = 0;
	private DisplayManager mDisplayManager;
	private DisplayListAdapter mDisplayListAdapter;
	private ListView mListView;
	private Button btnScan;
	/**
	 * 第一列的按钮
	 */
	private Button mBtnPlay, mBtnPause, mBtnSwitchTrack, mBtnNext, mBtnVolumeAdd,
			mBtnVolumeReduce, mBtnIncreaseSound, mBtnDecreaseSound, mBtnChannel, mBtnMenu, mBtnNfs;
	/**
	 * 第二列的按钮
	 */
	private Button mBtnHdmiSwitch, mBtnSpdifSwitch, mBtnLinein, mBtnDuplicateScreen,
			mBtnPreview, mBtnFullScreen;
	/**
	 * 第三列的按钮
	 */
	private Button mBtnHdmiMode, mBtnVgaMode, mBtnGetHdmiResolution,
			mBtnGetVgaResolution, mBtnComTop, mBtnComBottom, mBtnComRJ, mBtnGetTouchPort;
	/**
	 * 第四列的按钮
	 */
	private Button mBtnSetting, mBtnPoweroff, mBtnReboot, mBtnOtaUpdate, mBtnApkUpdate, mBtnTv, mBtnNavibar;
	/**
	 * 切换HDMI和SPDIF的透传开关
	 */
	private Switch switchHdmiPass, switchSpdifPass;
	/**
	 * HDMI SPDIF的透传是否打开
	 */
	private boolean isOpenHdmiPass, isOpenSpdifPass;
	/**
	 * 是否显示导航栏
	 */
	private boolean isShowNavibar;
	/**
	 * 当前音轨索引
	 */
	private static int curAudioIndex = 0;
	private MediaPlayer mMediaPlayer;
	private boolean isRecording = false;
	/**
	 * 播放器显示控件对象
	 */
	private SurfaceView mVideoSurfaceVga, mVideoSurfaceHdmi;
	/**
	 * 左右声道增益SeekBar
	 */
	private SeekBar mSeekBarGainL, mSeekBarGainR;
	/**
	 * 播放进度SeekBar
	 */
	private SeekBar sbPlayProgress;
	/**
	 * 保存所有声道索引的集合对象
	 */
	private Vector<Integer> mTrackAudioIndexs = new Vector<>();
	/**
	 * 存放异显对象的集合,代替键名为Integer类型的{@link java.util.HashMap}的，效率更高
	 */
	private final SparseArray<HdmiPresentation> mActivePresentations = new SparseArray<>();
	/**
	 * 自定义异显对象，用于控制播放内容显示在HDMI上的
	 */
	private HdmiPresentation mPresentation;
	/**
	 * 当前是否在异显
	 */
	final boolean enablePresentation = true;
	/**
	 * 当前播放文件集合的索引
	 */
	int curPlayIndex = 1;
	int pitch = 100;
	/**
	 * 录音对象
	 */
	private ExtAudioRecorder extAudioRecorder;
	/**
	 * 存放存储设备绝对路径的集合
	 */
	private List<MountEntity> mMountedDevices = new ArrayList<>();
	private ListView lvFiles;
	/**
	 * 扫描到的文件集合对象
	 */
	public static List<File> mFiles = new ArrayList<>();
	/**
	 * 在没有插入存储设备时播放HTTP下的媒体名字集合
	 */
	public static List<String> mHttpFileNames = new ArrayList<>();
	private FileAdapter mFileAdapter;
	/**
	 * Surface正常显示参数对象
	 */
	private ViewGroup.LayoutParams mNormalParams;
	/**
	 * Surface隐藏显示参数对象
	 */
	private ViewGroup.LayoutParams mGoneParams;
	/**
	 * Surface全屏显示参数对象
	 */
	private ViewGroup.LayoutParams mFullParams;
	/**
	 * 是否幻影
	 */
	private boolean isFullScreen;
	/**
	 * 是否同屏
	 */
	private boolean isDuplicate = true;
	/**
	 * 预览对话框
	 */
	private static final int DIALOG_OF_PREVIEW = 10;
	/**
	 * 菜单对话框
	 */
	private static final int DIALOG_OF_MENU = 20;
	/**
	 * NFS对话框
	 */
	private static final int DIALOG_OF_NFS = 30;
	/**
	 * 用来改变播放进度的任务管理Timer对象
	 */
	private Timer mTimer = new Timer();
	private boolean initFinished;

	/**
	 * 创建需要的几个Dialog
	 *
	 * @param id 对话框区分标识
	 * @return 要创建的对话框对象
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_OF_PREVIEW:
				return new PreviewDialog(this);
			case DIALOG_OF_MENU:
				return new MenuDialog(this);
			case DIALOG_OF_NFS:
				return new NfsDialog(this);
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PresentationUtil.hideNaviBar(this, true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.presentation_activity);
		mPresentationActivity = this;
		mMediaPlayer = new MediaPlayer();
		mApplication = (PresentationApplication) getApplication();
		extAudioRecorder = ExtAudioRecorder.getInstanse(false);
		mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
		initFinished = false;
		initViews();
		showViews();
		initDatas();
		initFinished = true;
	}

	/**
	 * 显示View,在这里做一些初始化后的显示
	 */
	private void showViews() {
		if (PresentationUtil.getIntegerValueFromFile(Constants.FILE_HDMI_OUTPUT_SWITCH) == 1) {
			mBtnHdmiSwitch.setText("HDMI ON");
			switchHdmiPass.setVisibility(View.VISIBLE);
		} else {
			mBtnHdmiSwitch.setText("HDMI OFF");
			switchHdmiPass.setVisibility(View.GONE);
		}
		if (PresentationUtil.getIntegerValueFromFile(Constants.FILE_SPDIF_OUTPUT_SWITCH) == 1) {
			mBtnSpdifSwitch.setText("SPDIF ON");
			switchSpdifPass.setVisibility(View.VISIBLE);
		} else {
			mBtnSpdifSwitch.setText("SPDIF OFF");
			switchSpdifPass.setVisibility(View.GONE);
		}
		if (PresentationUtil.getIntegerValueFromFile(Constants.FILE_HDMI_OUTPUT_PASS_SWITCH) == 1) {
			switchHdmiPass.setChecked(true);
		}
		if (PresentationUtil.getIntegerValueFromFile(Constants.FILE_SPDIF_OUTPUT_PASS_SWITCH) == 1) {
			switchSpdifPass.setChecked(true);
		}
	}

	/**
	 * 初始化View和设置监听
	 */
	private void initViews() {
		mDisplayListAdapter = new DisplayListAdapter(this);
		mListView = (ListView) findViewById(R.id.display_list);
		mListView.setAdapter(mDisplayListAdapter);
		mVideoSurfaceVga = (SurfaceView) findViewById(R.id.video_surface1);
		mVideoSurfaceVga.setOnClickListener(this);
		mNormalParams = mVideoSurfaceVga.getLayoutParams();
		mFullParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		mGoneParams = new RelativeLayout.LayoutParams(0, 0);
		mSeekBarGainL = (SeekBar) findViewById(R.id.seekbar_gain_l);
		mSeekBarGainL.setOnSeekBarChangeListener(mGainChanelSeekBarChangeListener);
		mSeekBarGainR = (SeekBar) findViewById(R.id.seekbar_gain_r);
		mSeekBarGainR.setOnSeekBarChangeListener(mGainChanelSeekBarChangeListener);
		sbPlayProgress = (SeekBar) findViewById(R.id.seekbar_play_progress);
		sbPlayProgress.setProgress(0);
		sbPlayProgress
				.setOnSeekBarChangeListener(mPlayProgressListener);
		sbPlayProgress.setMax(100);
		btnScan = (Button) findViewById(R.id.btn_scan_file);
		btnScan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getFiles();
			}
		});
		//-----------start button-------------
		//第一列按钮
		mBtnPlay = (Button) findViewById(R.id.btn_play);
		mBtnPlay.setOnClickListener(this);
		mBtnPause = (Button) findViewById(R.id.btn_pause);
		mBtnPause.setOnClickListener(this);
		mBtnSwitchTrack = (Button) findViewById(R.id.btn_switch);
		mBtnSwitchTrack.setOnClickListener(this);
		mBtnNext = (Button) findViewById(R.id.btn_next);
		mBtnNext.setOnClickListener(this);
		mBtnVolumeAdd = (Button) findViewById(R.id.btn_volume_add);
		mBtnVolumeAdd.setOnClickListener(this);
		mBtnVolumeReduce = (Button) findViewById(R.id.btn_volume_reduce);
		mBtnVolumeReduce.setOnClickListener(this);
		mBtnIncreaseSound = (Button) findViewById(R.id.btn_increase_sound);
		mBtnIncreaseSound.setOnClickListener(this);
		mBtnDecreaseSound = (Button) findViewById(R.id.btn_decrease_sound);
		mBtnDecreaseSound.setOnClickListener(this);
		mBtnChannel = (Button) findViewById(R.id.btn_channel);
		mBtnChannel.setOnClickListener(this);
		mBtnMenu = (Button) findViewById(R.id.btn_menu);
		mBtnMenu.setOnClickListener(this);
		mBtnNfs = (Button) findViewById(R.id.btn_nfs);
		mBtnNfs.setOnClickListener(this);

		//第二列按钮
		mBtnHdmiSwitch = (Button) findViewById(R.id.btn_hdmi_switch);
		mBtnHdmiSwitch.setOnClickListener(this);
		mBtnSpdifSwitch = (Button) findViewById(R.id.btn_spdif_switch);
		mBtnSpdifSwitch.setOnClickListener(this);
		mBtnLinein = (Button) findViewById(R.id.btn_linein);
		mBtnLinein.setOnClickListener(this);
		mBtnDuplicateScreen = (Button) findViewById(R.id.btn_duplicate_screen);
		mBtnDuplicateScreen.setOnClickListener(this);
		mBtnPreview = (Button) findViewById(R.id.btn_preview);
		mBtnPreview.setOnClickListener(this);
		mBtnFullScreen = (Button) findViewById(R.id.btn_full_screen);
		mBtnFullScreen.setOnClickListener(this);
		//第三列按钮
		mBtnHdmiMode = (Button) findViewById(R.id.btn_hdmi_mode);
		mBtnHdmiMode.setOnClickListener(this);
		mBtnVgaMode = (Button) findViewById(R.id.btn_vga_mode);
		mBtnVgaMode.setOnClickListener(this);
		mBtnGetHdmiResolution = (Button) findViewById(R.id.btn_get_hdmi_resolution);
		mBtnGetHdmiResolution.setOnClickListener(this);
		mBtnGetVgaResolution = (Button) findViewById(R.id.btn_get_vga_resolution);
		mBtnGetVgaResolution.setOnClickListener(this);
		mBtnComTop = (Button) findViewById(R.id.btn_com_1);
		mBtnComTop.setOnClickListener(this);
		mBtnComBottom = (Button) findViewById(R.id.btn_com_2);
		mBtnComBottom.setOnClickListener(this);
		mBtnComRJ = (Button) findViewById(R.id.btn_com_3);
		mBtnComRJ.setOnClickListener(this);
		mBtnGetTouchPort = (Button) findViewById(R.id.btn_get_touch_port);
		mBtnGetTouchPort.setOnClickListener(this);
		//第四列按钮
		mBtnSetting = (Button) findViewById(R.id.btn_setting);
		mBtnSetting.setOnClickListener(this);
		mBtnPoweroff = (Button) findViewById(R.id.btn_poweroff);
		mBtnPoweroff.setOnClickListener(this);
		mBtnReboot = (Button) findViewById(R.id.btn_reboot);
		mBtnReboot.setOnClickListener(this);
		mBtnApkUpdate = (Button) findViewById(R.id.btn_apk_update);
		mBtnApkUpdate.setOnClickListener(this);
		mBtnOtaUpdate = (Button) findViewById(R.id.btn_ota_update);
		mBtnOtaUpdate.setOnClickListener(this);
		mBtnTv = (Button) findViewById(R.id.btn_tv);
		mBtnTv.setOnClickListener(this);
		mBtnNavibar = (Button) findViewById(R.id.btn_navibar);
		mBtnNavibar.setOnClickListener(this);
		//---------------end button------
		switchHdmiPass = (Switch) findViewById(R.id.switch_hdmi_pass_switch);
		switchHdmiPass.setOnCheckedChangeListener(mSwitchListener);
		switchSpdifPass = (Switch) findViewById(R.id.switch_spdif_pass_switch);
		switchSpdifPass.setOnCheckedChangeListener(mSwitchListener);
		mTimer.schedule(mTimerTask, 0, 1000);
	}

	/**
	 * 初始化数据
	 */
	private void initDatas() {
		initHttpPlayFile();
		getFiles();
	}

	/**
	 * 是否正在拖动播放进度条
	 */
	private boolean isDragSeekBar;

	private TimerTask mTimerTask = new TimerTask() {
		@Override
		public void run() {
			try {
				if (!isDragSeekBar && mMediaPlayer != null && mMediaPlayer.isPlaying()) {
					sbPlayProgress.setProgress(
							mMediaPlayer.getCurrentPosition() * 100 / mMediaPlayer.getDuration());
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 初始化通过HTTP播放的文件名
	 */
	private void initHttpPlayFile() {
		if (mHttpFileNames.size() > 0) {
			mHttpFileNames.clear();
		}
//        mHttpFileNames.add("13703YHD.mpg");
//        mHttpFileNames.add("18027YHD.mpg");
//        mHttpFileNames.add("52427YHD.mpg");
//        mHttpFileNames.add("80254YHD.mpg");
//        mHttpFileNames.add("80319YHD.mpg");
//        mHttpFileNames.add("80344YHD.mpg");
//        mHttpFileNames.add("80347YHD.mpg");
//        mHttpFileNames.add("80560YHD.mpg");
//        mHttpFileNames.add("80573YHD.mpg");
//        mHttpFileNames.add("80630YHD.mpg");
	}

	/**
	 * 播放进度条改变监听器
	 */
	private OnSeekBarChangeListener mPlayProgressListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			mMediaPlayer.seekTo(sbPlayProgress.getProgress() * mMediaPlayer.getDuration() / 100);
			isDragSeekBar = false;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			isDragSeekBar = true;
		}

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1,
		                              boolean arg2) {
			if (arg2) {
				isDragSeekBar = true;
			}
		}
	};

	/**
	 * 开关按钮监听器
	 */
	private OnCheckedChangeListener mSwitchListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
			switch (compoundButton.getId()) {
				case R.id.switch_hdmi_pass_switch:
					if (initFinished) {
						pausePlayer();
						mhandler.sendEmptyMessageDelayed(PLAY, 5000);
						PresentationUtil.switchHdmiPass(PresentationActivity.this, b);
						isOpenHdmiPass = b;
						switchHdmiPass.setEnabled(false);
					}
					break;
				case R.id.switch_spdif_pass_switch:
					if (initFinished) {
						pausePlayer();
						mhandler.sendEmptyMessageDelayed(PLAY, 5000);
						PresentationUtil.switchSpdifPass(PresentationActivity.this, b);
						isOpenSpdifPass = b;
						switchSpdifPass.setEnabled(false);
					}
					break;
			}
		}
	};

	/**
	 * 暂停播放器
	 */
	private void pausePlayer() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
	}

	@Override
	public void onClick(View view) {
		if (view == mBtnPlay) {//播放
			if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
			}
		} else if (view == mBtnPause) {//暂停
			pausePlayer();
		} else if (view == mBtnSwitchTrack) {//原伴唱
			switchTrack();
		} else if (view == mBtnVolumeAdd) {//播放器音量+
			playerVolumeChange(true);
		} else if (view == mBtnVolumeReduce) {//播放器音量-
			playerVolumeChange(false);
		} else if (view == mBtnNext) {//切歌
			next();
		} else if (view == mBtnPreview) {//预览
			showDialog(DIALOG_OF_PREVIEW);

		} else if (view == mBtnChannel) {//左右声道
			switchChanel();
		} else if (view == mBtnIncreaseSound) {//升调
			if (pitch < 200) {
				pitch += 10;
			}
			mMediaPlayer.setAudioPitch(pitch);
			ToastUtil.showToastAndCancel(this, "升调到：" + pitch);
		} else if (view == mBtnDecreaseSound) {//降调
			if (pitch > 50) {
				pitch -= 10;
			}
			mMediaPlayer.setAudioPitch(pitch);
			ToastUtil.showToastAndCancel(this, "降调到：" + pitch);
		} else if (view == mBtnComTop) {
			PresentationUtil.switchTouchCom(this, Constants.COM_BOTTOM);
			ToastUtil.showToastAndCancel(this, "切换到3.5下了，即将重启！");
			PresentationUtil.rebootDevice(this);
		} else if (view == mBtnComBottom) {
			PresentationUtil.switchTouchCom(this, Constants.COM_TOP);
			ToastUtil.showToastAndCancel(this, "切换到3.5上了，即将重启！");
			PresentationUtil.rebootDevice(this);
		} else if (view == mBtnComRJ) {
			PresentationUtil.switchTouchCom(this, Constants.COM_RJ45);
			ToastUtil.showToastAndCancel(this, "切换到RJ45了，即将重启！");
			PresentationUtil.rebootDevice(this);
		} else if (view == mBtnLinein) {//开始录音
			recordControl();
		} else if (view == mBtnPoweroff) {//关机
			PresentationUtil.powerOffDevice(this);
		} else if (view == mBtnReboot) {//重启
			PresentationUtil.rebootDevice(this);
		} else if (view == mBtnOtaUpdate) {//ota升级
			PresentationUtil.updateSystemByOta(this);
		} else if (view == mBtnApkUpdate) {//apk本地升级
			PresentationUtil.updateApkByOta(this);
		} else if (view == mBtnTv) {
			ToolUtil.openThirdApplication(this, Constants.TV_PACKAGE);
		} else if (view == mBtnSetting) {//进入设置界面
			Intent intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
		} else if (view == mBtnHdmiSwitch) {//HDMI开关
			mBtnHdmiSwitch.setEnabled(false);
			soundSwitchHdmi();
		} else if (view == mBtnSpdifSwitch) {//SPDIF开关
			mBtnSpdifSwitch.setEnabled(false);
			soundSwitchSpdif();
		} else if (view == mBtnHdmiMode) {//hdmi输出模式
			switchHdmiMode();
		} else if (view == mBtnVgaMode) {//vga输出模式
			switchVgaMode();
		} else if (view == mBtnNfs) {//nfs
			showDialog(DIALOG_OF_NFS);
		} else if (view == mBtnMenu) {//菜单
			showDialog(DIALOG_OF_MENU);
		} else if (view == mVideoSurfaceVga) {//点击了SurfaceView进入或退出全屏
			if (isDuplicate && !isFullScreen) {
				mVideoSurfaceVga.setLayoutParams(mFullParams);
				isFullScreen = true;
			} else if (isDuplicate && isFullScreen) {
				mVideoSurfaceVga.setLayoutParams(mNormalParams);
				isFullScreen = false;
			} else if (!isDuplicate && isFullScreen) {
				mVideoSurfaceVga.setLayoutParams(mGoneParams);
				isFullScreen = false;
			}
		} else if (view == mBtnDuplicateScreen) {//同屏
			if (isDuplicate) {
				mVideoSurfaceVga.setLayoutParams(mGoneParams);
				isDuplicate = false;
			} else {
				mVideoSurfaceVga.setLayoutParams(mNormalParams);
				isDuplicate = true;
			}
		} else if (view == mBtnFullScreen) {//幻影
			mVideoSurfaceVga.setLayoutParams(mFullParams);
			isFullScreen = true;
			PresentationUtil.hideNaviBar(this, true);
		} else if (view == mBtnGetVgaResolution) {
			String vgaResolution = "";
			switch (getCurrentHdmiOrVgaResolution(false)) {
				case Constants.VGA_1280X720:
					vgaResolution = "1280x720";
					break;
				case Constants.VGA_1280X800:
					vgaResolution = "1280x800";
					break;
				case Constants.VGA_1440X900:
					vgaResolution = "1440x900";
					break;
				case Constants.VGA_1920X1080:
					vgaResolution = "1920x1080";
					break;
			}
			ToastUtil.showToastAndCancel(this, "当前VGA分辨率为：" + vgaResolution, true);
		} else if (view == mBtnGetHdmiResolution) {
			String hdmiResolution = "";
			switch (getCurrentHdmiOrVgaResolution(true)) {
				case Constants.HDMI_720P_50:
					hdmiResolution = "720P_50";
					break;
				case Constants.HDMI_720P_60:
					hdmiResolution = "720P_60";
					break;
				case Constants.HDMI_1080P_50:
					hdmiResolution = "1080P_50";
					break;
				case Constants.HDMI_1080P_60:
					hdmiResolution = "1080P_60";
					break;
			}
			ToastUtil.showToastAndCancel(this, "当前HDMI分辨率为：" + hdmiResolution, true);
		} else if (view == mBtnGetTouchPort) {
			String touchPort = "";
			switch (getCurrentTouchPort()) {
				case Constants.COM_0:
					touchPort = "主板com";
					break;
				case Constants.COM_TOP:
					touchPort = "3.5上";
					break;
				case Constants.COM_BOTTOM:
					touchPort = "3.5下";
					break;
				case Constants.COM_RJ45:
					touchPort = "rj45";
					break;
			}
			ToastUtil.showToastAndCancel(this, "当前触摸屏串口为：" + touchPort, true);
		} else if (view == mBtnNavibar) {//开关显示导航栏
			if (isShowNavibar) {
				PresentationUtil.hideNaviBar(this, true);
				isShowNavibar = false;
			} else {
				PresentationUtil.hideNaviBar(this, false);
				isShowNavibar = true;
			}
		}
	}

	/**
	 * 当前播放器音量
	 */
	private float currentPlayerVolume = 0.5f;

	/**
	 * 是否是加音量
	 *
	 * @param isAdd 是否是增大音量操作
	 */
	private void playerVolumeChange(boolean isAdd) {
		if (isAdd) {
			if (currentPlayerVolume < 1f) {
				currentPlayerVolume += 0.1f;
				mMediaPlayer.setVolume(currentPlayerVolume, currentPlayerVolume);
			}
		} else {
			if (currentPlayerVolume > 0f) {
				currentPlayerVolume -= 0.1f;
				mMediaPlayer.setVolume(currentPlayerVolume, currentPlayerVolume);
			}
		}
	}

	/**
	 * HDMI声音开关
	 */
	private void soundSwitchHdmi() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
		Intent intent = new Intent();
		intent.setAction("com.ynh.set_hdmi_on_off");
		if (mBtnHdmiSwitch.getText().toString().equals("HDMI OFF")) {//当前处于关闭状态
			switchHdmiPass.setVisibility(View.VISIBLE);
			if (isOpenHdmiPass) {
				PresentationUtil.switchHdmiPass(this, isOpenHdmiPass);
				isOpenHdmiPass = true;
			}
			intent.putExtra("on", 1);
			mBtnHdmiSwitch.setText("HDMI ON");
		} else {//当前处于开启状态
			switchHdmiPass.setVisibility(View.GONE);
			//关闭HDMI时，如果透传是打开状态，那就关掉
			if (PresentationUtil.getIntegerValueFromFile(Constants.FILE_HDMI_OUTPUT_PASS_SWITCH) == 1) {
				PresentationUtil.switchHdmiPass(this, false);
			}
			intent.putExtra("on", 0);
			mBtnHdmiSwitch.setText("HDMI OFF");
		}
		sendBroadcast(intent);
		//延时播放
		mhandler.sendEmptyMessageDelayed(PLAY, 5000);
	}

	/**
	 * SPDIF声音开关
	 */
	private void soundSwitchSpdif() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
		Intent intent = new Intent();
		intent.setAction("com.ynh.set_spdif_on_off");
		if (mBtnSpdifSwitch.getText().toString().equals("SPDIF OFF")) {//当前处于关闭状态
			switchSpdifPass.setVisibility(View.VISIBLE);
			if (isOpenSpdifPass) {
				PresentationUtil.switchSpdifPass(this, isOpenSpdifPass);
				isOpenSpdifPass = true;
			}
			intent.putExtra("on", 1);
			mBtnSpdifSwitch.setText("SPDIF ON");
		} else {//当前处于开启状态
			switchSpdifPass.setVisibility(View.GONE);
			//关闭SPDIF时，如果透传是打开状态，那就关掉
			if (PresentationUtil.getIntegerValueFromFile(Constants.FILE_SPDIF_OUTPUT_PASS_SWITCH) == 1) {
				PresentationUtil.switchSpdifPass(this, false);
			}
			intent.putExtra("on", 0);
			mBtnSpdifSwitch.setText("SPDIF OFF");
		}
		sendBroadcast(intent);
		//延时播放
		mhandler.sendEmptyMessageDelayed(PLAY, 5000);
	}

	/**
	 * 获取当前触摸屏串口
	 */
	private int getCurrentTouchPort() {
		File file = new File("/data/COM.TXT");
		if (!file.exists())
			return 1;
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
	 * 获取当前hdmi或vga分辨率
	 */
	private int getCurrentHdmiOrVgaResolution(boolean isHdmi) {
		File file = null;
		if (!isHdmi) {
			file = new File("/data/vga_mode");
			if (!file.exists())
				return -1;
		} else {
			file = new File("/data/hdmi_mode");
			if (!file.exists())
				return -1;
		}
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
	 * 切换VGA模式
	 */
	private void switchVgaMode() {
		Intent intent = new Intent();
		intent.setAction("com.android.vga_mode");
		if (mVgaModeValue == Constants.VGA_1280X720) {
			mVgaModeValue = Constants.VGA_1280X800;
			mBtnVgaMode.setText("1280X800");
		} else if (mVgaModeValue == Constants.VGA_1280X800) {
			mVgaModeValue = Constants.VGA_1440X900;
			mBtnVgaMode.setText("1440x900");
		} else if (mVgaModeValue == Constants.VGA_1440X900) {
			mVgaModeValue = Constants.VGA_1920X1080;
			mBtnVgaMode.setText("1920*1080");
		} else if (mVgaModeValue == Constants.VGA_1920X1080) {
			mVgaModeValue = Constants.VGA_1280X720;
			mBtnVgaMode.setText("1280x720");
		}
//		mVgaModeValue = Constants.VGA_1280X720;
//		mVgaModeValue = Constants.VGA_1280X800;
//		mVgaModeValue = Constants.VGA_1440X900;
//		mVgaModeValue = Constants.VGA_1920X1080;

		intent.putExtra("vga_mode", mVgaModeValue);
		sendBroadcast(intent);
		Intent intent1 = new Intent();
		intent1.setAction("com.ynh.vga_mode");
		intent1.putExtra("vga_mode", mVgaModeValue);
		sendBroadcast(intent1);
	}

	/**
	 * 切换HDMI模式
	 */
	private void switchHdmiMode() {
		Intent intent = new Intent();
		intent.setAction("com.android.hdmi_mode");
		if (mHdmiModeValue == Constants.HDMI_1080P_60) {
			mHdmiModeValue = Constants.HDMI_1080P_50;
			mBtnHdmiMode.setText("1080P_50");
		} else if (mHdmiModeValue == Constants.HDMI_1080P_50) {
			mHdmiModeValue = Constants.HDMI_720P_60;
			mBtnHdmiMode.setText("720P_60");
		} else if (mHdmiModeValue == Constants.HDMI_720P_60) {
			mHdmiModeValue = Constants.HDMI_720P_50;
			mBtnHdmiMode.setText("720P_50");
		} else {
			mHdmiModeValue = Constants.HDMI_1080P_60;
			mBtnHdmiMode.setText("1080P_60");
		}
		intent.putExtra("hdmi_mode", mHdmiModeValue);
		sendBroadcast(intent);
		Intent intent1 = new Intent();
		intent1.setAction("com.ynh.hdmi_mode");
		intent1.putExtra("hdmi_mode", mHdmiModeValue);
		sendBroadcast(intent1);
	}

	/**
	 * 切换音轨
	 */
	private void switchTrack() {
		Log.d(TAG, "当前播放内容总共有音轨条数为：" + mTrackAudioIndexs.size());
		try {
			if (trackNum < 2)
				return;
			curAudioIndex = (curAudioIndex + 1) % 2;
			mMediaPlayer.selectTrack(mTrackAudioIndexs.get(curAudioIndex));
			Log.d(TAG, "已经切换到音轨： " + curAudioIndex);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 切歌
	 */
	private void next() {
		try {
			mMediaPlayer.reset();
			if (mFiles.size() > 0) {
				curPlayIndex = (curPlayIndex + 1) % mFiles.size();
				mMediaPlayer.setDataSource(mFiles.get(curPlayIndex)
						.getAbsolutePath());
			} else if (mHttpFileNames.size() > 0) {
				curPlayIndex = (curPlayIndex + 1) % mHttpFileNames.size();
				mMediaPlayer.setDataSource(this, getHttpUri(mHttpFileNames.get(curPlayIndex)));
			} else {
				return;
			}
			if (mVideoSurfaceHdmi != null && enablePresentation) {
				mPresentation.setScrollText(mFiles.get(curPlayIndex).getName());
				mMediaPlayer.setMinorDisplay(mVideoSurfaceVga.getHolder());
				mMediaPlayer.setDisplay(mVideoSurfaceHdmi.getHolder());
				Log.d("Presentation", "setMinorDisplay and setDisplay");
			} else {
				mMediaPlayer.setDisplay(mVideoSurfaceVga.getHolder());
				Log.d("Presentation", "just setDisplay");
			}
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			getTrack(mMediaPlayer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 移除Activity中的所有Dialog
	 */
	private void removeAllDialogs() {
		removeDialog(DIALOG_OF_PREVIEW);
		removeDialog(DIALOG_OF_NFS);
		removeDialog(DIALOG_OF_MENU);
	}

	/**
	 * 获取HTTP播放Uri
	 *
	 * @param httpFileName
	 * @return
	 */
	private Uri getHttpUri(String httpFileName) {
		return Uri.parse(Constants.URL_PREFIX + httpFileName);
	}

	/**
	 * 切换声道
	 */
	private void switchChanel() {
		if (mChannelValue == 0) {
			mBtnChannel.setText("Stero");
			mMediaPlayer.setAudioChannel(mChannelValue);
			mChannelValue = 1;
		} else if (mChannelValue == 1) {
			mBtnChannel.setText("Left");
			mMediaPlayer.setAudioChannel(mChannelValue);
			mChannelValue = 2;
		} else {
			mBtnChannel.setText("Right");
			mMediaPlayer.setAudioChannel(mChannelValue);
			mChannelValue = 0;
		}
	}

	/**
	 * 录音控制（开始或停止）
	 */
	private void recordControl() {
		System.out.println("isRecording = " + isRecording);
		if (isRecording == false) {
			System.out.println("linein");
			isRecording = true;
			mBtnLinein.setText(getResources().getString(
					R.string.stop_linein));
			start_record();
			ToastUtil.showToastAndCancel(mPresentationActivity, "录音中...", true);
		} else {
			System.out.println("stop");
			isRecording = false;
			mBtnLinein.setText(getResources().getString(
					R.string.linein));
			stop_record();
			ToastUtil.showToastAndCancel(mPresentationActivity, "已停止录音...", true);
		}
	}

	@Override
	public void onBackPressed() {
//		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mDisplayManager.registerDisplayListener(mDisplayListener, null);
		if (enablePresentation) {
			mDisplayListAdapter.updateContents();
			int numDisplays = mDisplayListAdapter.getCount();
			Log.d(TAG, "onResume()   numDisplays=" + numDisplays);
			for (int i = 0; i < numDisplays; i++) {
				Display display = mDisplayListAdapter.getItem(i);
				if (display != null) {
					showPresentation(display);
				}
			}
		}
		mhandler.sendEmptyMessageDelayed(START_PLAY, 2000);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mDisplayManager.unregisterDisplayListener(mDisplayListener);
		Log.d(TAG, "Activity is being paused.  Dismissing all active presentation.");
		for (int i = 0; i < mActivePresentations.size(); i++) {
			HdmiPresentation presentation = mActivePresentations.valueAt(i);
			presentation.dismiss();
		}
		mActivePresentations.clear();
	}

	@Override
	protected void onStop() {
		super.onStop();
		PresentationUtil.hideNaviBar(this, false);
		isShowNavibar = true;
	}

	/**
	 * 在hdmi上显示{@link Presentation}
	 */
	private void showPresentation(Display display) {
		int displayId = display.getDisplayId();
		if (mActivePresentations.get(displayId) != null) {
			return;
		}
		mPresentation = new HdmiPresentation(this, display);
		mPresentation.show();
		mPresentation.setOnDismissListener(mOnDismissListener);
		mActivePresentations.put(displayId, mPresentation);
		mVideoSurfaceHdmi = mPresentation.getSurfaceView();
	}

	/**
	 * 取消{@link Presentation}在hdmi上显示
	 */
	private void hidePresentation(Display display) {
		int displayId = display.getDisplayId();
		HdmiPresentation presentation = mActivePresentations.get(displayId);
		if (presentation == null) {
			Log.d(TAG, "presentation == null");
			return;
		}
		mVideoSurfaceHdmi = null;
		presentation.onDisplayRemoved();
		presentation.dismiss();
		Log.d(TAG, "Dismissing presentation on display #" + displayId + ".");
		mActivePresentations.delete(displayId);
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Display display = (Display) buttonView.getTag();
		if (isChecked) {
			showPresentation(display);
		} else {
			hidePresentation(display);
		}
	}

	/**
	 * Listens for displays to be added, changed or removed. We use it to update
	 * the list and show a new {@link Presentation} when a display is connected.
	 * <p>
	 * Note that we don't bother dismissing the {@link Presentation} when a
	 * display is removed, although we could. The presentation API takes care of
	 * doing that automatically for us.
	 */
	private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
		public void onDisplayAdded(int displayId) {
			Log.d(TAG, "Display #" + displayId + " added.");
			mDisplayListAdapter.updateContents();
			Intent intent = new Intent();
			intent.setAction("com.ynh.check_hdmi");
			sendBroadcast(intent);
			if (mMediaPlayer != null) {
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
			Log.d(TAG, "re new player");
			mMediaPlayer = new MediaPlayer();
			int numDisplays = mDisplayListAdapter.getCount();
			Log.d(TAG, "numDisplays=" + numDisplays);
			for (int i = 0; i < numDisplays; i++) {
				Display display = mDisplayListAdapter.getItem(i);
				if (display != null) {
					Log.d(TAG, "new presentation");
					mPresentation = new HdmiPresentation(
							PresentationActivity.this, display);
					mPresentation.show();
					mPresentation.setOnDismissListener(mOnDismissListener);
					mActivePresentations.put(displayId, mPresentation);
					mVideoSurfaceHdmi = mPresentation.initSurfaceView();
					mhandler.sendEmptyMessageDelayed(START_PLAY, 500);
				}
			}
		}

		public void onDisplayChanged(int displayId) {
			mDisplayListAdapter.updateContents();
			Log.d(TAG, "Display #" + displayId + " changed.");
		}

		public void onDisplayRemoved(int displayId) {
			mDisplayListAdapter.updateContents();
			if (mMediaPlayer != null) {
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
			mVideoSurfaceHdmi = null;
			Log.d(TAG, "Display #" + displayId + " removed.");
		}
	};

	/**
	 * Listens for when presentations are dismissed.
	 */
	private final DialogInterface.OnDismissListener mOnDismissListener = new DialogInterface.OnDismissListener() {
		public void onDismiss(DialogInterface dialog) {
			if (mMediaPlayer != null) {
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
			HdmiPresentation presentation = (HdmiPresentation) dialog;
			int displayId = presentation.getDisplay().getDisplayId();
			Log.d(TAG, "Presentation on display #" + displayId + " was dismissed.");
			mActivePresentations.delete(displayId);
			mDisplayListAdapter.notifyDataSetChanged();
			if (mMediaPlayer == null) {
				Log.d(TAG, "re new player");
				mMediaPlayer = new MediaPlayer();
			}
			mhandler.sendEmptyMessageDelayed(START_PLAY, 500);
		}
	};

	/**
	 * List adapter. Shows information about all displays.
	 */

	private final class DisplayListAdapter extends ArrayAdapter<Display> {
		final Context mContext;

		public DisplayListAdapter(Context context) {
			super(context, R.layout.presentation_list_item);
			mContext = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View v;
			if (convertView == null) {
				v = ((Activity) mContext).getLayoutInflater().inflate(
						R.layout.presentation_list_item, null);
			} else {
				v = convertView;
			}
			final Display display = getItem(position);
			final int displayId = display.getDisplayId();
			CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox_presentation);
			cb.setTag(display);
			cb.setOnCheckedChangeListener(PresentationActivity.this);
			cb.setChecked(mActivePresentations.indexOfKey(displayId) >= 0);
			TextView tv = (TextView) v.findViewById(R.id.display_id);
			tv.setText(v
					.getContext()
					.getResources()
					.getString(R.string.presentation_display_id_text,
							displayId, display.getName()));
			return v;
		}

		/**
		 * Update the contents of the display list adapter to show information
		 * about all current displays.
		 */
		public void updateContents() {
			clear();
			String displayCategory = DisplayManager.DISPLAY_CATEGORY_PRESENTATION;
			Display[] displays = mDisplayManager.getDisplays(displayCategory);
			addAll(displays);
			Log.d(TAG, "There are currently " + displays.length
					+ " displays connected.");
		}

	}

	/**
	 * 获取当前播放内容的所有音轨
	 *
	 * @param player
	 */
	public void getTrack(MediaPlayer player) {
		Log.d(TAG, "******************** getTrack");
		MediaPlayer.TrackInfo[] trackInfos = player.getTrackInfo();
		mTrackAudioIndexs.clear();
		curAudioIndex = 0;
		trackNum = 0;
		if (trackInfos != null && trackInfos.length > 0) {
			for (int j = 0; j < trackInfos.length; j++) {
				MediaPlayer.TrackInfo info = trackInfos[j];
				Log.d(TAG, "***track type = "
						+ info.getTrackType());
				Log.d(TAG, "***MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO = "
						+ MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO);
				if (info.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
					trackNum++;
					Log.d(TAG, "add track " + j);
					mTrackAudioIndexs.add(j);
				}
			}
		} else {
			if (trackInfos == null)
				Log.e(TAG, "trackInfos = null");
			else
				Log.e(TAG, "trackInfos.length = " + trackInfos.length);
		}
	}

	@Override
	protected void onDestroy() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		mDisplayListAdapter.updateContents();
		int numDisplays = mDisplayListAdapter.getCount();
		Log.d(TAG, "numDisplays=" + numDisplays);
		for (int i = 0; i < numDisplays; i++) {
			final Display display = mDisplayListAdapter.getItem(i);
			if (display != null)
				hidePresentation(display);
		}
		mhandler.removeMessages(START_PLAY);
		PresentationUtil.hideNaviBar(this, false);
		removeAllDialogs();
		super.onDestroy();
		mApplication.exitAPP();
	}

	public static final int START_PLAY = 0;
	public static final int PLAY = 1;
	Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case START_PLAY:
					if (mFiles.size() > 0) {
						play(mFiles.get(0).getAbsolutePath(), false);
					} else if (mHttpFileNames.size() > 0) {
						play(mHttpFileNames.get(0), true);
					}
					break;
				case PLAY:
					if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
						mMediaPlayer.start();
					}
					mBtnHdmiSwitch.setEnabled(true);
					mBtnSpdifSwitch.setEnabled(true);
					switchHdmiPass.setEnabled(true);
					switchSpdifPass.setEnabled(true);
					break;
			}
		}
	};

	/**
	 * 播放
	 *
	 * @param filename
	 */
	public void play(String filename, boolean isPlayHttp) {
		try {
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer player) {
					Log.d(TAG, "player.stop");
					player.stop();
					sbPlayProgress.setProgress(0);
					try {
						Log.d(TAG, "player.reset");
						player.reset();
						Log.d(TAG, "player.setDataSource");
						if (mFiles.size() > 0) {
							curPlayIndex = (curPlayIndex + 1) % mFiles.size();
							player.setDataSource(mFiles.get(curPlayIndex)
									.getAbsolutePath());
						} else {
							curPlayIndex = (curPlayIndex + 1) % mHttpFileNames.size();
							player.setDataSource(PresentationActivity.this,
									getHttpUri(mHttpFileNames.get(curPlayIndex)));
						}
						pitch = 100;
						if (mVideoSurfaceHdmi != null && enablePresentation) {
							mPresentation.setScrollText(mFiles.get(curPlayIndex).getName());
							player.setMinorDisplay(mVideoSurfaceVga.getHolder());
							player.setDisplay(mVideoSurfaceHdmi.getHolder());
							Log.d(TAG, "setMinorDisplay and  setDisplay");
						} else {
							Log.d(TAG, "just setDisplay");
							player.setDisplay(mVideoSurfaceVga.getHolder());
						}
						player.prepare();
						player.start();
						getTrack(player);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			Log.d(TAG, "mMediaPlaer1.reset");
			mMediaPlayer.reset();
			Log.d(TAG, "mMediaPlaer1.setDataSource");
			if (isPlayHttp) {
				mMediaPlayer.setDataSource(this, getHttpUri(filename));
				Log.d("PLAY_HTTP", Uri.parse(filename).toString());
			} else {
				mMediaPlayer.setDataSource(filename);
			}
			if (mVideoSurfaceHdmi != null && enablePresentation) {
				mPresentation.setScrollText(filename.substring(filename.lastIndexOf("/") + 1));
				mMediaPlayer.setMinorDisplay(mVideoSurfaceVga.getHolder());
				mMediaPlayer.setDisplay(mVideoSurfaceHdmi.getHolder());
			} else {
				mMediaPlayer.setDisplay(mVideoSurfaceVga.getHolder());
			}
			mVideoSurfaceVga.getHolder().setType(
					SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			getTrack(mMediaPlayer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 左右声道增益监听器
	 */
	private OnSeekBarChangeListener mGainChanelSeekBarChangeListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
		                              boolean fromUser) {
			Log.d(TAG, "seekid:" + seekBar.getId() + ", progess" + progress);
			Intent intent = new Intent();
			intent.setAction("com.ynh.volume_control");
			switch (seekBar.getId()) {
				case R.id.seekbar_gain_l:
					intent.putExtra("command", 0);
					break;
				case R.id.seekbar_gain_r:
					intent.putExtra("command", 1);
					break;
				default:
					break;
			}
			intent.putExtra("value", progress);
			sendBroadcast(intent);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
	};

	/**
	 * 开始录音
	 */
	private void start_record() {
		// l 是 line in接口录音， r 是mic in接口录音
		extAudioRecorder
				.setOutputFile("/mnt/sdcard/l.pcm", "/mnt/sdcard/r.pcm");
		extAudioRecorder.start();
	}

	/**
	 * 停止录音
	 */
	private void stop_record() {
		extAudioRecorder.stop();
		extAudioRecorder.release();
		extAudioRecorder.pcm2wav("/mnt/sdcard/l.pcm", "/mnt/sdcard/l.wav");
		extAudioRecorder.pcm2wav("/mnt/sdcard/r.pcm", "/mnt/sdcard/r.wav");
		extAudioRecorder.flushAndRelease();
	}

	public MediaPlayer getMediaPlayer() {
		return mMediaPlayer;
	}

	/**
	 * 获取文件并更新适配器，设置点击事件
	 */
	private void getFiles() {
		lvFiles = (ListView) findViewById(R.id.lv_scaned_result);
		mFileAdapter = new FileAdapter(this, mFiles);
		lvFiles.setAdapter(mFileAdapter);
		lvFiles.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
			                        int position, long id) {
				try {
					mMediaPlayer.reset();
					mMediaPlayer.setDataSource(mFiles.get(position)
							.getAbsolutePath());
					if (mVideoSurfaceHdmi != null && enablePresentation) {
						mPresentation.setScrollText(mFiles.get(position).getName());
						mMediaPlayer.setMinorDisplay(mVideoSurfaceVga
								.getHolder());
						mMediaPlayer.setDisplay(mVideoSurfaceHdmi.getHolder());
					} else {
						mMediaPlayer.setDisplay(mVideoSurfaceVga.getHolder());
					}
					mMediaPlayer.prepare();
					mMediaPlayer.start();
					getTrack(mMediaPlayer);
				} catch (Exception e) {
					e.printStackTrace();
				}

				curPlayIndex = position;
			}
		});
		getAsyncTask().execute();
	}

	/**
	 * 获取扫描文件工作线程
	 *
	 * @return
	 */
	public AsyncTask<String, Integer, Boolean> getAsyncTask() {
		if (asyncTask != null)
			asyncTask = null;
		asyncTask = new AsyncTask<String, Integer, Boolean>() {

			@Override
			protected Boolean doInBackground(String... arg0) {
				DeviceManager deviceManager = new DeviceManager(
						PresentationActivity.this);
				if (mMountedDevices.size() > 0)
					mMountedDevices.clear();
				mMountedDevices.addAll(deviceManager.getAllMountedDevices());
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					if (mFiles.size() > 0)
						mFiles.clear();
					if (mMountedDevices.size() > 0) {
						for (MountEntity entity : mMountedDevices) {
							mFiles.addAll(ToolUtil.scanAllVideo(entity.path));
						}
						mFileAdapter.updateAdapter(mFiles);
					}
				}
			}
		};
		return asyncTask;
	}

	private AsyncTask<String, Integer, Boolean> asyncTask = null;

	/**
	 * 更新扫描到的文件
	 *
	 * @param nfsFiles 扫描到的文件
	 */
	public void updateFile(List<File> nfsFiles) {
		mFiles.removeAll(nfsFiles);
		mFiles.addAll(nfsFiles);
		mFileAdapter.updateAdapter(mFiles);
	}
}
