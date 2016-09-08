package com.android.presentation.app.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * @author 俊 Description: Text auto scroll TextView Created by Homgwu on
 *         2015/3/17.
 *         <p/>
 *         The method {@link #onDraw(Canvas)} has been modified by 俊 on 2016/7/1
 */
public class MarqueeTextView extends TextView {

	/**
	 * 是否停止滚动
	 */
	private boolean mStopMarquee;
	/**
	 * 是否循环滚动
	 */
	private boolean whetherLoop = true;
	private String mText;// 文本内容
	private float mCoordinateX = 800;// 当前滚动位置
	private float mTextWidth;// 文本宽度
	private int mScrollWidth = 800;// 滚动区域宽度
	private int speed = 1;// 滚动速度
	private Context mContext;

	public float getCurrentPosition() {
		return mCoordinateX;
	}

	public void setCurrentPosition(float mCoordinateX) {
		this.mCoordinateX = mCoordinateX;
	}

	public int getScrollWidth() {
		return mScrollWidth;
	}

	public void setScrollWidth(int mScrollWidth) {
		this.mScrollWidth = mScrollWidth;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	private static final String TAG = "MarqueeTextView";

	public MarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		mCoordinateX = 1920;
		mScrollWidth = 1920;
		Log.d(TAG, "mCoordinateX = " + mCoordinateX);
	}

	/**
	 * 设置滚动字幕滚动区域宽度，在切换了HDMI分辨率后需要调用来修改
	 *
	 * @param scrollWidth
	 */
	public void setmScrollWidth(int scrollWidth) {
		this.mScrollWidth = scrollWidth;
		if (mCoordinateX > this.mScrollWidth) {
			mCoordinateX = this.mScrollWidth;
		}
	}

	public void setText(String text) {
		this.mText = text;
		mTextWidth = getPaint().measureText(mText);
		if (mHandler.hasMessages(0))
			mHandler.removeMessages(0);
		mHandler.sendEmptyMessageDelayed(0, 500);
	}

	@Override
	protected void onAttachedToWindow() {
		mStopMarquee = false;
		if (!isEmpty(mText)) {
			mHandler.sendEmptyMessageDelayed(0, 2000);
		}
		super.onAttachedToWindow();
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	@Override
	protected void onDetachedFromWindow() {
		mStopMarquee = true;
		if (mHandler.hasMessages(0))
			mHandler.removeMessages(0);
		super.onDetachedFromWindow();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = getPaint();
		FontMetrics fontMetrics = paint.getFontMetrics();
		float fontHeight = fontMetrics.bottom - fontMetrics.top;
		// 计算文字baseline
		float textBaseY = getHeight() - (getHeight() - fontHeight) / 2
				- fontMetrics.bottom;
		if (!isEmpty(mText)) {
			canvas.drawText(mText, mCoordinateX, textBaseY, getPaint());
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					if (mCoordinateX < (-mTextWidth)) {// 文字滚动完了，从滚动区域的右边出来
						mCoordinateX = mScrollWidth;
						if (whetherLoop) {
							if (!mStopMarquee) {
								sendEmptyMessageDelayed(0, 2000);
							}
						} else {
							mText = "";
						}
						invalidate();
					} else {
						mCoordinateX -= speed;
						invalidate();
						if (!mStopMarquee) {
							sendEmptyMessageDelayed(0, 30);
						}
					}

					break;
			}
			super.handleMessage(msg);
		}
	};

	public boolean ismStopMarquee() {
		return mStopMarquee;
	}

	public void setmStopMarquee(boolean mStopMarquee) {
		this.mStopMarquee = mStopMarquee;
	}

	public boolean isWhetherLoop() {
		return whetherLoop;
	}

	public void setWhetherLoop(boolean whetherLoop) {
		this.whetherLoop = whetherLoop;
	}

}
