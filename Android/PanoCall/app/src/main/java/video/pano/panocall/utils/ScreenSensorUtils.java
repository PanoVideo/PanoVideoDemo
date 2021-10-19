package video.pano.panocall.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.view.OrientationEventListener;

import video.pano.panocall.listener.OnOrientationChangeListener;

public class ScreenSensorUtils {

    public static final long DURATION = 1000 ;

    private OrientationListener2 mOrientationListener;
    private boolean mAutoOrientation = true;
    private int mUserOrientation = -1;
    private int mNewOrientation;
    private long mLastTimeMillis = 0L ;

    private static class Holder {
        private static final ScreenSensorUtils INSTANCE = new ScreenSensorUtils();
    }

    public static ScreenSensorUtils getIns() {
        return ScreenSensorUtils.Holder.INSTANCE;
    }

    private ScreenSensorUtils() {
    }

    public void registerSensorManager(OnOrientationChangeListener listener) {
        mOrientationListener = new OrientationListener2(Utils.getApp(), listener);
        mOrientationListener.enable();
    }

    public void unregisterSensorManager() {
        if (mOrientationListener != null) {
            mOrientationListener.unregisterListener();
            mOrientationListener.disable();
        }
    }

    public void autoOrientation(boolean auto,int orientation){
        mAutoOrientation = auto;
        mUserOrientation = orientation;
    }

    public boolean isAutoOrientation() {
        return mAutoOrientation;
    }

    public long getLastTimeMillis() {
        return mLastTimeMillis;
    }

    public void setLastTimeMillis(long lastTimeMillis) {
        this.mLastTimeMillis = lastTimeMillis;
    }

    /**
     * 1 手机已开启屏幕旋转功能
     * 0 手机未开启屏幕旋转功能
     */
    private boolean isOrientationLock() {
        try {
            return Settings.System.getInt(Utils.getApp().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION) == 0;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    class OrientationListener2 extends OrientationEventListener {

        private OnOrientationChangeListener mListener;

        public OrientationListener2(Context context, OnOrientationChangeListener changeListener) {
            super(context);
            mListener = changeListener;
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if(isOrientationLock()){
                return ;
            }
            if(orientation == OrientationListener2.ORIENTATION_UNKNOWN ){
                return ;
            }

            //记录用户手机上一次放置的位置
            int mLastOrientation = mNewOrientation;

            //只检测是否有四个角度的改变
            if (orientation > 350 || orientation < 10) {
                //0度，竖直
                mNewOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else if (orientation > 80 && orientation < 100) {
                //90度，右侧横屏
                mNewOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            } else if (orientation > 170 && orientation < 190) {
                //180度，反向竖直
                mNewOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else if (orientation > 260 && orientation < 280) {
                //270度，左侧横屏
                mNewOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }

            if (mLastOrientation == mNewOrientation) {
                return ;
            }

            if(!mAutoOrientation && mUserOrientation == mNewOrientation){
                mAutoOrientation = true ;
            }
            if (mListener != null) {
                long currentTimeMillis = System.currentTimeMillis() ;
                if(currentTimeMillis - mLastTimeMillis < DURATION){
                    return ;
                }
                
                mListener.orientationChanged(mNewOrientation);
                mLastTimeMillis = currentTimeMillis ;
            }
        }

        public void unregisterListener() {
            if (mListener != null) {
                mListener = null;
            }
        }
    }
}
