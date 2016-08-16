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
import android.widget.TextView;

import com.android.presentation.app.adapter.FileAdapter;
import com.android.presentation.app.common.Constants;
import com.android.presentation.app.common.PresentationApplication;
import com.android.presentation.app.dialog.MenuDialog;
import com.android.presentation.app.dialog.NfsDialog;
import com.android.presentation.app.dialog.PreviewDialog;
import com.android.presentation.app.holder.DeviceManager;
import com.android.presentation.app.holder.ExtAudioRecorder;
import com.android.presentation.app.holder.HdmiPresentation;
import com.android.presentation.app.util.ToastUtil;
import com.android.presentation.app.util.ToolUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

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
    private CheckBox mShowAllDisplaysCheckbox;
    /**
     * 第一列的按钮
     */
    private Button mBtnPlay, mBtnPause, mBtnSwitchTrack, mBtnNext, mBtnPreview, mBtnChannel,
            mBtnIncreaseSound, mBtnDecreaseSound, mBtnDuplicateScreen, mBtnFullScreen;
    /**
     * 第二列的按钮
     */
    private Button mBtnLinein, mBtnPoweroff, mBtnReboot, mBtnOtaUpdate,
            mBtnApkUpdate;
    /**
     * 第三列的按钮
     */
    private Button mBtnAudioMode, mBtnHdmiMode, mBtnVgaMode, mBtnNfs, mBtnMenu;
    /**
     * 当前音轨索引
     */
    private static int curAudioIndex = 0;
    private MediaPlayer mMediaPlayer1;
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
    private Vector<Integer> mTrackAudioIndexs = new Vector<Integer>();
    /**
     * 存放异显对象的集合,代替{@link java.util.HashMap<Integer,Object>}的，效率更高
     */
    private final SparseArray<HdmiPresentation> mActivePresentations = new SparseArray<HdmiPresentation>();
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
    private List<String> mMountedDevices = new ArrayList<>();
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

    /**
     * 创建需要的几个Dialog
     *
     * @param id
     * @return
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_OF_PREVIEW:
                PreviewDialog previewDialog = new PreviewDialog(this);
                return previewDialog;
            case DIALOG_OF_MENU:
                MenuDialog menuDialog = new MenuDialog(this);
                return menuDialog;
            case DIALOG_OF_NFS:
                NfsDialog nfsDialog = new NfsDialog(this);
                return nfsDialog;
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //showNaviBar(false);
        setContentView(R.layout.presentation_activity);
        mPresentationActivity = this;
        mMediaPlayer1 = new MediaPlayer();
        mApplication = (PresentationApplication) getApplication();
        extAudioRecorder = ExtAudioRecorder.getInstanse(false);
        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Log.d(TAG, Constants.SD_CARD);
        initViews();
        initDatas();
    }

    /**
     * 初始化View和设置监听
     */
    private void initViews() {
        // Set up checkbox to toggle between showing all displays or only presentation displays.
        mShowAllDisplaysCheckbox = (CheckBox) findViewById(R.id.show_all_displays);
        mShowAllDisplaysCheckbox.setOnCheckedChangeListener(this);
        // Set up the list of displays.
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
        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);
        mBtnPause = (Button) findViewById(R.id.btn_pause);
        mBtnPause.setOnClickListener(this);
        mBtnSwitchTrack = (Button) findViewById(R.id.btn_switch);
        mBtnSwitchTrack.setOnClickListener(this);
        mBtnNext = (Button) findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(this);
        mBtnPreview = (Button) findViewById(R.id.btn_preview);
        mBtnPreview.setOnClickListener(this);
        mBtnChannel = (Button) findViewById(R.id.btn_channel);
        mBtnChannel.setOnClickListener(this);
        mBtnIncreaseSound = (Button) findViewById(R.id.btn_increase_sound);
        mBtnIncreaseSound.setOnClickListener(this);
        mBtnDecreaseSound = (Button) findViewById(R.id.btn_decrease_sound);
        mBtnDecreaseSound.setOnClickListener(this);
        mBtnDuplicateScreen = (Button) findViewById(R.id.btn_duplicate_screen);
        mBtnDuplicateScreen.setOnClickListener(this);
        mBtnFullScreen = (Button) findViewById(R.id.btn_full_screen);
        mBtnFullScreen.setOnClickListener(this);
        mBtnLinein = (Button) findViewById(R.id.btn_linein);
        mBtnLinein.setOnClickListener(this);
        mBtnPoweroff = (Button) findViewById(R.id.btn_poweroff);
        mBtnPoweroff.setOnClickListener(this);
        mBtnReboot = (Button) findViewById(R.id.btn_reboot);
        mBtnReboot.setOnClickListener(this);
        mBtnOtaUpdate = (Button) findViewById(R.id.btn_ota_update);
        mBtnOtaUpdate.setOnClickListener(this);
        mBtnApkUpdate = (Button) findViewById(R.id.btn_apk_update);
        mBtnApkUpdate.setOnClickListener(this);
        mBtnAudioMode = (Button) findViewById(R.id.btn_audio_mode);
        mBtnAudioMode.setOnClickListener(this);
        mBtnHdmiMode = (Button) findViewById(R.id.btn_hdmi_mode);
        mBtnHdmiMode.setOnClickListener(this);
        mBtnVgaMode = (Button) findViewById(R.id.btn_vga_mode);
        mBtnVgaMode.setOnClickListener(this);
        mBtnMenu = (Button) findViewById(R.id.btn_menu);
        mBtnMenu.setOnClickListener(this);
        mBtnNfs = (Button) findViewById(R.id.btn_nfs);
        mBtnNfs.setOnClickListener(this);
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
            if (!isDragSeekBar && mMediaPlayer1 != null && mMediaPlayer1.isPlaying()) {
                sbPlayProgress.setProgress(
                        mMediaPlayer1.getCurrentPosition() * 100 / mMediaPlayer1.getDuration());
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
        mHttpFileNames.add("13703YHD.mpg");
        mHttpFileNames.add("18027YHD.mpg");
        mHttpFileNames.add("52427YHD.mpg");
        mHttpFileNames.add("80254YHD.mpg");
        mHttpFileNames.add("80319YHD.mpg");
        mHttpFileNames.add("80344YHD.mpg");
        mHttpFileNames.add("80347YHD.mpg");
        mHttpFileNames.add("80560YHD.mpg");
        mHttpFileNames.add("80573YHD.mpg");
        mHttpFileNames.add("80630YHD.mpg");
    }

    /**
     * 播放进度条改变监听器
     */
    private OnSeekBarChangeListener mPlayProgressListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            mMediaPlayer1.seekTo(sbPlayProgress.getProgress() * mMediaPlayer1.getDuration() / 100);
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

    @Override
    public void onClick(View view) {
        if (view == mBtnPlay) {//播放
            if (mMediaPlayer1 != null && !mMediaPlayer1.isPlaying()) {
                mMediaPlayer1.start();
            }
        } else if (view == mBtnPause) {//暂停
            if (mMediaPlayer1 != null && mMediaPlayer1.isPlaying()) {
                mMediaPlayer1.pause();
            }
        } else if (view == mBtnSwitchTrack) {//原伴唱
            switchTrack();
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
            mMediaPlayer1.setAudioPitch(pitch);
            ToastUtil.showToastAndCancel(this, "升调到：" + pitch);
        } else if (view == mBtnDecreaseSound) {//降调
            if (pitch > 50) {
                pitch -= 10;
            }
            mMediaPlayer1.setAudioPitch(pitch);
            ToastUtil.showToastAndCancel(this, "降调到：" + pitch);
        } else if (view == mBtnLinein) {//开始录音
            recordControl();
        } else if (view == mBtnPoweroff) {//关机
            powerOffDevice();
        } else if (view == mBtnReboot) {//重启
            rebootDevice();
        } else if (view == mBtnOtaUpdate) {//ota升级
            updateSystemByOta();
        } else if (view == mBtnApkUpdate) {//apk本地升级
            updateApkByOta();
        } else if (view == mBtnAudioMode) {//音频输出模式
            switchAudioMode();
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
                Intent intent = new Intent();
                intent.setAction("android.intent.action.hideNaviBar");
                intent.putExtra("hide", true);
                sendBroadcast(intent);
            } else if (isDuplicate && isFullScreen) {
                mVideoSurfaceVga.setLayoutParams(mNormalParams);
                isFullScreen = false;
                Intent intent = new Intent();
                intent.setAction("android.intent.action.hideNaviBar");
                intent.putExtra("hide", false);
                sendBroadcast(intent);
            } else if (!isDuplicate && isFullScreen) {
                mVideoSurfaceVga.setLayoutParams(mGoneParams);
                isFullScreen = false;
                return;
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
            Intent intent = new Intent();
            intent.setAction("android.intent.action.hideNaviBar");
            intent.putExtra("hide", true);
            sendBroadcast(intent);
        }
    }

    /**
     * 切换VGA模式
     */
    private void switchVgaMode() {
        System.out.println("vga mode");
        Intent intent = new Intent();
        intent.setAction("com.android.vga_mode");
        if (mVgaModeValue == 0) {
            mBtnVgaMode.setText("1280*800");
            intent.putExtra("vga_mode", 1);
            mVgaModeValue = 1;
        } else if (mVgaModeValue == 1) {
            mBtnVgaMode.setText("1920*1080");
            intent.putExtra("vga_mode", 2); // 2: 1920*1080 0:1440*900
            // 1:1280*800
            mVgaModeValue = 2;
        } else if (mVgaModeValue == 2) {
            mBtnVgaMode.setText("1440*900");
            intent.putExtra("vga_mode", 0);
            mVgaModeValue = 0;
        }
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
        // 16: 1920*1080-60 31:1920*1080-50 4:1280*720-60 19:1280*720-50
        System.out.println("hdmi mode");
        Intent intent = new Intent();
        intent.setAction("com.android.hdmi_mode");
        if (mHdmiModeValue == 16) {
            mBtnHdmiMode.setText("1080p-50");
            intent.putExtra("hdmi_mode", 31);
            mHdmiModeValue = 31;
        } else if (mHdmiModeValue == 31) {
            mBtnHdmiMode.setText("720p-60");
            intent.putExtra("hdmi_mode", 4);
            mHdmiModeValue = 4;
        } else if (mHdmiModeValue == 4) {
            mBtnHdmiMode.setText("720p-50");
            intent.putExtra("hdmi_mode", 19);
            mHdmiModeValue = 19;
        } else {
            mBtnHdmiMode.setText("1080p-60");
            intent.putExtra("hdmi_mode", 16);
            mHdmiModeValue = 16;
        }
        sendBroadcast(intent);
        Intent intent1 = new Intent();
        intent1.setAction("com.ynh.hdmi_mode");
        intent1.putExtra("hdmi_mode", mHdmiModeValue);
        sendBroadcast(intent1);
    }

    /**
     * 切换音频输出模式
     */
    private void switchAudioMode() {
        System.out.println("audio mode");
        Intent intent = new Intent();
        intent.setAction("com.android.audio_mode");

        if (mAudioModeValue == 0) {
            mBtnAudioMode.setText("spdif passthrough");
            intent.putExtra("audio_mode", 1);
            mAudioModeValue = 2;
        } else if (mAudioModeValue == 1) {
            mBtnAudioMode.setText("default");
            // 0: 默认输出 1:spdif源码输出 2:HDMI源码输出
            intent.putExtra("audio_mode", 0);
            mAudioModeValue = 0;
        } else {
            mBtnAudioMode.setText("HDMI passthrough");
            intent.putExtra("audio_mode", 2);
            mAudioModeValue = 1;
        }
        sendBroadcast(intent);
    }

    /**
     * OTA升级系统
     */
    private void updateSystemByOta() {
        // ota update
        // need to rename ota package to update.zip, and put it in /mnt/sdcard
        // folder, then click this button for ota update
        System.out.println("ota update");
        Intent intent = new Intent(
                "softwinner.intent.action.autoupdate");
        startActivity(intent);
    }

    /**
     * OTA升级软件
     */
    private void updateApkByOta() {
        // apk update
        // need to put your apk in /mnt/sdcard folder, then click this button
        // for
        // apk update
        System.out.println("ota update");
        Intent intent = new Intent();
        intent.setAction("com.ynh.update_apk");
        // abc.apk name
        intent.putExtra("apkname", "PresentationActivity.apk");
        // com.xxx.xxx is the package name of your apk
        intent.putExtra("packagename", "com.android.presentation.app");
        // com.xxx.xxx.xxx is the first activity name of your apk
        intent.putExtra("activityname",
                "com.android.presentation.app.PresentationActivity");
        sendBroadcast(intent);
    }

    /**
     * 重启设备
     */
    private void rebootDevice() {
        System.out.println("reboot");
        Intent intent = new Intent();
        intent.setAction("android.com.ynh.reboot");
        sendBroadcast(intent);
    }

    /**
     * 切换音轨
     */
    private void switchTrack() {
        Log.d(TAG, "mTrackAudioIndexs size = " + mTrackAudioIndexs.size());
        try {
            if (trackNum < 2)
                return;
            curAudioIndex = (curAudioIndex + 1) % 2;
            mMediaPlayer1.selectTrack(mTrackAudioIndexs.get(curAudioIndex));
            // mMediaPlayer1.pause();
            Log.d(TAG, "curentAudioIndex = " + curAudioIndex);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切歌
     */
    private void next() {
        try {
            removeAllDialogs();
            Intent intent = new Intent();
            intent.setAction("com.ynh.restart_ms");
            sendBroadcast(intent);
            TimeUnit.MILLISECONDS.sleep(500);
            mMediaPlayer1.reset();
            if (mFiles.size() > 0) {
                curPlayIndex = (curPlayIndex + 1) % mFiles.size();
                mMediaPlayer1.setDataSource(mFiles.get(curPlayIndex)
                        .getAbsolutePath());
            } else {
                curPlayIndex = (curPlayIndex + 1) % mHttpFileNames.size();
                mMediaPlayer1.setDataSource(this, getHttpUri(mHttpFileNames.get(curPlayIndex)));
            }
            if (mVideoSurfaceHdmi != null && enablePresentation) {
                mMediaPlayer1.setMinorDisplay(mVideoSurfaceVga
                        .getHolder());
                Log.d("Presentation", "@@@@@@@@@@@@ setMinorDisplay");
                mMediaPlayer1.setDisplay(mVideoSurfaceHdmi.getHolder());
                Log.d("Presentation", "@@@@@@@@@@@@ setDisplay");
            } else {
                Log.d("Presentation", "@@@@@@@@@@@@ setDisplay");
                mMediaPlayer1.setDisplay(mVideoSurfaceVga.getHolder());
            }
            Log.d("Presentation", "@@@@@@@@@@@@ prepare");
            mMediaPlayer1.prepare();
            mMediaPlayer1.start();
            getTrack(mMediaPlayer1);
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
            mBtnChannel.setText("Right");
            mMediaPlayer1.setVolume(0, 1);
            mChannelValue = 2;
        } else if (mChannelValue == 1) {
            mBtnChannel.setText("Stero");
            mMediaPlayer1.setVolume(1, 1);
            mChannelValue = 0;
        } else {
            mBtnChannel.setText("Left");
            mMediaPlayer1.setVolume(1, 0);
            mChannelValue = 1;
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

    /**
     * 关机
     */
    private void powerOffDevice() {
        System.out.println("power off");
        Intent intent = new Intent();
        intent.setAction("android.com.ynh.power_off");
        sendBroadcast(intent);
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
                curPlayIndex = position;
                try {
                    Intent intent = new Intent();
                    intent.setAction("com.ynh.restart_ms");
                    sendBroadcast(intent);
                    TimeUnit.MILLISECONDS.sleep(500);
                    mMediaPlayer1.reset();
                    mMediaPlayer1.setDataSource(mFiles.get(position)
                            .getAbsolutePath());
                    if (mVideoSurfaceHdmi != null && enablePresentation) {
                        mMediaPlayer1.setMinorDisplay(mVideoSurfaceVga
                                .getHolder());
                        Log.d("Presentation", "@@@@@@@@@@@@ setMinorDisplay");
                        mMediaPlayer1.setDisplay(mVideoSurfaceHdmi.getHolder());
                        Log.d("Presentation", "@@@@@@@@@@@@ setDisplay");
                    } else {
                        Log.d("Presentation", "@@@@@@@@@@@@ setDisplay");
                        mMediaPlayer1.setDisplay(mVideoSurfaceVga.getHolder());
                    }
                    Log.d("Presentation", "@@@@@@@@@@@@ prepare");
                    mMediaPlayer1.prepare();
                    mMediaPlayer1.start();
                    getTrack(mMediaPlayer1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        asyncTask.execute();
    }

    /**
     * 扫描文件线程对象
     */
    private AsyncTask<String, Integer, Boolean> asyncTask = new AsyncTask<String, Integer, Boolean>() {

        @Override
        protected Boolean doInBackground(String... arg0) {
            DeviceManager deviceManager = new DeviceManager(
                    PresentationActivity.this);
            mMountedDevices.addAll(deviceManager.getMountedDevicesList());
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (mMountedDevices.size() > 0) {
                    for (String string : mMountedDevices) {
                        mFiles.addAll(ToolUtil.scanAllVideo(string));
                    }
                    mFileAdapter.updateAdapter(mFiles);
                }
            }
        }
    };

    @Override
    protected void onResume() {
        // Be sure to call the super class.
        super.onResume();
        if (enablePresentation) {
            // Update our list of displays on resume.
            mDisplayListAdapter.updateContents();
            // Restore presentations from before the activity was paused.
            int numDisplays = mDisplayListAdapter.getCount();
            System.out.println("@@@@@@@@numDisplays=" + numDisplays);
            for (int i = 0; i < numDisplays; i++) {
                final Display display = mDisplayListAdapter.getItem(i);
                if (display != null)
                    showPresentation(display);
            }
            // Register to receive events from the display manager.
            mDisplayManager.registerDisplayListener(mDisplayListener, null);
        }
        mhandler.sendEmptyMessageDelayed(0, 2000);
    }

    @Override
    protected void onPause() {
        // Be sure to call the super class.
        super.onPause();
        // Unregister from the display manager.
        mDisplayManager.unregisterDisplayListener(mDisplayListener);
        // Dismiss all of our presentations but remember their contents.
        Log.d(TAG,
                "Activity is being paused.  Dismissing all active presentation.");
        for (int i = 0; i < mActivePresentations.size(); i++) {
            HdmiPresentation presentation = mActivePresentations.valueAt(i);
            presentation.dismiss();
        }
        mActivePresentations.clear();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Be sure to call the super class.
        super.onSaveInstanceState(outState);
    }

    /**
     * 在hdmi上显示{@link Presentation}
     */
    private void showPresentation(Display display) {
        final int displayId = display.getDisplayId();
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
        final int displayId = display.getDisplayId();
        HdmiPresentation presentation = mActivePresentations.get(displayId);
        if (presentation == null) {
            Log.d(TAG, "presentation == null");
            return;
        }
        Log.d(TAG, "Dismissing presentation on display #" + displayId + ".");
        mVideoSurfaceHdmi = null;
        presentation.onDisplayRemoved();
        presentation.dismiss();
        mActivePresentations.delete(displayId);
    }

    /**
     * Called when the show all displays checkbox is toggled or when an item in
     * the list of displays is checked or unchecked.
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mShowAllDisplaysCheckbox) {
            // Show all displays checkbox was toggled.
            mDisplayListAdapter.updateContents();
        } else {
            // Display item checkbox was toggled.
            final Display display = (Display) buttonView.getTag();
            if (isChecked) {
                showPresentation(display);
            } else {
                hidePresentation(display);
            }
        }
    }

    /**
     * Listens for displays to be added, changed or removed. We use it to update
     * the list and show a new {@link Presentation} when a display is connected.
     * <p/>
     * Note that we don't bother dismissing the {@link Presentation} when a
     * display is removed, although we could. The presentation API takes care of
     * doing that automatically for us.
     */
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        public void onDisplayAdded(int displayId) {
            Log.d(TAG, "leow Display #" + displayId + " added.");
            mDisplayListAdapter.updateContents();

            if (mMediaPlayer1 != null) {
                mMediaPlayer1.release();
                mMediaPlayer1 = null;
            }
            if (mMediaPlayer1 == null) {
                Log.d(TAG, "re new player");
                mMediaPlayer1 = new MediaPlayer();
            }
            int numDisplays = mDisplayListAdapter.getCount();
            System.out.println("@@@@@@@@numDisplays=" + numDisplays);
            for (int i = 0; i < numDisplays; i++) {
                final Display display = mDisplayListAdapter.getItem(i);
                if (display != null) {
                    Log.d(TAG, "new presentation");
                    mPresentation = new HdmiPresentation(
                            PresentationActivity.this, display);
                    mPresentation.show();
                    mPresentation.setOnDismissListener(mOnDismissListener);
                    mActivePresentations.put(displayId, mPresentation);
                    mVideoSurfaceHdmi = mPresentation.setSurfaceView();
                    mhandler.sendEmptyMessageDelayed(0, 500);
                }
            }

        }

        public void onDisplayChanged(int displayId) {
            Log.d(TAG, "Display #" + displayId + " changed.");
            mDisplayListAdapter.updateContents();
        }

        public void onDisplayRemoved(int displayId) {
            Log.d(TAG, "Display #" + displayId + " removed.");
            mDisplayListAdapter.updateContents();
            if (mMediaPlayer1 != null) {
                mMediaPlayer1.release();
                mMediaPlayer1 = null;
            }
            mVideoSurfaceHdmi = null;
        }
    };

    /**
     * Listens for when presentations are dismissed.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener = new DialogInterface.OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            if (mMediaPlayer1 != null) {

                mMediaPlayer1.release();
                mMediaPlayer1 = null;
            }
            HdmiPresentation presentation = (HdmiPresentation) dialog;
            int displayId = presentation.getDisplay().getDisplayId();
            Log.d(TAG, "Presentation on display #" + displayId
                    + " was dismissed.");
            mActivePresentations.delete(displayId);
            mDisplayListAdapter.notifyDataSetChanged();
            if (mMediaPlayer1 == null) {
                Log.d(TAG, "re new player");
                mMediaPlayer1 = new MediaPlayer();
            }
            mhandler.sendEmptyMessageDelayed(0, 500);
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
            String displayCategory = getDisplayCategory();
            Display[] displays = mDisplayManager.getDisplays(displayCategory);
            addAll(displays);
            Log.d(TAG, "There are currently " + displays.length
                    + " displays connected.");
            for (Display display : displays) {
                Log.d(TAG, "  " + display);
            }
        }

        private String getDisplayCategory() {
            return mShowAllDisplaysCheckbox.isChecked() ? null
                    : DisplayManager.DISPLAY_CATEGORY_PRESENTATION;
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
        if (mMediaPlayer1 != null) {
            mMediaPlayer1.release();
            mMediaPlayer1 = null;
        }
        // final Display display = mDisplayListAdapter.getItem(0);
        mDisplayListAdapter.updateContents();
        // Restore presentations from before the activity was paused.
        int numDisplays = mDisplayListAdapter.getCount();
        System.out.println("@@@@@@@@numDisplays=" + numDisplays);
        for (int i = 0; i < numDisplays; i++) {
            final Display display = mDisplayListAdapter.getItem(i);
            if (display != null)
                hidePresentation(display);
        }
        mhandler.removeMessages(0);
        //showNaviBar(true);
        super.onDestroy();
        mApplication.exitAPP();
    }


    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mFiles.size() > 0) {
                        play(mFiles.get(0).getAbsolutePath(), false);
                    } else {
                        play(mHttpFileNames.get(0), true);
                    }
//                    showDialog(DIALOG_OF_PREVIEW);
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
            mMediaPlayer1.setOnCompletionListener(new OnCompletionListener() {
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
                            Log.d(TAG, "@@@@@@@@@@@@ setMinorDisplay");

                            Log.d(TAG, "@@@@@@@@@@@@ setDisplay");
                            player.setMinorDisplay(mVideoSurfaceVga.getHolder());
                            player.setDisplay(mVideoSurfaceHdmi.getHolder());

                        } else {
                            Log.d(TAG, "@@@@@@@@@@@@ setDisplay");
                            player.setDisplay(mVideoSurfaceVga.getHolder());
                        }

                        Log.d(TAG, "@@@@@@@@@@@@ player.prepare");
                        player.prepare();
                        Log.d(TAG, "******************** player.start");
                        player.start();
                        getTrack(player);

                    } catch (Exception e) {
                    }
                }
            });
            Log.d(TAG, "mMediaPlaer1.reset");
            mMediaPlayer1.reset();
            Log.d(TAG, "mMediaPlaer1.setDataSource");
            if (isPlayHttp) {
                mMediaPlayer1.setDataSource(this, getHttpUri(filename));
                Log.d("PLAY_HTTP", Uri.parse(filename).toString());
            } else {
                mMediaPlayer1.setDataSource(filename);
            }
            if (mVideoSurfaceHdmi != null && enablePresentation) {
                Log.d(TAG, "@@@@@@@@@@@@ mMediaPlaer1.setMinorDisplay");

                Log.d(TAG, "@@@@@@@@@@@@ mMeidaPlaer1.setDisplay");
                mMediaPlayer1.setMinorDisplay(mVideoSurfaceVga.getHolder());
                mMediaPlayer1.setDisplay(mVideoSurfaceHdmi.getHolder());

            } else {
                Log.d(TAG, "@@@@@@@@@@@@ mMediaPlaer1.setDisplay");
                mMediaPlayer1.setDisplay(mVideoSurfaceVga.getHolder());
            }
            mVideoSurfaceVga.getHolder().setType(
                    SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            Log.d(TAG, "@@@@@@@@@@@@ mMediaPlaer1.prepare");
            mMediaPlayer1.prepare();
            Log.d(TAG, "mMediaPlaer1.start");
            mMediaPlayer1.start();
            getTrack(mMediaPlayer1);
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
        extAudioRecorder.flushAndRelease();
//        extAudioRecorder.pcm2wav("/mnt/sdcard/r.pcm", "/mnt/sdcard/r.wav");
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer1;
    }

    /**
     * 是否显示下面的导航栏
     *
     * @param showNaviBar
     */
    private void showNaviBar(boolean showNaviBar) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.hideNaviBar");
        intent.putExtra("hide", showNaviBar);
        sendBroadcast(intent);
    }
}
