package video.pano.panocall.rtc;

import android.util.Log;
import android.widget.Toast;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.IVideoRender;

import video.pano.panocall.info.Constant;
import video.pano.panocall.utils.Utils;

public class RemoteScreenOperate extends AbsOperateComponent {

    @Override
    public boolean subscribeVideo() {
        if (mRtcView == null || mUserInfo == null) return false;
        mRtcView.setMirror(mUserInfo.isMirror());
        mRtcView.setScalingType(IVideoRender.ScalingType.SCALE_ASPECT_ADJUST);
        PanoRtcEngine.getIns().getPanoEngine().setRemoteScreenRender(mUserInfo.userId, mRtcView);

        Constants.QResult ret = PanoRtcEngine.getIns().getPanoEngine().subscribeScreen(mUserInfo.userId);
        if (ret == Constants.QResult.OK) {
            return true;
        } else {
            String msg = "subscribeUserScreen failed, userId=" + mUserInfo.userId + ", result=" + ret;
            Log.w(Constant.TAG, msg);
            Toast.makeText(Utils.getApp(), msg, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public boolean unSubscribeVideo() {
        if (mUserInfo == null) return false;
        PanoRtcEngine.getIns().getPanoEngine().unsubscribeScreen(mUserInfo.userId);
        PanoRtcEngine.getIns().getPanoEngine().setRemoteScreenRender(mUserInfo.userId, null);
        return true;
    }

}
