package com.tencent.liteav.demo.play.controller;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.play.R;
import com.tencent.liteav.demo.play.SuperPlayerConst;
import com.tencent.liteav.demo.play.utils.TCTimeUtils;
import com.tencent.liteav.demo.play.view.TCPointSeekBar;
import com.tencent.liteav.demo.play.view.TCVideoProgressLayout;
import com.tencent.liteav.demo.play.view.TCVodMoreView;
import com.tencent.liteav.demo.play.view.TCVolumeBrightnessProgressLayout;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

/**
 * Created by liyuejiao on 2018/7/3.
 * <p>
 * 超级播放器小窗口控制界面
 */
public class TCVodControllerSmall extends TCVodControllerBase implements View.OnClickListener, TCVodMoreView.Callback {
    private static final String TAG = "TCVodControllerSmall";
    public LinearLayout mLayoutTop;
    public LinearLayout mLayoutBottom;
    public ImageView mIvPause;
    public ImageView ivBack;
    public ImageView mIvFullScreen;
    private ImageView iconPlay;
    private TextView mTvTitle;
    private TextView mTvBackToLive;
    private ImageView mBackground;
    private Bitmap mBackgroundBmp;
    private ImageView mIvWatermark;
    private boolean isClick = false;
    private TCVodMoreView mVodMoreView;
    public RelativeLayout rlVideoButtomMute;
    public ImageView ivMuteFalse;
    public ImageView ivMuteTrue;
    public ImageView mIvMore;
    private boolean isHideTopBar = false;
    private SharedPreferences sharedPreferences;


    public TCVodControllerSmall(Context context) {
        super(context);
        initViews();
    }

    public TCVodControllerSmall(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public TCVodControllerSmall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    /**
     * 显示播放控制界面
     */
    @Override
    void onShow() {
        if (!isHideTopBar) {
            mLayoutTop.setVisibility(View.VISIBLE);
        }
        mLayoutBottom.setVisibility(View.VISIBLE);
        mVodMoreView.setVisibility(GONE);
        mIvMore.setVisibility(VISIBLE);
        if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
            mTvBackToLive.setVisibility(View.VISIBLE);
        }

    }


    /**
     * 隐藏播放控制界面
     */
    @Override
    void onHide() {
        mLayoutTop.setVisibility(View.GONE);
        mLayoutBottom.setVisibility(View.GONE);
        if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
            mTvBackToLive.setVisibility(View.GONE);
        }
    }

    /**
     * 隐藏顶部导航栏
     */
    public void hideTopBar() {
        mLayoutTop.setVisibility(View.GONE);
        isHideTopBar = true;
    }

    /**
     * 显示顶部导航栏
     */
    public void showTopBar() {
        mLayoutTop.setVisibility(View.VISIBLE);
        isHideTopBar = false;
    }

    /**
     * 隐藏返回键
     */
    public void hideBackButton() {
        ivBack.setVisibility(GONE);
    }

    /**
     * 显示返回键
     */
    public void showBackButton() {
        ivBack.setVisibility(VISIBLE);
    }

    private void initViews() {
        mLayoutInflater.inflate(R.layout.vod_controller_small, this);
        mLayoutTop = (LinearLayout) findViewById(R.id.layout_top);
        rlVideoButtomMute = (RelativeLayout) findViewById(R.id.rl_video_buttom_mute);
        ivMuteFalse = (ImageView) findViewById(R.id.iv_mute_false);
        ivMuteTrue = (ImageView) findViewById(R.id.iv_mute_true);

        tvTime = (TextView) findViewById(R.id.tv_time);

        mLayoutBottom = (LinearLayout) findViewById(R.id.layout_bottom);
        mLayoutBottom.setOnClickListener(this);
        mLayoutReplay = (LinearLayout) findViewById(R.id.layout_replay);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mIvPause = (ImageView) findViewById(R.id.iv_pause);
        ivBack = findViewById(R.id.iv_back);
        iconPlay = (ImageView) findViewById(R.id.icon_play);
        mTvCurrent = (TextView) findViewById(R.id.tv_current);
        mTvDuration = (TextView) findViewById(R.id.tv_duration);
        tvTime = (TextView) findViewById(R.id.tv_time);

        mSeekBarProgress = (TCPointSeekBar) findViewById(R.id.seekbar_progress);
        mSeekBarProgress.setProgress(0);
        mSeekBarProgress.setMax(100);
        mIvFullScreen = (ImageView) findViewById(R.id.iv_fullscreen);
        mTvBackToLive = (TextView) findViewById(R.id.tv_backToLive);
        mPbLiveLoading = (ProgressBar) findViewById(R.id.pb_live);

        mTvBackToLive.setOnClickListener(this);
        mIvPause.setOnClickListener(this);
        iconPlay.setOnClickListener(this);
        mIvFullScreen.setOnClickListener(this);
        mLayoutReplay.setOnClickListener(this);

        mSeekBarProgress.setOnSeekBarChangeListener(this);

        mGestureVolumeBrightnessProgressLayout = (TCVolumeBrightnessProgressLayout) findViewById(R.id.gesture_progress);

        mGestureVideoProgressLayout = (TCVideoProgressLayout) findViewById(R.id.video_progress_layout);

        mBackground = (ImageView) findViewById(R.id.small_iv_background);
        setBackground(mBackgroundBmp);
        mIvWatermark = (ImageView) findViewById(R.id.small_iv_water_mark);

        mVodMoreView = (TCVodMoreView) findViewById(R.id.vodMoreView);
        mVodMoreView.setCallback(this);

        mIvMore = (ImageView) findViewById(R.id.iv_more);
        mIvMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                mVodMoreView.setVisibility(View.VISIBLE);
                mIvMore.setVisibility(GONE);
            }
        });
    }


    public void setBackground(final Bitmap bitmap) {
        this.post(new Runnable() {
            @Override
            public void run() {
                if (bitmap == null) return;
                if (mBackground == null) {
                    mBackgroundBmp = bitmap;
                } else {
                    setBitmap(mBackground, mBackgroundBmp);
                }
            }
        });
    }

    public void dismissBackground() {
        this.post(new Runnable() {
            @Override
            public void run() {
                if (mBackground.getVisibility() != View.VISIBLE) return;
                ValueAnimator alpha = ValueAnimator.ofFloat(1.0f, 0.0f);
                alpha.setDuration(500);
                alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (Float) animation.getAnimatedValue();
                        mBackground.setAlpha(value);
                        if (value == 0) {
                            mBackground.setVisibility(GONE);
                        }
                    }
                });
                alpha.start();
            }
        });
    }

    public void showBackground() {
        this.post(new Runnable() {
            @Override
            public void run() {
                ValueAnimator alpha = ValueAnimator.ofFloat(0.0f, 1);
                alpha.setDuration(500);
                alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (Float) animation.getAnimatedValue();
                        mBackground.setAlpha(value);
                        if (value == 1) {
                            mBackground.setVisibility(VISIBLE);
                        }
                    }
                });
                alpha.start();
            }
        });
    }


    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.iv_back) {
            if (mVodController != null) {
                mVodController.onBackPress();
            }
        } else if (i == R.id.iv_pause) {
            EventBus.getDefault().post(String.valueOf(mVodController.getProgress()));
            isClick = true;
            changePlayState();
            if (mVodController != null) {
                mVodController.play();
            }
        } else if (i == R.id.iv_fullscreen) {
            fullScreen();

        } else if (i == R.id.layout_replay) {
            replay();

        } else if (i == R.id.tv_backToLive) {
            backToLive();

        }
    }

    /**
     * 返回直播
     */
    private void backToLive() {
        mVodController.resumeLive();
    }

    /**
     * 全屏
     */
    private void fullScreen() {
        mVodController.onRequestPlayMode(SuperPlayerConst.PLAYMODE_FULLSCREEN);
    }


    /**
     * 更新播放UI
     *
     * @param isStart
     */
    public void updatePlayState(boolean isStart, boolean isDialogClick) {
        if (isDialogClick) {
            isClick = true;
        }

        // 播放中
        if (isStart) {
            mIvPause.setImageResource(R.drawable.ic_vod_pause_normal);
            if (!isClick) {
//                changePlayState(); //首次非自动播放  自动播放取消此方法即可
            }

            if (isDialogClick) {
                changePlayState();
            }
        }
        // 未播放
        else {
            mIvPause.setImageResource(R.drawable.ic_vod_play_normal);
        }

    }

    /**
     * 更新标题
     *
     * @param title
     */
    public void updateTitle(String title) {
        super.updateTitle(title);
        mTvTitle.setText(mTitle);
    }


    /**
     * 更新播放类型
     *
     * @param playType
     */
    public void updatePlayType(int playType) {
        TXCLog.i(TAG, "updatePlayType playType:" + playType);

        super.updatePlayType(playType);
        switch (playType) {
            case SuperPlayerConst.PLAYTYPE_VOD:
                mTvBackToLive.setVisibility(View.GONE);
                mTvDuration.setVisibility(View.VISIBLE);
                break;
            case SuperPlayerConst.PLAYTYPE_LIVE:
                mTvBackToLive.setVisibility(View.GONE);
                mTvDuration.setVisibility(View.GONE);
                mSeekBarProgress.setProgress(100);
                break;
            case SuperPlayerConst.PLAYTYPE_LIVE_SHIFT:
                if (mLayoutBottom.getVisibility() == VISIBLE)
                    mTvBackToLive.setVisibility(View.VISIBLE);
                mTvDuration.setVisibility(View.GONE);
                break;
        }
    }


    @Override
    public void setWaterMarkBmp(final Bitmap bmp, final float xR, final float yR) {
        super.setWaterMarkBmp(bmp, xR, yR);
        if (bmp != null) {
            this.post(new Runnable() {
                @Override
                public void run() {
                    int width = TCVodControllerSmall.this.getWidth();
                    int height = TCVodControllerSmall.this.getHeight();

                    int x = (int) (width * xR) - bmp.getWidth() / 2;
                    int y = (int) (height * yR) - bmp.getHeight() / 2;

                    mIvWatermark.setX(x);
                    mIvWatermark.setY(y);

                    mIvWatermark.setVisibility(VISIBLE);
                    setBitmap(mIvWatermark, bmp);
                }
            });
        } else {
            mIvWatermark.setVisibility(GONE);
        }
    }


    /**
     * Log.e("TCTimeUtils", String.valueOf(TCTimeUtils.formattedTime(duration)));
     * // 流量（kb）=  观看音/视频时长（s）*当前音/视频码率（kbps）/8
     * Log.e("TCTimeUtils----", String.valueOf(44 * 512 / 8));
     */
    public void getmCurrentPlayVideoURL(String flow) {
        if (!isWifi(getContext())) {
            Log.e("Consumptog", flow);
//            ConsumptionFlowOBAlertDialog noticeDialogAbutton = new ConsumptionFlowOBAlertDialog(getContext(),flow);
//            noticeDialogAbutton.setSure(new ConsumptionFlowOBAlertDialog.DialogSure() {
//                @Override
//                public void onSureResult(ConsumptionFlowOBAlertDialog dialog, boolean flag) {
//                    if (flag) {
//                        dialog.dismiss();
//                        isClick = true;
//                        changePlayState();
//                    }
//                }
//            });
//            noticeDialogAbutton.show();
        }
    }

    @Override
    public void onSpeedChange(float speedLevel) {
        mVodController.onSpeedChange(speedLevel);
    }

    @Override
    public void onMirrorChange(boolean isMirror) {
        mVodController.onMirrorChange(isMirror);
    }

    @Override
    public void onHWAcceleration(boolean isAccelerate) {
        mVodController.onHWAcceleration(isAccelerate);
    }
}
