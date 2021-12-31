package video.pano.panocall.rtc;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcView;

import video.pano.panocall.model.UserInfo;

public abstract class AbsOperateComponent {

    protected UserInfo mUserInfo;
    protected long mUserId = 0L ;
    protected RtcView mRtcView;


    // 此用户的当前订阅能力
    protected Constants.VideoProfileType mSubProfile;

    public void setRtcView(RtcView rtcView) {
        mRtcView = rtcView;
    }

    public void setProfile(Constants.VideoProfileType profile) {
        mSubProfile = profile;
    }

    public void setData(UserInfo userInfo){
        mUserInfo = userInfo ;
        if(mUserInfo != null) mUserId = mUserInfo.userId;
    }

    public void abandon() {
        unSubscribeVideo();
        if (mRtcView != null) {
            mRtcView.release();
            mRtcView = null;
        }
        mUserInfo = null ;
    }

    public void releaseData(){
        mUserInfo = null ;
        mUserId = 0L ;
    }

    public void refreshMirror(boolean mirror){
        if(mRtcView != null){
            mRtcView.setMirror(mirror);
        }
    }

    public abstract boolean subscribeVideo();

    public abstract boolean unSubscribeVideo();


}
