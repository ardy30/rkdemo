package com.android.presentation.app.holder;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceView;
import android.view.MotionEvent;

import com.android.presentation.app.R;
import com.android.presentation.app.view.MarqueeTextView;

/**
 * The presentation to show on the secondary display.
 * <p/>
 * Note that the presentation display may have different metrics from the
 * display on which the main activity is showing so we must be careful to use
 * the presentation's own {@link Context} whenever we load resources.
 */
public class HdmiPresentation extends Presentation {

    private Context mContext;
    private SurfaceView mVideoSurfaceHdmi;
    private MarqueeTextView mMarqueeTextView;

    public HdmiPresentation(Context context, Display display) {
        super(context, display);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);
        // Inflate the layout.
        setContentView(R.layout.presentation_content);
        mVideoSurfaceHdmi = (SurfaceView) findViewById(R.id.video_surface_hdmi);
        mMarqueeTextView = (MarqueeTextView) findViewById(R.id.mtv_scroll);
        mMarqueeTextView.setText("测试滚动字幕");
    }

    public SurfaceView getSurfaceView() {
        return mVideoSurfaceHdmi;
    }


    public SurfaceView setSurfaceView() {
        mVideoSurfaceHdmi = (SurfaceView) findViewById(R.id.video_surface_hdmi);
        return mVideoSurfaceHdmi;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println("touch in the external window");
        return super.onTouchEvent(event);
    }

    @Override
    public void onDisplayRemoved() {
        mMarqueeTextView.setText("");
        super.onDisplayRemoved();
    }
}
