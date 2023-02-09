package com.tencent.liteav.demo.play.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.liteav.demo.play.MediaService;
import com.tencent.liteav.demo.play.R;
import com.tencent.liteav.demo.play.SuperPlayerConst;
import com.tencent.liteav.demo.play.controller.TCVodControllerBase;
import com.tencent.liteav.demo.play.utils.TCTimeUtils;
import com.tencent.liteav.demo.play.utils.VideoGestureUtil;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Description:     DraggableView
 * Author:         刘帅
 * CreateDate:     2022/1/17
 */
@SuppressLint("ViewConstructor")
public class FloatVideoPlayerView extends DraggableView implements TCPointSeekBar.OnSeekBarChangeListener  {

    private static final int MAX_SHIFT_TIME = 7200; // demo演示直播时移是MAX_SHIFT_TIMEs，即2小时
    public TCVodControllerBase.VodController vodController;
    protected GestureDetector mGestureDetector;
    private boolean isShowing;
    protected boolean mLockScreen;
    protected ArrayList<TCVideoQulity> mVideoQualityList;
    protected int mPlayType;
    protected long mLivePushDuration;
    protected String mTitle;

    public TextView mTvCurrent;
    public TextView mTvDuration;
    public TextView tvTime;
    public TCPointSeekBar mSeekBarProgress;
    protected LinearLayout mLayoutReplay;
    protected ProgressBar mPbLiveLoading;
    protected VideoGestureUtil mVideoGestureUtil;
    protected TCVolumeBrightnessProgressLayout mGestureVolumeBrightnessProgressLayout;
    protected TCVideoProgressLayout mGestureVideoProgressLayout;

    protected HideViewControllerViewRunnable mHideViewRunnable;
    protected boolean mIsChangingSeekBarProgress; // 标记状态，避免SeekBar由于视频播放的update而跳动
    protected boolean mFirstShowQuality;

    protected Bitmap mWaterMarkBmp;
    protected float mWaterMarkBmpX, mWaterMarkBmpY;
    private long duration;

    public int currentProgress;
    private TXCloudVideoView txCloudVideoView;
    private final MediaService mediaService;

    public TXCloudVideoView getTxCloudVideoView() {
        return txCloudVideoView;
    }

    public FloatVideoPlayerView(MediaService context) {
        this(context, null);
    }

    public FloatVideoPlayerView(MediaService context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatVideoPlayerView(MediaService context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mediaService = context;
        init();
        initView();
    }

    private void init() {

//        sharedPreferences = mContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        int mVodPlayerMute = sharedPreferences.getInt("click", 0);
        mHideViewRunnable = new HideViewControllerViewRunnable(this);
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mLockScreen) return false;
                changePlayState();
                show();
                if (mHideViewRunnable != null) {
                    getHandler().removeCallbacks(mHideViewRunnable);
                    getHandler().postDelayed(mHideViewRunnable, 7000);
                }
                return true;
            }


            //如果双击的话，则onSingleTapConfirmed不会执行
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                onToggleControllerView();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
                if (mLockScreen) return false;
                if (downEvent == null || moveEvent == null) {
                    return false;
                }
                if (mVideoGestureUtil != null && mGestureVolumeBrightnessProgressLayout != null) {
                    mVideoGestureUtil.check(mGestureVolumeBrightnessProgressLayout.getHeight(), downEvent, moveEvent, distanceX, distanceY);
                }
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                if (mLockScreen) return true;
                if (mVideoGestureUtil != null) {
                    mVideoGestureUtil.reset(getWidth(), mSeekBarProgress.getProgress());
                }
                return true;
            }

        });
        mGestureDetector.setIsLongpressEnabled(false);

        mVideoGestureUtil = new VideoGestureUtil(getContext());
        mVideoGestureUtil.setVideoGestureListener(new VideoGestureUtil.VideoGestureListener() {
            @Override
            public void onBrightnessGesture(float newBrightness) {
                if (mGestureVolumeBrightnessProgressLayout != null) {
                    mGestureVolumeBrightnessProgressLayout.setProgress((int) (newBrightness * 100));
                    mGestureVolumeBrightnessProgressLayout.setImageResource(R.drawable.ic_light_max);
                    mGestureVolumeBrightnessProgressLayout.show();
                }
            }

            @Override
            public void onVolumeGesture(float volumeProgress) {
                if (mGestureVolumeBrightnessProgressLayout != null) {
                    mGestureVolumeBrightnessProgressLayout.setImageResource(R.drawable.ic_volume_max);
                    mGestureVolumeBrightnessProgressLayout.setProgress((int) volumeProgress);
                    mGestureVolumeBrightnessProgressLayout.show();
                }
            }

            @Override
            public void onSeekGesture(int progress) {
                if (duration == 0) return;
                mIsChangingSeekBarProgress = true;
                if (mGestureVideoProgressLayout != null) {

                    if (progress > mSeekBarProgress.getMax()) {
                        progress = mSeekBarProgress.getMax();
                    }
                    if (progress < 0) {
                        progress = 0;
                    }
                    mGestureVideoProgressLayout.setProgress(progress);
                    mGestureVideoProgressLayout.show();

                    float percentage = ((float) progress) / mSeekBarProgress.getMax();
                    float currentTime = (vodController.getDuration() * percentage);

                    Log.e("MAX_SHIFT_TIME", String.valueOf(currentTime));
                    if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE || mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
                        if (mLivePushDuration > MAX_SHIFT_TIME) {
                            currentTime = (int) (mLivePushDuration - MAX_SHIFT_TIME * (1 - percentage));
                        } else {
                            currentTime = mLivePushDuration * percentage;
                        }
                        mGestureVideoProgressLayout.setTimeText(TCTimeUtils.formattedTime((long) currentTime));
                    } else {
                        mGestureVideoProgressLayout.setTimeText(TCTimeUtils.formattedTime((long) currentTime) + " / " + TCTimeUtils.formattedTime((long) vodController.getDuration()));
                    }
                    onGestureVideoProgress(progress);

                }
                if (mSeekBarProgress != null)
                    mSeekBarProgress.setProgress(progress);
            }
        });
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_draggle_float_player, this);
        txCloudVideoView = findViewById(R.id.superplayer_float_cloud_video_view);
        setOnClickListener(v -> {
            if (vodController != null) {
                vodController.onRequestPlayMode(SuperPlayerConst.PLAYMODE_WINDOW);
            }else {
                mediaService.startMediaActivity();
            }
        });
        findViewById(R.id.superplayer_iv_close).setOnClickListener(v -> {
            //设置点击事件监听，实现点击关闭按钮后关闭悬浮窗
            if (vodController != null) {
                vodController.onBackPress();
            }else {
                mediaService.closeFloatPlayer();
            }
        });
    }

//    public TXCloudVideoView getTxCloudVideoView() {
//        return txCloudVideoView;
//    }

    public void setVideoQualityList(ArrayList<TCVideoQulity> videoQualityList) {
        mVideoQualityList = videoQualityList;
        mFirstShowQuality = false;
    }

    /**
     * 设置明文水印
     *
     * @param bmp 水印内容
     * @param x   归一化坐标: 水印中心点x坐标
     * @param y   归一化坐标: 水印中心点y坐标
     *            例子: x,y = 0.5 那么水印将放在播放视频的正中间
     */
    public void setWaterMarkBmp(Bitmap bmp, float x, float y) {
        mWaterMarkBmp = bmp;
        mWaterMarkBmpY = y;
        mWaterMarkBmpX = x;
    }

    public void updateTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            mTitle = title;
        } else {
            mTitle = " ";
        }
    }

    public void updateVideoProgress(long current, long duration) {
        if (current < 0) {
            current = 0;
        }
        if (duration < 0) {
            duration = 0;
        }
        this.duration = duration;
        if (mTvCurrent != null) mTvCurrent.setText(TCTimeUtils.formattedTime(current));

        if (tvTime != null) tvTime.setText(TCTimeUtils.formattedTime(duration-current));

//        Log.d("TCTimeUtils", "" + duration);
        float percentage = duration > 0 ? ((float) current / (float) duration) : 1.0f;
        if (current == 0) {
            mLivePushDuration = 0;
            percentage = 0;
        }
        if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE || mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
            mLivePushDuration = mLivePushDuration > current ? mLivePushDuration : current;
            long leftTime = duration - current;
            if (duration > MAX_SHIFT_TIME) {
                duration = MAX_SHIFT_TIME;
            }
            percentage = 1 - (float) leftTime / (float) duration;
        }

        if (percentage >= 0 && percentage <= 1) {
            if (mSeekBarProgress != null) {
                int progress = Math.round(percentage * mSeekBarProgress.getMax());
                if (!mIsChangingSeekBarProgress)
                    mSeekBarProgress.setProgress(progress);
            }
            if (mTvDuration != null) mTvDuration.setText(TCTimeUtils.formattedTime(duration));
        }


        //Log.e("TCTimeUtils", String.valueOf(TCTimeUtils.formattedTime(duration)));
        // 流量（kb）=  观看音/视频时长（s）*当前音/视频码率（kbps）/8
        //Log.e("TCTimeUtils----", String.valueOf(44 * 512 / 8));

    }

    public void setVodController(TCVodControllerBase.VodController vodController) {
        this.vodController = vodController;
    }

    @Override
    public void onProgressChanged(TCPointSeekBar seekBar, int progress, boolean isFromUser) {
        currentProgress = progress;
        if (mGestureVideoProgressLayout != null && isFromUser) {
            mGestureVideoProgressLayout.show();
            float percentage = ((float) progress) / seekBar.getMax();
            float currentTime = (vodController.getDuration() * percentage);
            if (mPlayType == SuperPlayerConst.PLAYTYPE_LIVE || mPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {
                if (mLivePushDuration > MAX_SHIFT_TIME) {
                    currentTime = (int) (mLivePushDuration - MAX_SHIFT_TIME * (1 - percentage));
                } else {
                    currentTime = mLivePushDuration * percentage;
                }
                mGestureVideoProgressLayout.setTimeText(TCTimeUtils.formattedTime((long) currentTime));
            } else {
                mGestureVideoProgressLayout.setTimeText(TCTimeUtils.formattedTime((long) currentTime) + " / " + TCTimeUtils.formattedTime((long) vodController.getDuration()));
            }
            mGestureVideoProgressLayout.setProgress(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(TCPointSeekBar seekBar) {
        this.getHandler().removeCallbacks(mHideViewRunnable);
    }

    @Override
    public void onStopTrackingTouch(TCPointSeekBar seekBar) {
        // 拖动seekbar结束时,获取seekbar当前进度,进行seek操作,最后更新seekbar进度
        int curProgress = seekBar.getProgress();
        int maxProgress = seekBar.getMax();

        switch (mPlayType) {
            case SuperPlayerConst.PLAYTYPE_VOD:
                if (curProgress >= 0 && curProgress <= maxProgress) {
                    // 关闭重播按钮
                    updateReplay(false);
                    float percentage = ((float) curProgress) / maxProgress;
                    int position = (int) (vodController.getDuration() * percentage);
                    vodController.seekTo(position);
                    vodController.resume();
                }
                break;
            case SuperPlayerConst.PLAYTYPE_LIVE:
            case SuperPlayerConst.PLAYTYPE_LIVE_SHIFT:
                updateLiveLoadingState(true);
                int seekTime = (int) (mLivePushDuration * curProgress * 1.0f / maxProgress);
                if (mLivePushDuration > MAX_SHIFT_TIME) {
                    seekTime = (int) (mLivePushDuration - MAX_SHIFT_TIME * (maxProgress - curProgress) * 1.0f / maxProgress);
                }
                vodController.seekTo(seekTime);
                break;
        }
        this.getHandler().postDelayed(mHideViewRunnable, 7000);
    }

    public void updateReplay(boolean replay) {
        if (mLayoutReplay != null) {
            mLayoutReplay.setVisibility(replay ? View.VISIBLE : View.GONE);
        }
    }

    public void updateLiveLoadingState(boolean loading) {
        if (mPbLiveLoading != null) {
            mPbLiveLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 重新播放
     */
    protected void replay() {
        updateReplay(false);
        vodController.onReplay();
    }


    /**
     * 切换播放状态
     */
    public void changePlayState() {
        // 播放中
        if (vodController.isPlaying()) {
            vodController.pause();
            show();
        }
        // 未播放
        else if (!vodController.isPlaying()) {
            updateReplay(false);
            vodController.resume();
            show();
        }
    }


    /**
     * 判断是否是wifi连接
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null)
            return false;
        return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;

    }

    protected void onToggleControllerView() {
        if (!mLockScreen) {
            if (isShowing) {
                hide();
            } else {
                show();
                if (mHideViewRunnable != null) {
                    this.getHandler().removeCallbacks(mHideViewRunnable);
                    this.getHandler().postDelayed(mHideViewRunnable, 7000);
                }
            }
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void show(int x, int y) {
        super.show(x, y);
        isShowing = true;
    }

    @Override
    public void show() {
        super.show();
        isShowing = true;
    }

    @Override
    public void hide() {
        txCloudVideoView.removeVideoView();
        isShowing = false;
        super.hide();
    }

    public void release() {
        hide();
        if (vodController != null) {
            vodController.release();
        }
    }


    protected void setBitmap(ImageView view, Bitmap bitmap) {
        if (view == null || bitmap == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(new BitmapDrawable(getContext().getResources(), bitmap));
        } else {
            view.setBackgroundDrawable(new BitmapDrawable(getContext().getResources(), bitmap));
        }
    }

    public void updatePlayType(int playType) {
        mPlayType = playType;
    }


    protected void onGestureVideoProgress(int currentProgress) {

    }


    private static class HideViewControllerViewRunnable implements Runnable {
        public WeakReference<FloatVideoPlayerView> mWefControlBase;

        public HideViewControllerViewRunnable(FloatVideoPlayerView base) {
            mWefControlBase = new WeakReference<>(base);
        }

        @Override
        public void run() {
            if (mWefControlBase != null && mWefControlBase.get() != null) {
                mWefControlBase.get().hide();
            }
        }
    }

}
