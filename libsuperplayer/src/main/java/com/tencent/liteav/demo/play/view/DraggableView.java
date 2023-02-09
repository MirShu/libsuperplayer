package com.tencent.liteav.demo.play.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

/**
 * Description:     DraggableView
 * Author:         刘帅
 * CreateDate:     2022/1/17
 */
public class DraggableView extends FrameLayout {

    private static final String TAG = "DraggableView";
    private final WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;
    //水平方向加速度 单位：像素/毫秒²
    private float a = 0.098f;
    private float xInView;
    private float yInView;
    private boolean isDrag;
    private final int statusBarHeight;
    private ValueAnimator animate;
    private long downTime;
    private final int screenWidth;
    private final int screenHeight;
    private boolean isAnimalEnable = true;
    private float downX;
    private float downY;
    private long oldTime;
    private float vX;
    private float vY;
    private long oldPlayTime;
    private boolean isClick;

    public DraggableView(Context context) {
        this(context, null);
    }

    public DraggableView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DraggableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (getId() == View.NO_ID) {
            setId(View.generateViewId());
        }
        statusBarHeight = getStatusBarHeight();
        windowManager = (WindowManager) getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowParams = getWindowParams();
        final DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
    }

    /**
     * 设置坐标并显示在window上
     */
    public void show(int x, int y) {
        windowParams.x = x;
        windowParams.y = y;
        show();
    }

    /**
     * 显示
     */
    public void show() {
        windowManager.addView(this, windowParams);
    }

    /**
     * 隐藏
     */
    public void hide() {
        if (isAttachedToWindow()){
            windowManager.removeView(this);
        }
    }

    public void setAnimalEnable(boolean isEnable) {
        isAnimalEnable = isEnable;
    }

    public boolean isAnimalEnable() {
        return isAnimalEnable;
    }


    public WindowManager.LayoutParams getWindowParams() {
        if (windowParams == null) {
            windowParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                windowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                windowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            windowParams.format = PixelFormat.TRANSLUCENT;
            windowParams.gravity = Gravity.START | Gravity.TOP;
            windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        return windowParams;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        final int pointerCount = event.getPointerCount();
        super.onTouchEvent(event);
        if (this.isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isDrag = false;
                    isClick = false;
                    xInView = event.getX();
                    yInView = event.getY();
                    downTime = System.currentTimeMillis();
                    oldTime = System.currentTimeMillis();
                    downX = event.getRawX();
                    downY = event.getRawY() - statusBarHeight;
                    if (animate != null) {
                        animate.cancel();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveView(event);
                    break;
                case MotionEvent.ACTION_UP:
                    performClick();
                    downTime = 0;
                case MotionEvent.ACTION_CANCEL:
                    if (isAnimalEnable) {
                        moveToBorder();
                    }
                    break;
            }
            return true;
        }
        return false;
    }

    private void moveView(MotionEvent event) {
        final float xInScreen = event.getRawX();
        final float yInScreen = event.getRawY() - statusBarHeight;
        final float xDistance = xInScreen - downX;
        final float yDistance = yInScreen - downY;
        final long currentTime = System.currentTimeMillis();
        final long time = currentTime - oldTime;
        oldTime = currentTime;
        if (!isDrag && Math.abs(xDistance) > 10 || Math.abs(yDistance) > 10 || (currentTime - downTime) > 200) {
            isDrag = true;
        }
        final int x = (int) (xInScreen - xInView);
        final int y = (int) (yInScreen - yInView);
        int oldX = windowParams.x;
        int oldY = windowParams.y;
        final int maxX = screenWidth - getWidth();
        final int maxY = screenHeight - getHeight();
        windowParams.x = x < maxX ? Math.max(x, 0) : maxX;
        windowParams.y = y < maxY ? Math.max(y, 0) : maxY;
        final int dX = windowParams.x - oldX;
        final int dY = windowParams.y - oldY;

        vX = dX / (float) time;
        vY = dY / (float) time;
        windowManager.updateViewLayout(this, windowParams);
    }

    public void moveToBorder() {
        final int maxX = screenWidth - getWidth();
        if (windowParams.x <= 0 || windowParams.x >= maxX) return;
        oldPlayTime = 0;
        if (animate == null) {
            animate = new ValueAnimator();
            animate.addUpdateListener(valueAnimator -> {
                final long currentPlayTime = valueAnimator.getCurrentPlayTime();
                final long dTime = currentPlayTime <= 0? 0 : currentPlayTime - oldPlayTime;
                oldPlayTime = currentPlayTime;
                windowParams.x = (int) (float) valueAnimator.getAnimatedValue() + (int) ((a * currentPlayTime + vX) * dTime);
                windowParams.y = windowParams.y + (int) (vY * dTime);
                windowManager.updateViewLayout(this, windowParams);
            });
        }
        final double t;
        final int s;
        if (windowParams.x < maxX >> 1) {
            a = -Math.abs(a);
            s = windowParams.x;
            animate.setFloatValues(windowParams.x, 0);
            final double d = Math.pow(vX, 2) - 4 * a * s;
            t = -vX - Math.sqrt(d);
        } else {
            a = Math.abs(a);
            s = -(maxX - windowParams.x);
            animate.setFloatValues(windowParams.x, maxX);
            final double d = Math.pow(vX, 2) - 4 * a * s;
            t = -vX + Math.sqrt(d);
        }
        final long duration = (long) (t / (2 * a));
        Log.d(TAG, "moveToBorder: duration = " + duration);
        animate.setDuration(duration);
        animate.start();
    }

    @Override
    public boolean performClick() {
        if (isDrag || isClick) {
            return false;
        } else {
            isClick = true;
            return super.performClick();
        }
    }
}
