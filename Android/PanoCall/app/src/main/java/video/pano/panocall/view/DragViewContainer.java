package video.pano.panocall.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import video.pano.panocall.R;
import video.pano.panocall.info.Config;
import video.pano.panocall.utils.Utils;

/**
 * 自由拖动并且贴边的ViewGroup
 */
public class DragViewContainer extends ConstraintLayout {

    private static final String TAG = "DragViewContainer";

    private static final int DURATION = 500;
    private static final int DISTANCE = 30;
    //手指按下位置
    private float mLastRawX, mLastRawY;
    private int mScreenWidth, mScreenHeight, mWidth, mHeight;
    private int mMarginTop, mMarginBottom;

    public DragViewContainer(@NonNull Context context) {
        this(context, null);
    }

    public DragViewContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public DragViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initDragView();
    }

    /**
     * 初始化自定义属性
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.DragViewContainer);
        mMarginTop = mTypedArray.getInt(R.styleable.DragViewContainer_dragMarginTop, 0);
        mMarginBottom = mTypedArray.getInt(R.styleable.DragViewContainer_dragMarginBottom, 0);
        mTypedArray.recycle();
    }

    private void initDragView() {
        Utils.getApp().getResources().getConfiguration();
        Resources resources = Utils.getApp().getResources();
        if (resources != null && resources.getConfiguration() != null
                && resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mScreenHeight = Config.sScreenWidth;
            mScreenWidth = Config.sScreenHeight;
        } else {
            mScreenHeight = Config.sScreenHeight;
            mScreenWidth = Config.sScreenWidth;
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mScreenHeight = Config.sScreenWidth;
            mScreenWidth = Config.sScreenHeight;
        } else {
            mScreenHeight = Config.sScreenHeight;
            mScreenWidth = Config.sScreenWidth;
        }

        int bottomDistance = mScreenHeight - mMarginBottom - mHeight - DISTANCE;
        int topDistance = mMarginTop;
        int rightDistance = mScreenWidth - mWidth - DISTANCE;
        int leftDistance = DISTANCE;
        int centerX = mScreenWidth / 2;
        int centerY = mScreenHeight / 2;

        float rawX = mLastRawX;
        float rawY = mLastRawY;

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rawX = mLastRawY;
            rawY = mLastRawX;
        }

        if (rawX <= 0 && rawY <= 0) {
            animate().setInterpolator(new OvershootInterpolator())
                    .setDuration(DURATION)
                    .x(rightDistance)
                    .start();

            animate().setInterpolator(new OvershootInterpolator())
                    .setDuration(DURATION)
                    .y(topDistance)
                    .start();
        } else {
            if (rawX <= centerX) {
                //向左贴边
                animate().setInterpolator(new OvershootInterpolator())
                        .setDuration(DURATION)
                        .x(leftDistance)
                        .start();
            }
            //向右贴边
            if (rawX > centerX) {
                animate().setInterpolator(new OvershootInterpolator())
                        .setDuration(DURATION)
                        .x(rightDistance)
                        .start();
            }

            if (rawY <= centerY) {
                //向上贴边
                animate().setInterpolator(new OvershootInterpolator())
                        .setDuration(DURATION)
                        .y(topDistance)
                        .start();
            }
            if (rawY > centerY) {
                //向下贴边
                animate().setInterpolator(new OvershootInterpolator())
                        .setDuration(DURATION)
                        .y(bottomDistance)
                        .start();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        Log.d(TAG, "onMeasure mWidth = " + mWidth + " , mHeight = " + mHeight);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isEnabled()) {
            //当前手指的坐标
            float rawX = event.getRawX();
            float rawY = event.getRawY();
            float y = getY();
            float x = getX();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN://手指按下
                    //记录按下的位置
                    mLastRawX = rawX;
                    mLastRawY = rawY;
                    break;
                case MotionEvent.ACTION_MOVE://手指滑动
//                    Log.d(TAG,"Move rawX = "+rawX+", rawY = "+rawY);
                    if (rawX >= 0 && rawX <= mScreenWidth && rawY >= 0 && rawY <= mScreenHeight) {
                        //手指X轴滑动距离
                        float differenceValueX = rawX - mLastRawX;
                        float differenceValueY = rawY - mLastRawY;
                        float endX = x + differenceValueX;
                        float endY = y + differenceValueY;
                        float maxX = mScreenWidth - mWidth;
                        float maxY = mScreenHeight - mHeight;
                        //X轴边界限制
                        endX = endX < 0 ? 0 : Math.min(endX, maxX);
                        //Y轴边界限制
                        endY = endY < 0 ? 0 : Math.min(endY, maxY);
//                        Log.d(TAG,"Move endX = "+rawX+", endY = "+rawY+" , maxX = "+maxX+" , maxY = "+maxY);
                        //开始移动
                        setX(endX);
                        setY(endY);
                        //记录位置
                        mLastRawX = rawX;
                        mLastRawY = rawY;
                    }
                    break;
                case MotionEvent.ACTION_UP://手指离开
                    //根据自定义属性判断是否需要贴边
                    int centerX = mScreenWidth / 2;

                    int bottomDistance = mScreenHeight - mMarginBottom - mHeight - DISTANCE;
                    int topDistance = mMarginTop;
                    int rightDistance = mScreenWidth - mWidth - DISTANCE;
                    int leftDistance = DISTANCE;
                    //自动贴边
                    if (mLastRawX <= centerX) {
                        //向左贴边
                        animate().setInterpolator(new OvershootInterpolator())
                                .setDuration(DURATION)
                                .x(leftDistance)
                                .start();
                    }
                    if (mLastRawX > centerX) {
                        //向右贴边
                        animate().setInterpolator(new OvershootInterpolator())
                                .setDuration(DURATION)
                                .x(rightDistance)
                                .start();
                    }

                    if (y <= topDistance) {
                        //向上贴边
                        animate().setInterpolator(new OvershootInterpolator())
                                .setDuration(DURATION)
                                .y(topDistance)
                                .start();
                    }
                    if (y > bottomDistance) {
                        //向下贴边
                        animate().setInterpolator(new OvershootInterpolator())
                                .setDuration(DURATION)
                                .y(bottomDistance)
                                .start();
                    }
                    requestLayout();
                    break;
                default:
                    break;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
