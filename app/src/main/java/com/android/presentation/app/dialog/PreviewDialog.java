package com.android.presentation.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.media.MediaPlayer;
import android.view.SurfaceView;
import android.view.View.*;
import android.view.*;
import android.widget.*;
import android.view.WindowManager.*;
import android.util.Log;

import com.android.presentation.app.PresentationActivity;
import com.android.presentation.app.R;
import com.android.presentation.app.common.Constants;

public class PreviewDialog extends Dialog implements OnClickListener, SurfaceHolder.Callback {
    private final String TAG = "MediaPlayer.java";
    Context context;
    MediaPlayer mMediaPlayer2;

    SurfaceView mPreviewSurface;
    Button mCancel;
    private SurfaceHolder holder;


    public PreviewDialog(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PreviewDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    void init() {
        this.setContentView(R.layout.preview);
        this.setTitle(R.string.preview);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = 600;
        lp.height = 480;
        getWindow().setAttributes(lp);
        mPreviewSurface = (SurfaceView) this.findViewById(R.id.video_surface3);
        mCancel = (Button) this.findViewById(R.id.back);
        mCancel.setOnClickListener(this);
        mPreviewSurface.setZOrderOnTop(true);
        mPreviewSurface.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mPreviewSurface.getHolder().addCallback(this);

    }

    private void play(SurfaceHolder holder) {
        try {
            mMediaPlayer2 = new MediaPlayer();
            System.out.println("@@@@@@@@@@111111111111111111111");
            Log.e(TAG, "mMediaPlaer2.reset");
            mMediaPlayer2.reset();
            Log.e(TAG, "mMediaPlaer2.setDataSource");
            if (PresentationActivity.mFiles.size() > 0) {
                mMediaPlayer2.setDataSource(PresentationActivity.mFiles.get(0).getPath());
            } else {
                mMediaPlayer2.setDataSource(context, Uri.parse(Constants.URL_PREFIX +
                        PresentationActivity.mHttpFileNames.get(0)));
            }
            System.out.println("@@@@@@@@@@44444444444444444444444444");
            Log.e(TAG, "mMediaPlaer2.setDisplay");
            mMediaPlayer2.setDisplay(holder);
            System.out.println("@@@@@@@@@@5555555555555555555555");
            Log.e(TAG, "mMediaPlaer2.prepare");
            mMediaPlayer2.prepare();
            Log.e(TAG, "mMediaPlaer2.start");
            System.out.println("@@@@@@@@@@66666666666666666666");
            mMediaPlayer2.start();
            mMediaPlayer2.setVolume(0, 0);
        } catch (Exception e) {
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mCancel) {
            //mMediaPlayer2.reset();
            if (mMediaPlayer2 != null) {
                Log.e(TAG, "mMediaPlayer2.release");
                mMediaPlayer2.release();
                mMediaPlayer2 = null;
            }
            this.dismiss();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        System.out.println("@@@@@@@@@@@surfaceCreated");
        play(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mMediaPlayer2 != null) {
            Log.e(TAG, "mMediaPlayer2.release 2");
            mMediaPlayer2.release();
            mMediaPlayer2 = null;
        }
    }
}
