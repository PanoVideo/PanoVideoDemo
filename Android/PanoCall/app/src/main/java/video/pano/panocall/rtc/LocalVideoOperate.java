package video.pano.panocall.rtc;

import com.pano.rtc.api.IVideoRender;

public class LocalVideoOperate extends AbsOperateComponent {

    @Override
    public boolean subscribeVideo() {
        if (mRtcView == null || mUserInfo == null) return false;
        mRtcView.setMirror(mUserInfo.isMirror());
        mRtcView.setScalingType(IVideoRender.ScalingType.SCALE_ASPECT_ADJUST);
        PanoRtcEngine.getIns().getPanoEngine().setLocalVideoRender(mRtcView);
        return true ;
    }

    @Override
    public boolean unSubscribeVideo() {
        PanoRtcEngine.getIns().getPanoEngine().setLocalVideoRender(null);
        return true;
    }
}
