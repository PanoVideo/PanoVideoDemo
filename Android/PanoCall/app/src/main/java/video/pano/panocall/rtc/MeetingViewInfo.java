package video.pano.panocall.rtc;

import android.view.ViewGroup;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.pano.rtc.api.IVideoRender;
import com.pano.rtc.api.RtcWbView;

import java.util.Objects;

import video.pano.panocall.listener.OnPanoTouchListener;
import video.pano.panocall.model.UserInfo;

public class MeetingViewInfo implements LifecycleObserver {

    private UserInfo mUserInfo;
    private AbsInfoViewComponent mInfoView;
    private AbsOperateComponent mOperate;

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        abandon();
    }

    public void addInfoComponent(AbsInfoViewComponent infoView) {
        if (mInfoView != null) {
            mInfoView.abandon();
        }
        mInfoView = infoView;
    }

    public void addOperateComponent(AbsOperateComponent operate) {
        mOperate = operate;
        if (mInfoView != null) {
            mOperate.setRtcView(mInfoView.getRtcView());
            mOperate.setProfile(mInfoView.getProfileType());
        }
    }

    public AbsInfoViewComponent getInfoView() {
        return mInfoView;
    }

    public AbsOperateComponent getOperate() {
        return mOperate;
    }

    public void setData(UserInfo userInfo) {
        mUserInfo = userInfo;
        if (mInfoView != null) {
            mInfoView.setData(mUserInfo);
        }
        if (mOperate != null) {
            mOperate.setData(mUserInfo);
        }
    }

    public void setParentView(ViewGroup parentView) {
        if (mInfoView != null) {
            mInfoView.setParentView(parentView);
        }
    }

    public void setScalingRatio(IVideoRender.ScalingRatio ratio) {
        if (mInfoView != null && mInfoView.getRtcView() != null) {
            mInfoView.getRtcView().setScalingRatio(ratio);
        }
    }

    public void switchRtcVisible(boolean show) {
        if (mInfoView != null) {
            mInfoView.setDefaultHeadVisible(!show);
            mInfoView.setRtcViewVisible(show);
            mInfoView.setParentViewVisible(true);
        }
    }

    public void setDefaultHeadVisible(boolean show){
        if (mInfoView != null) {
            mInfoView.setDefaultHeadVisible(!show);
        }
    }

    public void setInfoViewVisible(boolean visible) {
        if (mInfoView != null) {
            mInfoView.setParentViewVisible(visible);
            mInfoView.setRtcViewVisible(visible);
        }
    }

    public void setActiveViewVisible(boolean visible) {
        if (mInfoView != null) {
            mInfoView.setActiveViewVisible(visible);
        }
    }

    public void updateAudioImg(int audioResourceId) {
        if (mInfoView != null && audioResourceId != -1) {
            mInfoView.setAudioResource(audioResourceId);
        }
    }

    public void updateSignalImg(int signalRes) {
        if (mInfoView != null && signalRes != -1) {
            mInfoView.setSignalRes(signalRes);
        }
    }

    public boolean isEmptyUserInfo() {
        return mUserInfo == null;
    }

    public boolean checkUserInfo(UserInfo userInfo) {
        if (userInfo == null) return false;
        if (mUserInfo == null) return false;
        return userInfo.userId == mUserInfo.userId;
    }

    public boolean checkUserInfo(long userId) {
        if (userId == 0L) return false;
        if (mUserInfo == null) return false;
        return mUserInfo.userId == userId;
    }

    public long getUserId() {
        if (mUserInfo == null) return 0L;
        return mUserInfo.userId;
    }

    public boolean isScreenUser() {
        if (mUserInfo == null) return false;
        return mUserInfo.isScreenStarted();
    }

    public void addRtcWbView(RtcWbView rtcWbView) {
        if (mInfoView != null) {
            mInfoView.addRtcWbView(rtcWbView);
        }
    }

    public void abandon() {
        if (mInfoView != null) {
            mInfoView.abandon();
        }
        if (mOperate != null) {
            mOperate.abandon();
        }
    }

    public void initUserStatistics() {
        if (mInfoView != null) {
            mInfoView.initUserStatistics();
        }
    }

    public void setUserVideoStatisticsVisible(boolean visible) {
        if (mInfoView != null) {
            mInfoView.setUserVideoStatisticsVisible(visible);
        }
    }

    public void setUserStatistics(String decodeType, String resolutionType, String bitRate) {
        if (mInfoView != null) {
            mInfoView.setUserVideoStatistics(decodeType, resolutionType, bitRate);
        }
    }


    public void refreshMirror(boolean mirror) {
        if (mOperate != null) {
            mOperate.refreshMirror(mirror);
        }
    }

    public void release() {
        mUserInfo = null;
        if (mInfoView != null) {
            mInfoView.releaseData();
        }
        if (mOperate != null) {
            mOperate.releaseData();
            mOperate = null;
        }
    }

    public boolean subscribeVideo() {
        if (mOperate != null) {
            return mOperate.subscribeVideo();
        }
        return false;
    }

    public boolean unSubscribeVideo() {
        if (mOperate != null) {
            return mOperate.unSubscribeVideo();
        }
        return false;
    }

    public void setOnTouchListener(OnPanoTouchListener touchListener) {
        if(touchListener == null || mInfoView == null) return ;

        if(mInfoView.getRtcView() != null){
            mInfoView.getRtcView().setOnTouchListener(touchListener);
        }

        if(mInfoView.getParentView() != null){
            mInfoView.getParentView().setOnTouchListener(touchListener);
        }

        mInfoView.setOnTouchListener(touchListener);
    }

}
