package com.android.presentation.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.*;
import android.view.*;
import android.widget.*;
import android.util.Log;

import com.android.presentation.app.PresentationActivity;
import com.android.presentation.app.R;
import com.android.presentation.app.holder.VideoSettingHelper;

import java.util.ArrayList;

public class MenuDialog extends Dialog implements OnClickListener {
    Context context;
    Button mCancel;
    private static VideoSettingHelper mSettingHelper = null;
    private static IndexStringPair mItemsPair2d3d = null;
    private static IndexStringPair mItemsPairSutitle = null;
    private static IndexStringPair mItemsPairTrack = null;
    private int mFucusIndex = 0;

    public MenuDialog(Context context) {
        super(context);
        this.context = context;
        mSettingHelper = new VideoSettingHelper(context);
        mItemsPairSutitle = new IndexStringPair(null);
        mItemsPairTrack = new IndexStringPair(null);
        String[] mode_2d3d = context.getResources().getStringArray(
                R.array.mode_2d3d);
        mItemsPair2d3d = new IndexStringPair(mode_2d3d);
        init();
    }

    public MenuDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public void initAlterableStringPair() {
        // LogUtil.Log(TAG,"getAlterableStringPair");
        mSettingHelper.initSubtitleAndTrack((PresentationActivity) context);
        mItemsPairSutitle.updateStringPair(mSettingHelper.getPrimarySubtitles());
        mItemsPairTrack.updateStringPair(mSettingHelper.getSupportedTracks());
        mItemsPairSutitle.setCurIndex(mSettingHelper.getSubtitleTrack());
        mItemsPairTrack.setCurIndex(mSettingHelper.getAudioTrack());
    }

    void init() {
        this.setContentView(R.layout.menu);
        this.setTitle(R.string.menu);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = 750;
        lp.height = 400;
        getWindow().setAttributes(lp);
        mCancel = (Button) this.findViewById(R.id.back);
        mCancel.setOnClickListener(this);
        initAlterableStringPair();
        updateMainMenu();
    }

    @Override
    public void onClick(View v) {
        if (v == mCancel) {
            this.dismiss();
        }
    }

    private class IndexStringPair {
        private ArrayList<String> mList;
        private int mCurIndex = 0;
        private final int DIR_LEFT = 100;
        private final int DIR_RIGHT = 200;

        public IndexStringPair() {
            mList = new ArrayList<String>();
            updateStringPair(null);

        }

        public IndexStringPair(String[] itemNames) {
            mList = new ArrayList<String>();
            updateStringPair(itemNames);
        }

        public void updateStringPair(String[] itemNames) {
            mList.clear();
            if (itemNames != null) {
                for (int index = 0; index < itemNames.length; index++) {
                    addIndexString(index, itemNames[index]);
                }
            }
            if (mList.size() == 0) {
                addIndexString(0, "None");
            }
            setCurIndex(0);

        }

        public int addIndexString(int index, String text) {
            if (index == mList.size()) {
                mList.add(index, text);
                return 0;
            }
            return -1;
        }

        public String getCurText() {
            if (mCurIndex < 0) {
                mCurIndex = 0;
            }
            if (mCurIndex >= mList.size()) {
                mCurIndex = mList.size() - 1;
            }
            return mList.get(mCurIndex);

        }

        public void setCurIndex(int idx) {
            mCurIndex = idx;
            if (mCurIndex < 0) {
                mCurIndex = 0;
            }
            if (mCurIndex >= mList.size()) {
                mCurIndex = mList.size() - 1;
            }
        }

        public int getCurIndex() {
            return mCurIndex;
        }

        public String switchLeft() {
            mCurIndex--;
            return getCurText();
        }

        public String switchRight() {
            mCurIndex++;
            return getCurText();
        }
    }

    private void setLeftImage(ImageView view, boolean isHeightlight) {
        if (isHeightlight) {
            view.setImageResource(R.drawable.arrow_left_h);
        } else {
            view.setImageResource(R.drawable.arrow_left);
        }
    }

    private void setRightImage(ImageView view, boolean isHeightlight) {
        if (isHeightlight) {
            view.setImageResource(R.drawable.arrow_right_h);
        } else {
            view.setImageResource(R.drawable.arrow_right);
        }
    }

    private void updateMainMenu() {
        // 2d&3d mode
        TextView func2 = (TextView) findViewById(R.id.linear_text20_tile);
        mItemsPair2d3d.setCurIndex(mSettingHelper.getCur2D3DModeIndex());
        func2.setText(mItemsPair2d3d.getCurText());
        // audio track
        TextView func5 = (TextView) findViewById(R.id.linear_text50_tile);
        mItemsPairTrack.setCurIndex(mSettingHelper.getAudioTrack());
        func5.setText(mItemsPairTrack.getCurText());
        // Primary Subtitle
        TextView func6 = (TextView) findViewById(R.id.linear_text60_tile);
        mItemsPairSutitle.setCurIndex(mSettingHelper.getSubtitleTrack());
        func6.setText(mItemsPairSutitle.getCurText());
        ImageView btn_L2 = (ImageView) findViewById(R.id.linear_image_left2);
        ImageView btn_R2 = (ImageView) findViewById(R.id.linear_image_right2);
        btn_L2.setOnTouchListener(mBtnTouchListener);
        btn_R2.setOnTouchListener(mBtnTouchListener);
        ImageView btn_L5 = (ImageView) findViewById(R.id.linear_image_left5);
        ImageView btn_R5 = (ImageView) findViewById(R.id.linear_image_right5);
        btn_L5.setOnTouchListener(mBtnTouchListener);
        btn_R5.setOnTouchListener(mBtnTouchListener);
        ImageView btn_L6 = (ImageView) findViewById(R.id.linear_image_left6);
        ImageView btn_R6 = (ImageView) findViewById(R.id.linear_image_right6);
        btn_L6.setOnTouchListener(mBtnTouchListener);
        btn_R6.setOnTouchListener(mBtnTouchListener);
        ImageView image = null;
        image = (ImageView) findViewById(R.id.linear_image_left2);
        setLeftImage(image, false);
        image = (ImageView) findViewById(R.id.linear_image_right2);
        setRightImage(image, false);
        image = (ImageView) findViewById(R.id.linear_image_left5);
        setLeftImage(image, false);
        image = (ImageView) findViewById(R.id.linear_image_right5);
        setRightImage(image, false);
        image = (ImageView) findViewById(R.id.linear_image_left6);
        setLeftImage(image, false);
        image = (ImageView) findViewById(R.id.linear_image_right6);
        setRightImage(image, false);
    }

    View.OnTouchListener mBtnTouchListener = new View.OnTouchListener() {

        public boolean onTouch(View v, MotionEvent event) {
            System.out
                    .println("******************************** leow on touch *****************************8");
            // cancel old auto hide-msg and send new auto hide-msg with delay
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                onLeftClick(v);
                onRightClick(v);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                updateMainMenu();
                updateArrowHighlight(false, false);
                updateArrowHighlight(true, false);
            }
            return true;
        }
    };

    public void onLeftClick(View view) {
        switch (view.getId()) {
            case R.id.linear_image_left2:
                mFucusIndex = 1;
                updateArrowHighlight(true, true);
                doLeftRight(mItemsPair2d3d, true);
                mSettingHelper.set2D3DMode(mItemsPair2d3d.getCurIndex());
                break;
            case R.id.linear_image_left5:
                mFucusIndex = 4;
                updateArrowHighlight(true, true);
                doLeftRight(mItemsPairTrack, true);
                mSettingHelper.setAudioTrack(mItemsPairTrack.getCurIndex());
                break;
            case R.id.linear_image_left6:
                mFucusIndex = 5;
                updateArrowHighlight(true, true);
      /*
       * int index = mItemsPairSutitle.getCurIndex();
       * doLeftRight(mItemsPairSutitle, true);
       * mSettingHelper.setSubtitleTrack(index);
       */
                int index = mItemsPairSutitle.getCurIndex();
                doLeftRight(mItemsPairSutitle, true);
                Log.e("leow", "onKeyLeftRight index=" + mItemsPairSutitle.getCurIndex());
                if (index != mItemsPairSutitle.getCurIndex()) {
                    mSettingHelper.setSubtitleTrack(mItemsPairSutitle.getCurIndex());
                }
                break;
            default:
                break;
        }
    }

    public void onRightClick(View view) {
        switch (view.getId()) {
            case R.id.linear_image_right2:
                mFucusIndex = 1;
                updateArrowHighlight(false, true);
                doLeftRight(mItemsPair2d3d, false);
                mSettingHelper.set2D3DMode(mItemsPair2d3d.getCurIndex());
                break;
            case R.id.linear_image_right5:
                mFucusIndex = 4;
                updateArrowHighlight(false, true);
      /*
       * doLeftRight(mItemsPairSndTrack, false);
       * mSettingHelper.setSoundTrack(mItemsPairSndTrack.getCurIndex());
       */
                doLeftRight(mItemsPairTrack, false);
                System.out.println("track index = " + mItemsPairTrack.getCurIndex());
                mSettingHelper.setAudioTrack(mItemsPairTrack.getCurIndex());
                break;
            case R.id.linear_image_right6:
                System.out
                        .println("R.id.linear_image_right6 &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                mFucusIndex = 5;
                updateArrowHighlight(false, true);
      /*
       * int index = mItemsPairSutitle.getCurIndex();
       * System.out.println("index = " + index); doLeftRight(mItemsPairSutitle,
       * false);
       * mSettingHelper.setSubtitleTrack(mItemsPairSutitle.getCurIndex());
       */
                int index = mItemsPairSutitle.getCurIndex();
                doLeftRight(mItemsPairSutitle, false);
                Log.e("leow", "onKeyLeftRight index=" + mItemsPairSutitle.getCurIndex());
                if (index != mItemsPairSutitle.getCurIndex()) {
                    mSettingHelper.setSubtitleTrack(mItemsPairSutitle.getCurIndex());
                }
                break;
            default:
                break;
        }
    }

    private void updateArrowHighlight(boolean isLeft, boolean isHightlight) {
        ImageView image = null;
        int imageLeft = 0;
        int imageRight = 0;
        switch (mFucusIndex) {
            case 1:
                imageLeft = R.id.linear_image_left2;
                imageRight = R.id.linear_image_right2;
                break;
            case 4:
                imageLeft = R.id.linear_image_left5;
                imageRight = R.id.linear_image_right5;
                break;
            case 5:
                imageLeft = R.id.linear_image_left6;
                imageRight = R.id.linear_image_right6;
                break;
            default:
                break;
        }
        if (0 != imageLeft) {
            updateItemHightlight(isLeft, isHightlight, imageLeft, imageRight);
        }
    }

    private void updateItemHightlight(boolean isLeft, boolean isHighlight,
                                      final int imageLeft, final int imageRight) {
        if (isLeft) {
            ImageView image = (ImageView) findViewById(imageLeft);
            if (null != image) {
                setLeftImage(image, isHighlight);
            }
        } else {
            ImageView image = (ImageView) findViewById(imageRight);
            if (null != image) {
                setRightImage(image, isHighlight);
            }
        }
    }

    private void doLeftRight(IndexStringPair pair, boolean isLeft) {
        if (isLeft) {
            pair.switchLeft();
        } else {
            pair.switchRight();
        }
    }
}