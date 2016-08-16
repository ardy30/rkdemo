package com.android.presentation.app.holder;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.content.SharedPreferences;
import android.content.Context;

import com.android.presentation.app.PresentationActivity;
import com.android.presentation.app.R;

public class VideoSettingHelper {
    private final String TAG = "VideoSettingHelper.java";

    private SubtitleAndTrackInfo mPlayerInfo;
    private Context mContext;
    //private RkISOInterface mRkISOApi;
    private MediaPlayer mMediaPlayer;
    private int mAudioChannelMode = 2;
    private int mMode2D3DIndex = 0;
    private int mScreenScaleMode = 0;

    public static final int SCREEN_MODE_ORIGINAL = 0;
    public static final int SCREEN_MODE_169 = 1;
    public static final int SCREEN_MODE_43 = 2;
    public static final int SCREEN_MODE_FULL = 3;

    public static final int MODE_2D = 0;
    public static final int MODE_MVC_3D = 1;
    public static final int MODE_SIDE_BY_SIDE_TO_3D = 2;
    public static final int MODE_TOP_BOTTOM_TO_3D = 3;
    public static final int MODE_SIDE_BY_SIDE_TO_2D = 4;
    public static final int MODE_TOP_BOTTOM_TO_2D = 5;

    //menu_items_repeat_mode
    public final int sSingle = 0;
    public final int sRepeatOne = 1;
    public final int sRepeatAll = 2;
    private static final String PREFS_NAME = "android.rk.RockVideoPlayer";

    //menu_items_subtitle
    private int mAudioTrack = -1;
    private int mSubtitleTrack = 0;
    private int mSubtitleTrackMAX = 0;

    public VideoSettingHelper(Context context) {
        mContext = context;
        mPlayerInfo = new SubtitleAndTrackInfo();
    }

    public void releaseHelper() {
        mMediaPlayer = null;
        if (null != mPlayerInfo) {
            mPlayerInfo.clear();
        }
    }

    /*public int setRkISOApi(RkISOInterface rkISOApi){
        mRkISOApi = rkISOApi;
        return 0;
    }*/

    public boolean isTVSupport3D() {
        //Return False if HMDI is Disconnected
        String mHDMIConnect_Name = "/sys/class/display/HDMI/connect";
        try {
            FileReader fread = new FileReader(mHDMIConnect_Name);
            BufferedReader buffer = new BufferedReader(fread);
            String line = null;
            while ((line = buffer.readLine()) != null) {
                int connect = Integer.parseInt(line);
                if (connect == 0) {
                    Log.d(TAG, "isTVSupport3D()-->HMDI is Disconnected(2D Default)");
                    return false;
                }
            }
            buffer.close();
            fread.close();
        } catch (IOException e) {
            Log.d(TAG, "Fail to Read Kernel Node" + mHDMIConnect_Name);
            return false;
        }

        int mode = 0;
        String mHDMI3DMode_Name = "/sys/class/display/HDMI/3dmode";
        try {
            FileReader fread = new FileReader(mHDMI3DMode_Name);
            BufferedReader buffer = new BufferedReader(fread);
            String line = null;
            String regex = "[0-9]+x[0-9]+[ipIP][-][0-9]+[,].*";
            while ((line = buffer.readLine()) != null) {
                if (line.matches(regex)) {
                    String mode3d = line.substring(line.indexOf(",") + 1);

                    mode = Integer.parseInt(mode3d.trim());
                    if (mode > 0) break;
                }
            }
            buffer.close();
            fread.close();
        } catch (IOException e) {
            Log.d(TAG, "Fail to Read Kernel Node" + mHDMI3DMode_Name);
            return false;
        }

        if (mode > 0) {
            Log.d(TAG, "isTVSupport3D()-->Support 3D Mode...");
            return true;
        }

        Log.d(TAG, "isTVSupport3D()-->Support 2D Only");
        return false;
    }

    public int set2D3DMode(int index) {
        System.out.println("set2D3DMode index = " + index);
        if ((isTVSupport3D() == false) && ((index == MODE_MVC_3D) ||
                (index == MODE_SIDE_BY_SIDE_TO_3D) || (index == MODE_TOP_BOTTOM_TO_3D))) {
            //ReflectionMediaplayer.set3DMode(mMediaPlayer, MODE_2D); /leow
            mMediaPlayer.set3DMode(MODE_2D);
            mMode2D3DIndex = index;
            return 0;
        }
        if ((index < 0) || (index > MODE_TOP_BOTTOM_TO_2D)) {
            Log.d(TAG, "set2D3DMode index must be 0-5, CHECK!");
            return -1;
        }

        mMode2D3DIndex = index;

        //for MODE_MVC_3D video only
//        if((ReflectionMediaplayer.getVideoStreamNum(mMediaPlayer)==2)&&(index==MODE_MVC_3D)){ //leow
        if ((mMediaPlayer.getVideoStreamNum() == 2) && (index == MODE_MVC_3D)) {
            mMediaPlayer.set3DMode(MODE_MVC_3D);
            System.out.println("MODE_MVC_3D");
            //ReflectionMediaplayer.set3DMode(mMediaPlayer, MODE_MVC_3D); //leow
            mMediaPlayer.set3DMode(MODE_MVC_3D);
            return 0;
        }

        //for all video
        if (index != MODE_MVC_3D) {
            mMediaPlayer.set3DMode(index);
            System.out.println("not MODE_MVC_3D");
            //    ReflectionMediaplayer.set3DMode(mMediaPlayer, index); //leow
        }
        return 0;
    }

    public int getCur2D3DModeIndex() {
        return mMode2D3DIndex;
    }

    public int setAudioTrack(int index) {
        if (mPlayerInfo == null || mPlayerInfo.getTraIndexMap() == null
                || mPlayerInfo.getTraIndexMap().size() == 0) {
            mAudioTrack = -1;
            return -1;
        }
        int realIndex = mPlayerInfo.getTraIndexMap().get(index);
        Log.d(TAG, "index=" + index + "; mapped index=" + realIndex);
        System.out.println("*******************************index=" + index + "; mapped index=" + realIndex);
        mAudioTrack = index;
        try {
            mMediaPlayer.selectTrack(realIndex);
        } catch (IllegalStateException e) {
            Log.d(TAG, "setAudioTrack(): IllegalStateException: set audio track fail");
        } catch (RuntimeException e) {
            Log.d(TAG, "setAudioTrack(): RuntimeException: set subtitle fail");
        }

        return 0;
    }

    public int getAudioTrack() {
        return mAudioTrack;
    }

    public int setSubtitleTrack(int index) {
        //probe Embedded Subtitle Track
        Log.d(TAG, "setSubtitleTrack index=" + index);

        if (index == mSubtitleTrackMAX) {
            setSubtitleVisible(0);
        } else {
            setSubtitleVisible(1);
            setSubtitleTrack_i(index);
        }
        mSubtitleTrack = index;
        return index;
    }

    public int getSubtitleTrack() {
        return mSubtitleTrack;
    }

    private void setSubtitleVisible(int visible) {
        if (mMediaPlayer != null) {
            boolean value = visible > 0 ? true : false;
            try {
                //ReflectionMediaplayer.setSubtitleVisible(mMediaPlayer, value); //leow
                mMediaPlayer.setSubtitleVisible(value);
            } catch (NoSuchMethodError nsme) {
                Log.d(TAG, "setSubtitleVisible(): NoSuchMethodError Exception Occured....");
            } catch (RuntimeException exc) {
                Log.d(TAG, "setSubtitleVisible(): RuntimeException Exception Occured....");
            }
        }
    }

    public synchronized int setSubtitleTrack_i(int index) {
        if (mPlayerInfo == null || mPlayerInfo.getSubIndexMap() == null
                || mPlayerInfo.getSubIndexMap().size() == 0) {
            return -1;
        }
        if (index >= mPlayerInfo.getSubIndexMap().size()) {
            return -1;
        }
        int realIndex = mPlayerInfo.getSubIndexMap().get(index);

        Log.d(TAG, "setSubtitleTrack index=" + index + " realIndex=" + realIndex);

        if (null != mMediaPlayer) {
            try {
                mMediaPlayer.selectTrack(realIndex);
                mSubtitleTrack = index;
                return index;
            } catch (IllegalStateException e) {
                Log.d(TAG, "setSubtitleTrack_i(): IllegalStateException: set subtitle fail");
            } catch (RuntimeException e) {
                Log.d(TAG, "setSubtitleTrack_i(): RuntimeException: set subtitle fail");
            }
        }
        return -1;
    }

    /**
     * 初始化字幕和音轨
     * @param activity
     * @return
     */
    public int initSubtitleAndTrack(PresentationActivity activity) {
        Log.e(TAG, "initEmbeddedSubtitleAndTrack");
        mMediaPlayer = activity.getMediaPlayer();
        mPlayerInfo.clear();
        if (mMediaPlayer != null) {
            MediaPlayer.TrackInfo[] trkInfo = null;
            try {
                trkInfo = mMediaPlayer.getTrackInfo();
            } catch (RuntimeException exc) {
                Log.d(TAG, "Catched one RuntimeException()");
                trkInfo = null;
            }
            if (null == trkInfo) {
                return -1;
            }
            int type = -1;
            String value = null;
            for (int i = 0; i < trkInfo.length; i++) {
                type = trkInfo[i].getTrackType();
                value = trkInfo[i].getLanguage();
                Log.d(TAG, "TrackInfo[" + i + "] = " + trkInfo[i].getFormat());
                if (type == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT
                        || type == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE) {
                    String niceName = formatSubtitleName(trkInfo, i);
                    mPlayerInfo.addToSubtitles(niceName, i);
                    Log.d(TAG, "NiceName for Subtitle-->" + niceName);
                } else if (type == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                    String niceName = formatAudioTrackName(trkInfo, i);
                    mPlayerInfo.addToTracks(niceName, i);
                    Log.d(TAG, "NiceName for Audio   -->" + niceName);
                }
            }
        }
        return 0;
    }

    /**
     * 格式化音轨名字
     * @param tracks
     * @param baseIndex
     * @return
     */
    private String formatAudioTrackName(MediaPlayer.TrackInfo[] tracks, int baseIndex) {
        int trackCount = 0;
        for (int inc = 0; inc < tracks.length; inc++) {
            int type = tracks[inc].getTrackType();
            if (type == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                trackCount++;
            }
        }
        int index = mPlayerInfo.getTrack().size() + 1;
        String lang = tracks[baseIndex].getLanguage();
        Log.d(TAG, "formatAudioTrackName   -->" + lang);
        if (lang != null) {
            return index + "/" + trackCount + " " + lang;
        }
        return index + "/" + trackCount;
    }

    /**
     * 格式化
     * @param tracks
     * @param baseIndex
     * @return
     */
    private String formatSubtitleName(MediaPlayer.TrackInfo[] tracks, int baseIndex) {
        int trackCount = 0;
        for (int inc = 0; inc < tracks.length; inc++) {
            int type = tracks[inc].getTrackType();
            if (type == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT
                    || type == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE) {
                trackCount++;
            }
        }
        int index = mPlayerInfo.getSubtitles().size() + 1;
        String lang = tracks[baseIndex].getLanguage();
        Log.d(TAG, "formatSubtitleName   -->" + lang);
        String type = "";
        if (lang != null) {
            return index + "/" + trackCount + " " + lang + " " + type;
        } else {
            return index + "/" + trackCount + " " + lang + " " + type;
        }
    }

    public String[] getNonISOSupportedSubtitles() {
        Log.d(TAG, "getNonISOSupportedSubtitles....");
        if (null == mMediaPlayer) {
            return null;
        }
        //calculate count of subtitle
        Object[] objList = mPlayerInfo.getSubtitles().toArray();
        //save embedded subtitle list
        String[] subtitles = null;
        if (objList.length > 0) {
            subtitles = new String[objList.length + 1];
            for (int i = 0; i < objList.length; i++) {
                subtitles[i] = (String) objList[i];
                Log.d(TAG, "Subtitle --> " + subtitles[i]);
            }
            mSubtitleTrackMAX = objList.length;
            subtitles[objList.length] = mContext.getResources().getString(R.string.subtitle_close);
        }
        return subtitles;
    }


    public String[] getPrimarySubtitles() {
        /*if((null!=mMediaPlayer)&&mRkISOApi.isISO()){
            return mRkISOApi.getISOSupportedSubtitles();
        }*/ //leow
        return getNonISOSupportedSubtitles();
    }

    public String[] getSupportedTracks() {
        if (null == mMediaPlayer) {
            return null;
        }
        Object[] typeObject = mPlayerInfo.getTrack().toArray();
        String[] type = null;

        Log.d(TAG, "getSupportedTracks() ...");
        type = new String[typeObject.length];
        for (int i = 0; i < type.length; i++) {
            type[i] = (String) typeObject[i];
            Log.d(TAG, "SupportedTracks type:" + type[i]);
        }
        return type;
    }

    public static Object invokeMethod(Object obj, String methodName, Class<?>[] types, Object... arguments) {
        Class<?> cls = obj.getClass();
        Method method;
        Object result = null;
        try {
            method = cls.getMethod(methodName, types);
            result = method.invoke(obj, arguments);
        } catch (Exception ex) {
        }
        return result;
    }

    public class SubtitleAndTrackInfo {
        private ArrayList<String> mListSubtitle;
        private ArrayList<String> mListTrack;
        private HashMap<Integer, Integer> mMapSubIndex;
        private HashMap<Integer, Integer> mMapTrackIndex;

        public SubtitleAndTrackInfo() {
            mListSubtitle = new ArrayList<String>();
            mListTrack = new ArrayList<String>();
            mMapSubIndex = new HashMap<Integer, Integer>();
            mMapTrackIndex = new HashMap<Integer, Integer>();
        }

        public ArrayList<String> getSubtitles() {
            return mListSubtitle;
        }

        public ArrayList<String> getTrack() {
            return mListTrack;
        }

        public void addToSubtitles(String value, int index) {
            mMapSubIndex.put(mListSubtitle.size(), index);
            mListSubtitle.add(value);
        }

        public void addToTracks(String value, int index) {
            mMapTrackIndex.put(mListTrack.size(), index);
            mListTrack.add(value);
        }

        public HashMap<Integer, Integer> getTraIndexMap() {
            return mMapTrackIndex;
        }

        public HashMap<Integer, Integer> getSubIndexMap() {
            return mMapSubIndex;
        }

        public void clear() {
            mListSubtitle.clear();
            mListTrack.clear();
            mMapSubIndex.clear();
            mMapTrackIndex.clear();
        }
    }
}
