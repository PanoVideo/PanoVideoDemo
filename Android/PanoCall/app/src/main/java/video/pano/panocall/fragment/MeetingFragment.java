package video.pano.panocall.fragment;

import static video.pano.panocall.rtc.MeetingViewFactory.MODE_LOCAL_VIDEO;
import static video.pano.panocall.rtc.MeetingViewFactory.MODE_REMOTE_SCREEN;
import static video.pano.panocall.rtc.MeetingViewFactory.MODE_REMOTE_VIDEO;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.pano.rtc.api.Constants;

import java.util.List;

import video.pano.panocall.R;
import video.pano.panocall.info.Config;
import video.pano.panocall.info.UserManager;
import video.pano.panocall.listener.OnFrgEventListener;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.rtc.AbsOperateComponent;
import video.pano.panocall.rtc.MeetingViewFactory;
import video.pano.panocall.rtc.MeetingViewInfo;
import video.pano.panocall.viewmodel.MeetingViewModel;

public class MeetingFragment extends Fragment implements OnFrgEventListener {

    protected MeetingViewModel mViewModel;
    protected final Handler mHandler = new Handler(Looper.getMainLooper());
    protected MeetingViewInfo mLocalMeetingView;
    protected NavController mNavController ;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavController = NavHostFragment.findNavController(MeetingFragment.this);
        initViewModel();
    }

    protected void initViewModel() {
        mViewModel = new ViewModelProvider(getActivity()).get(MeetingViewModel.class);
        mViewModel.setOnFragmentEventListener(this);
    }

    @Override
    public void onUserAudioMute(UserInfo user) {
        updateUserAudioState(user);
    }

    @Override
    public void onUserAudioUnmute(UserInfo user) {
        updateUserAudioState(user);
    }

    @Override
    public void onUserAudioStart(UserInfo user) {
        updateUserAudioState(user);
    }

    @Override
    public void onUserAudioStop(UserInfo user) {
        updateUserAudioState(user);
    }

    @Override
    public void onUserScreenStart(long userId) {
        List<UserInfo> screenUserList = UserManager.getIns().getScreenUsers();
        int screenSize = screenUserList.size();
        if (screenSize > 0) {
            mViewModel.mCurrentScreenUserId = screenUserList.get(screenSize - 1).userId;
        }
    }

    @Override
    public void onUserScreenStop(UserInfo user) {
        if (mViewModel.mCurrentScreenUserId == user.userId) {
            List<UserInfo> screenUserList = UserManager.getIns().getScreenUsers();
            int screenSize = screenUserList.size();
            if (screenSize > 0) {
                mViewModel.mCurrentScreenUserId = screenUserList.get(screenSize - 1).userId;
            }
        }
    }

    @Override
    public void onDestinationChangedListener() {
        unSubscribeAllMeetingView();
    }

    protected int getAudioMutedResourceId(boolean isLarge) {
        return R.drawable.svg_icon_small_audio_mute;
    }

    protected int getAudioNormalResourceId(boolean isLarge) {
        return R.drawable.svg_icon_small_audio_normal;
    }

    protected int getAudioMutePSTNResourceId(boolean isLarge) {
        return R.drawable.svg_icon_small_audio_pstn_mute;
    }

    protected int getAudioNormalPSTNResourceId(boolean isLarge) {
        return R.drawable.svg_icon_small_audio_pstn_normal;
    }

    protected int getSignalLowResourceId(boolean isLarge) {
        return R.drawable.svg_icon_small_signal_low;
    }

    protected int getSignalPoorResourceId(boolean isLarge) {
        return R.drawable.svg_icon_small_signal_poor;
    }

    protected int getSignalGoodResourceId(boolean isLarge) {
        return R.drawable.svg_icon_small_signal_good;
    }

    protected void startPreviewLocalUser() {
        if (UserManager.getIns().isRemoteEmpty()) {
            UserInfo localUser = UserManager.getIns().getLocalUser();
            if (mViewModel.mAutoStartCamera) {
                localUser.setVideoStarted(true);
                if (!Config.sIsLocalVideoStarted) {
                    mViewModel.startPreview();
                    Config.sIsLocalVideoStarted = true;
                }
                subscribeLocalVideo(mLocalMeetingView, localUser);
            } else {
                mLocalMeetingView.switchRtcVisible(false);
            }
            updateUserAudioState(localUser);
        }
    }

    protected MeetingViewInfo refreshViewInfo(MeetingViewInfo viewInfo, UserInfo userInfo) {
        if (userInfo == null) {
            return null;
        }
        if (userInfo.isScreenStarted()) {
            return buildViewInfo(viewInfo, false, userInfo, MODE_REMOTE_SCREEN);
        } else if (userInfo.userId != UserManager.getIns().getLocalUser().userId) {
            return buildViewInfo(viewInfo, false, userInfo, MODE_REMOTE_VIDEO);
        } else {
            return buildViewInfo(viewInfo, Config.sIsFrontCamera, userInfo, MODE_LOCAL_VIDEO);
        }
    }

    protected void unSubscribeVideo(MeetingViewInfo viewInfo) {
        if (viewInfo != null) {
            viewInfo.switchRtcVisible(false);
            viewInfo.unSubscribeVideo();
        }
    }

    protected void subscribeVideo(MeetingViewInfo viewInfo) {
        if (viewInfo != null) {
            viewInfo.subscribeVideo();
        }
    }

    protected void subscribeLocalVideo(MeetingViewInfo viewInfo, UserInfo userInfo) {
        buildViewInfo(viewInfo, Config.sIsFrontCamera, userInfo, MODE_LOCAL_VIDEO)
                .subscribeVideo();
    }

    protected MeetingViewInfo buildViewInfo(MeetingViewInfo viewInfo, boolean isMirror, UserInfo userInfo, int type) {
        if (userInfo == null || viewInfo == null) return null;
        userInfo.setMirror(isMirror);
        viewInfo.unSubscribeVideo();
        viewInfo.release();
        AbsOperateComponent operateComponent = MeetingViewFactory.createOperate(type);
        viewInfo.addOperateComponent(operateComponent);
        viewInfo.setData(userInfo);
        viewInfo.unSubscribeVideo();
        viewInfo.switchRtcVisible(userInfo.isVideoStarted() || userInfo.isScreenStarted());
        return viewInfo;
    }

    protected void switchViewInfoData(MeetingViewInfo viewInfo, UserInfo userInfo) {
        viewInfo.release();
        viewInfo.setData(userInfo);
        viewInfo.switchRtcVisible(false);
    }

    @Override
    public void onSwitchCamera() {
        if (mLocalMeetingView != null && mLocalMeetingView.getUserId() == UserManager.getIns().getLocalUser().userId) {
            mLocalMeetingView.refreshMirror(Config.sIsFrontCamera);
        }
    }

    @Override
    public void subscribeLocalVideo() {
        subscribeLocalVideo(mLocalMeetingView, UserManager.getIns().getLocalUser());
    }

    @Override
    public void onUserAudioCallTypeChanged(UserInfo user) {
        updateUserAudioState(user);
    }

    @Override
    public void onUserVideoStart(UserInfo user) {
    }

    @Override
    public void onUserVideoStop(UserInfo user) {
    }

    @Override
    public void onNetworkQuality(long userId, Constants.QualityRating quality) {
    }

    @Override
    public void onVideoAnnotationStart(long userId,int streamIds) {
    }

    @Override
    public void onVideoAnnotationStop(long userId) {
    }

    @Override
    public void onShareAnnotationStart(long userId) {
    }

    @Override
    public void onShareAnnotationStop(long userId) {
    }

    @Override
    public void onUserLeaveIndication(UserInfo user) {
    }

    @Override
    public void onUserJoinIndication(UserInfo user) {
    }

    @Override
    public void updateUserAudioState(UserInfo userInfo) {
    }

    @Override
    public void stopLocalVideo() {
    }

    @Override
    public void startLocalVideo() {
    }

    @Override
    public void onCheckState() {
    }

    protected void unSubscribeAllMeetingView() {
    }
}
