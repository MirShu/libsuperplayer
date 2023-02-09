package com.tencent.liteav.demo.play;


import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tencent.liteav.demo.play.controller.TCVodControllerBase;
import com.tencent.liteav.demo.play.v3.SuperPlayerModelWrapper;
import com.tencent.liteav.demo.play.view.FloatVideoPlayerView;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * Description:     MediaService
 * Author:         刘帅
 * CreateDate:     2022/1/14
 */
public class MediaService extends Service {
    private final int OP_SYSTEM_ALERT_WINDOW = 24;                      // 支持TYPE_TOAST悬浮窗的最高API版本
    public static final String TAG = "MediaService";
    private final IBinder mediaBinder = new LocalBinder();
    private FloatVideoPlayerView floatVideoPlayerView;
    private Intent activityIntent;
    private SuperPlayerModelWrapper superPlayerModel;

    // 点播播放器
    private TXVodPlayer txVodPlayer;
    // 直播播放器
    public TXLivePlayer txLivePlayer;

    private int currentPlayState = SuperPlayerConst.PLAYSTATE_PLAY;
    private int current;
    private int duration;


    public OnClickFloatCloseBtnListener onClickFloatCloseBtnListener;
    private Bundle bundle;

    /**
     * 设置超级播放器的回掉
     *
     * @param callback
     */
    public void setOnClickFloatCloseBtnListener(OnClickFloatCloseBtnListener callback) {
        onClickFloatCloseBtnListener = callback;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public void closeFloatPlayer() {
        if (onClickFloatCloseBtnListener != null) {
            onClickFloatCloseBtnListener.onClickFloatCloseBtn(current / 1000, bundle);
            onClickFloatCloseBtnListener = null;
        }
        stopPlay();
    }

    public interface OnClickFloatCloseBtnListener {

        /**
         * 点击悬浮窗模式下的x按钮
         */
        void onClickFloatCloseBtn(int progress, Bundle bundle);
    }

    public void setVodController(TCVodControllerBase.VodController vodController) {
        floatVideoPlayerView.setVodController(vodController);
    }

    public void hideFloatPlayer() {
        floatVideoPlayerView.hide();
    }

    public void showFloatPlayer(final int x, final int y) {
        floatVideoPlayerView.show(x, y);
    }

    public TXCloudVideoView getTxCloudVideoView() {
        return floatVideoPlayerView.getTxCloudVideoView();
    }

    public void release() {
        Log.d(TAG, "call release...");
        //floatVideoPlayerView.release();
    }

    public void updateVideoProgress(final int current, final int duration) {
        this.current = current;
        this.duration = duration;
    }

    public class LocalBinder extends Binder {
        public MediaService getService() {
            return MediaService.this;
        }
    }

    public int getCurrentPlayState() {
        return currentPlayState;
    }

    public void setCurrentPlayState(final int currentPlayState) {
        this.currentPlayState = currentPlayState;
        switch(currentPlayState){
            case SuperPlayerConst.PLAYSTATE_PAUSE:
                floatVideoPlayerView.setKeepScreenOn(false);
            break;
            case SuperPlayerConst.PLAYSTATE_PLAY:
                floatVideoPlayerView.setKeepScreenOn(true);
            break;
        }
    }

    /**
     * 获取悬浮窗中的视频播放view
     */
    public FloatVideoPlayerView getFloatVideoPlayerView() {
        return floatVideoPlayerView;
    }

    public static void bindMediaService(@NonNull Activity activity, ServiceConnection serviceConnection, UnbindServiceListener unbindServiceListener) {
        LifeCycleFragment
                .attach(activity)
                .addCallBack(new LifeCycleFragment.CallBack() {
                    @Override
                    public void onDestroy() {
                        unbindServiceListener.unbindService(serviceConnection);
                    }
                });
        final Intent startIntent = new Intent(activity, MediaService.class);
        activity.startService(startIntent);
        final Intent bindIntent = new Intent(activity, MediaService.class);
        activity.bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "call onCreate...");
        floatVideoPlayerView = new FloatVideoPlayerView(this);
        floatVideoPlayerView.setAnimalEnable(false);
    }

    public void setPlayerModel(final SuperPlayerModelWrapper superPlayerModel) {
        this.superPlayerModel = superPlayerModel;
    }

    public SuperPlayerModelWrapper getPlayerModel() {
        return superPlayerModel;
    }

    public void setCurrentIntent(Intent intent) {
        this.activityIntent = intent;
    }

    public void startMediaActivity() {
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.putExtra("isFromFloat", true);
        startActivity(activityIntent);
    }

    public TXVodPlayer getTXVodPlayer() {
        if (txVodPlayer == null){
            txVodPlayer = new TXVodPlayer(this);
        }
        return txVodPlayer;
    }

    public TXLivePlayer getTXLivePlayer() {
        if (txLivePlayer == null){
            txLivePlayer = new TXLivePlayer(this);
        }
        return txLivePlayer;
    }

    public void stopPlay() {
        if (txVodPlayer != null) {
            txVodPlayer.setVodListener(null);
            txVodPlayer.stopPlay(false);
        }
        if (txLivePlayer != null) {
            txLivePlayer.setPlayListener(null);
            txLivePlayer.stopPlay(false);
            floatVideoPlayerView.hide();
        }
        superPlayerModel = null;
        currentPlayState = SuperPlayerConst.PLAYSTATE_PAUSE;
        Log.d(TAG, "stopPlay mCurrentPlayState:" + currentPlayState);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "call onBind...");
        return mediaBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        Log.d(TAG, "call onDestroy...");

        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "call onStartCommand...");
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        Log.d(TAG, "call onTaskRemoved...");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "call onDestroy...");
        stopPlay();
    }

    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    interface UnbindServiceListener {
        void unbindService(final ServiceConnection serviceConnection);
    }
}