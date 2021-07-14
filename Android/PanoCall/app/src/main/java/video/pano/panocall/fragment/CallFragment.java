package video.pano.panocall.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.IVideoRender;
import com.pano.rtc.api.RtcView;
import com.pano.rtc.api.model.RtcAudioLevel;

import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.activity.CallActivity;
import video.pano.panocall.listener.PanoCallEventHandler;
import video.pano.panocall.listener.OnControlEventListener;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.model.UserViewInfo;
import video.pano.panocall.utils.Utils;
import video.pano.panocall.viewmodel.CallViewModel;

import static video.pano.panocall.info.Config.MAX_AUDIO_DUMP_SIZE;


public class CallFragment extends Fragment implements PanoCallEventHandler {

    CallViewModel mViewModel;
    OnControlEventListener mListener;
    RtcView mLocalView;

    // 视图数组，浮动视同和宫格视图
    int mUserViewCount = 0;
    int mLocalViewIndex = 0;
    boolean mLocalViewMirror = true ;
    protected UserViewInfo[] mUserViewArray;
    protected final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(getActivity()).get(CallViewModel.class);
        mViewModel.setCallEventHandler(this);
        setupUserViewArray();
        PanoApplication app = (PanoApplication)Utils.getApp();
        app.getLocalVideoProfile().observe(getViewLifecycleOwner(), profile -> {
            if (mViewModel.mLocalProfile != profile) {
                mViewModel.mLocalProfile = profile;
                if (app.mIsLocalVideoStarted) {
                    mViewModel.rtcEngine().startVideo(mViewModel.mLocalProfile,mViewModel.mIsFrontCamera);
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnControlEventListener) {
            mListener = (OnControlEventListener) context;
        }
    }

    @Override
    public void onDestroyView() {
        resetUserViewArray();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onSwitchCamera() {
        if (mLocalView != null) {
            mLocalView.setMirror(mViewModel.mIsFrontCamera);
        }
    }

    public void onLocalVideo(boolean closed) {
        if (closed) {
            stopLocalVideo();
        } else {
            startLocalVideo();
        }
    }

    public void onLocalAudio(boolean muted) {
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        localUser.setAudioMuted(muted);
        updateUserAudioState(mLocalViewIndex);
        if (muted) {
            mViewModel.rtcEngine().muteAudio();
        } else {
            mViewModel.rtcEngine().unmuteAudio();
        }
    }


    void initUserViewArray(int count) {
        // reset RTC Engine render
        for (int i=0; i < mUserViewCount; i++) {
            if (mUserViewArray[i].rtcView == null || mUserViewArray[i].isFree) {
                continue;
            }
            if (mUserViewArray[i].userId == mViewModel.getLocalUserId()) {
                clearLocalVideoRender();
            } else if (mUserViewArray[i].isScreen) {
                clearRemoteScreenRender(mUserViewArray[i].userId);
            } else {
                clearRemoteVideoRender(mUserViewArray[i].userId);
            }
        }

        resetUserViewArray();

        mUserViewCount = count;
        mUserViewArray = new UserViewInfo[mUserViewCount];
        for (int i=0; i < mUserViewCount; i++) {
            mUserViewArray[i] = new UserViewInfo();
        }
    }
    void initUserVideoView(int localIndex, boolean showScreen) {
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();

        if (mViewModel.mIsRoomJoined || localUser.isVideoStarted()) {
            //if (localUser.isVideoStarted())
            { // always show avatar even if video is not started
                mLocalViewIndex = localIndex;
                mUserViewArray[mLocalViewIndex].isFree = false;
                mUserViewArray[mLocalViewIndex].setUser(localUser.userId, localUser.userName,mViewModel.getUserManager().isMySelf(localUser.userId));
                if (localUser.isVideoStarted()) {
                    mUserViewArray[mLocalViewIndex].setVisible(true);
                } else {
                    mUserViewArray[mLocalViewIndex].setUserVisible(true);
                }
                updateLocalVideoRender(mUserViewArray[mLocalViewIndex].rtcView, mLocalViewIndex);
                updateUserAudioState(mLocalViewIndex);
            }
            int count = mUserViewCount - 1;
            // 1. add screen user
            if (showScreen) {
                int screenCount = mViewModel.getUserManager().getScreenSize();
                for(int i=0; i<screenCount; i++){
                    UserInfo user = mViewModel.getUserManager().getScreenUsers().valueAt(i);
//                    UserInfo user = mViewModel.getUserManager().getRemoteUser(vui.userId);
                    if (user.isScreenStarted()) {
                        setupUserScreenView(user);
                        if (count > 0) {
                            --count;
                        }
                        break;
                    }
                }
            }
            // 2. add video user
            int videoCount = mViewModel.getUserManager().getVideoSize();
            for(int i=0; i<videoCount && count>0; i++){
                UserInfo user = mViewModel.getUserManager().getVideoUsers().valueAt(i);
//                UserInfo user = mViewModel.getUserManager().getRemoteUser(vui.userId);
                if (user.isVideoStarted()) {
                    setupUserVideoView(user);
                    --count;
                }
            }
            // 3. add non-video user
            if (count > 0) {
                LongSparseArray<UserInfo> remoteUsers = mViewModel.getUserManager().getRemoteUsers();
                int userCount = remoteUsers.size();
                for(int i=0; i<userCount && count>0; i++){
                    UserInfo user = remoteUsers.valueAt(i);
                    if (getIndexForNotFreeUser(user.userId) == -1) {
                        setupUserView(user);
                        --count;
                    }
                }
            }
            // 4. remove other non-video user
            if(count > 0){
                for(int k = 0 ; k < mUserViewCount ; k ++){
                    if (mUserViewArray[k].userId == 0L || mUserViewArray[k].isFree) {
                        mUserViewArray[k].setVisible(false);
                    }
                }
            }
        } else {
            // 启动视频预览，并且显示到大图
            if (mViewModel.mAutoStartCamera && mViewModel.getUserManager().isRemoteEmpty()) {
                mUserViewArray[0].isFree = false;
                mUserViewArray[0].setUser(localUser.userId, localUser.userName,mViewModel.getUserManager().isMySelf(localUser.userId));
                mUserViewArray[0].setVisible(true);

                updateLocalVideoRender(mUserViewArray[0].rtcView, 0);
                updateUserAudioState(mLocalViewIndex);
                PanoApplication app = (PanoApplication)Utils.getApp();
                if (!app.mIsLocalVideoStarted) {
                    mViewModel.rtcEngine().startPreview(mViewModel.mLocalProfile, mViewModel.mIsFrontCamera);
                    app.mIsLocalVideoStarted = true;
                }
            }
        }
    }


    private void resetUserViewArray(){
        for (int i=0; i < mUserViewCount; i++) {
            if (mUserViewArray[i] != null) {
                mUserViewArray[i].reset();
                mUserViewArray[i] = null;
            }
        }
        mUserViewCount = 0;
        mUserViewArray = null;
    }
    int getIndexForUser(long userId) {
        for (int i=0; i < mUserViewCount; i++) {
            if (userId == mUserViewArray[i].userId) {
                return i;
            }
        }

        return -1;
    }
    int getIndexForNotFreeUser(long userId) {
        for (int j=0; j < mUserViewCount; j++) {
            if (userId == mUserViewArray[j].userId && !mUserViewArray[j].isFree) {
                return j;
            }
        }
        return -1;
    }
    int getIndexForFreeRemoteUser(UserInfo user) {
        int viewIndex = getIndexForUser(user.userId);
        if (viewIndex != -1) {
            return viewIndex;
        }
        // 尝试找到一个空闲的视图
        // try to find a free view
        for (int i=1; i < mUserViewCount; i++) {
            if (mUserViewArray[i].isFree) {
                viewIndex = i;
                break;
            }
        }
        if (viewIndex != -1) {
            mUserViewArray[viewIndex].isFree = true;
            mUserViewArray[viewIndex].isScreen = false;
            mUserViewArray[viewIndex].isSubscribed = false;
        }
        return viewIndex;
    }

    // 订阅用户视频
    boolean subscribeUserVideo(long userId, String userName, UserViewInfo viewInfo, Constants.VideoProfileType profile) {
        viewInfo.rtcView.setMirror(false);
        viewInfo.setUser(userId, userName,mViewModel.getUserManager().isMySelf(userId));
        viewInfo.setVisible(true);
        viewInfo.isFree = false;
        viewInfo.isScreen = false;
        updateRemoteVideoRender(userId,viewInfo.rtcView);

        Constants.QResult ret = mViewModel.rtcEngine().subscribeVideo(userId, profile);
        if(ret == Constants.QResult.OK){
            viewInfo.subProfile = profile;
            viewInfo.isSubscribed = true;
            return true;
        } else {
            String msg = "subscribeUserVideo failed, userId=" + userId + ", result=" + ret;
            Log.w(CallActivity.TAG, msg);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    // 订阅用户桌面共享
    boolean subscribeUserScreen(long userId, String userName, UserViewInfo viewInfo) {
        viewInfo.rtcView.setMirror(false);
        viewInfo.setUser(userId, userName,mViewModel.getUserManager().isMySelf(userId));
        viewInfo.setVisible(true);
        viewInfo.isFree = false;
        viewInfo.isScreen = true;
        updateRemoteScreenRender(userId,viewInfo.rtcView);

        Constants.QResult ret = mViewModel.rtcEngine().subscribeScreen(userId);
        if(ret == Constants.QResult.OK){
            viewInfo.isSubscribed = true;
            return true;
        } else {
            String msg = "subscribeUserScreen failed, userId=" + userId + ", result=" + ret;
            Log.w(CallActivity.TAG, msg);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    void clearRemoteVideoRender(long userId){
        mViewModel.rtcEngine().unsubscribeVideo(userId);
        mViewModel.rtcEngine().setRemoteVideoRender(userId, null);
    }

    void clearRemoteScreenRender(long userId){
        mViewModel.rtcEngine().unsubscribeScreen(userId);
        mViewModel.rtcEngine().setRemoteScreenRender(userId, null);
    }

    void clearLocalVideoRender(){
        mViewModel.rtcEngine().setLocalVideoRender(null);
    }

    void updateRemoteVideoRender(long userId , RtcView view){
        if(view != null){
            view.setScalingType(IVideoRender.ScalingType.SCALE_ASPECT_ADJUST);
        }
        mViewModel.rtcEngine().setRemoteVideoRender(userId, view);
    }

    void updateRemoteScreenRender(long userId , RtcView view){
        if(view != null){
            view.setScalingType(IVideoRender.ScalingType.SCALE_ASPECT_ADJUST);
        }
        mViewModel.rtcEngine().setRemoteScreenRender(userId, view);
    }

    // 更新本地用户视频的视图
    void updateLocalVideoRender(RtcView view, int index) {
        mLocalView = view;
        mLocalViewIndex = index;
        updateLocalVideoRender(view,mViewModel.mIsFrontCamera);
    }

    // 更新本地用户视频的视图
    void updateLocalVideoRender(RtcView view , boolean isMirror){
        mLocalViewMirror = isMirror ;
        if (view != null) {
            view.setMirror(isMirror);
            view.setScalingType(IVideoRender.ScalingType.SCALE_ASPECT_FIT);
        }
        mViewModel.rtcEngine().setLocalVideoRender(view);
    }


    private void stopLocalVideo() {
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (mViewModel.mIsRoomJoined) {
            mViewModel.rtcEngine().stopVideo();
            localUser.setVideoStarted(false);
        } else {
            mViewModel.rtcEngine().stopPreview();
        }
        clearLocalVideoRender();
        if (mLocalView == mUserViewArray[mLocalViewIndex].rtcView) {
            mUserViewArray[mLocalViewIndex].setVideoVisible(false);
            mUserViewArray[mLocalViewIndex].isFree = true;
        }
        PanoApplication app = (PanoApplication)Utils.getApp();
        app.mIsLocalVideoStarted = false;
    }

    private void startLocalVideo() {
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        for (int i = 0; i < mUserViewCount; i++) {
            if (mUserViewArray[i].isFree) {
                mUserViewArray[i].isFree = false;
                mUserViewArray[i].setUser(localUser.userId, localUser.userName,mViewModel.getUserManager().isMySelf(localUser.userId));
                mUserViewArray[i].setVisible(true);
                updateLocalVideoRender(mUserViewArray[i].rtcView, i);
                break;
            }
        }
        if (mViewModel.mIsRoomJoined) {
            mViewModel.rtcEngine().startVideo(mViewModel.mLocalProfile,mViewModel.mIsFrontCamera);
            localUser.setVideoStarted(true);
        } else {
            mViewModel.rtcEngine().startPreview(mViewModel.mLocalProfile, mViewModel.mIsFrontCamera);
        }
        PanoApplication app = (PanoApplication)Utils.getApp();
        app.mIsLocalVideoStarted = true;
    }

    void updateUserAudioState(int index) {
        if (-1 == index) {
            return;
        }
        boolean muted = false;
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (mUserViewArray[index].userId == localUser.userId) {
            muted = localUser.isAudioMuted();
        } else {
            UserInfo user = mViewModel.getUserManager().getRemoteUser(mUserViewArray[index].userId);
            if (user == null) {
                return;
            }
            muted = user.isAudioMuted();
        }
        int audioResourceId = muted ? getAudioMutedResourceId(index) : getAudioNormalResourceId(index);
        mUserViewArray[index].imgView.setImageResource(audioResourceId);
    }

    UserInfo getUnsubscribedVideoUser() {
        int count = mViewModel.getUserManager().getVideoSize();
        for(int i=0; i<count; i++){
            UserInfo vui = mViewModel.getUserManager().getVideoUsers().valueAt(i);
            if (getIndexForNotFreeUser(vui.userId) == -1) {
                return vui;
            }
        }
        return null;
    }

    // 取消订阅用户视频
    private void stopUserVideo(UserInfo user) {
        for (int i=0; i < mUserViewCount; i++) {
            if (user.userId == mUserViewArray[i].userId && !mUserViewArray[i].isScreen) {
                mUserViewArray[i].isSubscribed = false;
                stopUserVideoView(user.userId, i);
                return;
            }
        }
    }

    // 取消订阅用户桌面共享
    private void stopUserScreen(UserInfo user) {
        if (user.userId == mUserViewArray[0].userId && mUserViewArray[0].isScreen) {
            mUserViewArray[0].isScreen = false;
            mUserViewArray[0].isSubscribed = false;
            if (user.isVideoStarted()) {
                Constants.VideoProfileType profile = getProfileForVideoView(0);
                subscribeUserVideo(mUserViewArray[0].userId, mUserViewArray[0].userName, mUserViewArray[0], profile);
                return;
            }
            stopUserVideoView(user.userId, 0);
        }
    }

    void stopUserView(long userId) {
        // check if there is unsubscribed video user
        int viewIndex = getIndexForUser(userId);
        if (viewIndex == -1) {
            return ;
        }
        UserInfo vui = getUnsubscribedVideoUser();
        if (vui != null) {
            Constants.VideoProfileType profile = getProfileForVideoView(viewIndex);
            if (subscribeUserVideo(vui.userId, vui.userName, mUserViewArray[viewIndex], profile)) {
                updateUserAudioState(viewIndex);
                return;
            }
        }

        mUserViewArray[viewIndex].isFree = true;
        mUserViewArray[viewIndex].setVisible(false);
    }

    private void stopUserVideoView(long userId, int index) {
        // check if there is unsubscribed video user
        UserInfo vui = getUnsubscribedVideoUser();
        if (vui != null) {
            Constants.VideoProfileType profile = getProfileForVideoView(index);
            if (subscribeUserVideo(vui.userId, vui.userName, mUserViewArray[index], profile)) {
                updateUserAudioState(index);
                return;
            }
        }

        mUserViewArray[index].setVideoVisible(false);
        clearRemoteVideoRender(userId);
    }

    void setupUserView(UserInfo user) {
        int index = getIndexForFreeRemoteUser(user);
        if (index != -1) {
            mUserViewArray[index].setUser(user.userId, user.userName,mViewModel.getUserManager().isMySelf(user.userId));
            mUserViewArray[index].isFree = false;
            mUserViewArray[index].isSubscribed = false;
            mUserViewArray[index].setUserVisible(true);
            mUserViewArray[index].setVideoVisible(false);
            updateUserAudioState(index);
        }
    }

    void setupUserVideoView(UserInfo user) {
        int index = getIndexForFreeRemoteUser(user);
        if (index != -1) {
            if (!mUserViewArray[index].isFree && mUserViewArray[index].isScreen) {
                // the user screen is already subscribed
                return;
            }
            Constants.VideoProfileType profile = getProfileForVideoView(index);
            // 订阅此用户视频到指定视图
            if (subscribeUserVideo(user.userId, user.userName, mUserViewArray[index], profile)) {
                updateUserAudioState(index);
            }
        }
    }

    Constants.VideoProfileType getProfileForVideoView(int index) {
        return Constants.VideoProfileType.Low;
    }
    void setupUserViewArray() {}
    void setupUserScreenView(UserInfo user) {}
    int getAudioMutedResourceId(int index) { return R.drawable.svg_icon_small_audio_mute; }
    int getAudioNormalResourceId(int index) { return R.drawable.svg_icon_small_audio_normal; }
    int getSignalLowResourceId(int index) { return R.drawable.svg_icon_small_signal_low; }
    int getSignalPoorResourceId(int index) { return R.drawable.svg_icon_small_signal_poor; }
    int getSignalGoodResourceId(int index) { return R.drawable.svg_icon_small_signal_good; }
    void onUserLeave(long userId) {}

    @Override
    public void onPageNumberChanged(int curPage, int totalPages) {
    }

    @Override
    public void onViewScaleChanged(float scale) {
    }

    @Override
    public void onVideoAnnotationStart(long userId, int streamId) {
    }

    @Override
    public void onVideoAnnotationStop(long userId, int streamId) {
    }

    @Override
    public void onShareAnnotationStart(long userId) {
    }

    @Override
    public void onShareAnnotationStop(long userId) {
    }

    @Override
    public void onRoleTypeChanged(Constants.WBRoleType newRole) {

    }

    @Override
    public void onUserJoined(long userId, String userName) {

    }

    @Override
    public void onUserLeft(long userId) {

    }

    @Override
    public void onMessage(long userId, byte[] msg) {

    }

    @Override
    public void onWhiteboardStop() {
    }

    @Override
    public void onCreateDoc(Constants.QResult result, String fileId) {

    }

    @Override
    public void onSwitchDoc(Constants.QResult result, String fileId) {

    }

    @Override
    public void onDeleteDoc(Constants.QResult result, String fileId) {

    }

    @Override
    public void onChannelJoinConfirm(Constants.QResult result) {
        if (result == Constants.QResult.OK) {
            mViewModel.mIsRoomJoined = true;
            if (mViewModel.mEnableDebugMode) {
                mViewModel.rtcEngine().startAudioDump(MAX_AUDIO_DUMP_SIZE);
            }
            mViewModel.rtcEngine().stopPreview();
            PanoApplication app = (PanoApplication)Utils.getApp();
            app.mIsLocalVideoStarted = false;
            UserInfo localUser = mViewModel.getUserManager().getLocalUser();
            // 启动本地视频
            mViewModel.mIsVideoClosed = !mViewModel.mAutoStartCamera;
            if(mViewModel.mAutoStartCamera){
                updateLocalVideoRender(mUserViewArray[0].rtcView, 0);
                mViewModel.rtcEngine().startVideo(mViewModel.mLocalProfile,mViewModel.mIsFrontCamera);
                localUser.setVideoStarted(true);
                app.mIsLocalVideoStarted = true;
            }
            // 启动本地音频
            mViewModel.rtcEngine().startAudio();
            localUser.setAudioStared(true);
            if (mViewModel.mAutoMuteAudio) {
                onLocalAudio(true);
            }
        } else {
            mViewModel.mIsRoomJoined = false;
            Toast.makeText(getContext(), "joinChannel failed, result=" + result, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onChannelLeaveIndication(Constants.QResult result) {
        mViewModel.mIsRoomJoined = false;
    }
    @Override
    public void onUserJoinIndication(UserInfo user) {
        setupUserView(user);
    }
    @Override
    public void onUserLeaveIndication(UserInfo user, Constants.UserLeaveReason reason) {
        stopUserVideo(user);
        stopUserScreen(user);
        stopUserView(user.userId);
        onUserLeave(user.userId);
    }
    @Override
    public void onUserAudioMute(UserInfo user) {
        updateUserAudioState(getIndexForUser(user.userId));
    }
    @Override
    public void onUserAudioUnmute(UserInfo user) {
        updateUserAudioState(getIndexForUser(user.userId));
    }
    @Override
    public void onUserVideoStart(UserInfo user) {
        setupUserVideoView(user);
    }
    @Override
    public void onUserVideoStop(UserInfo user) {
        // 取消订阅此用户视频
        stopUserVideo(user);
    }
    @Override
    public void onUserScreenStart(UserInfo user) {
        setupUserScreenView(user);
    }
    @Override
    public void onUserScreenStop(UserInfo user) {
        // 取消订阅此用户桌面共享
        stopUserScreen(user);
    }
    @Override
    public void onNetworkQuality(long userId, Constants.QualityRating quality) {
        int index = getIndexForUser(userId);
        if(index == -1 ) return ;
        if(quality == Constants.QualityRating.Excellent || quality == Constants.QualityRating.Good ){
            mUserViewArray[index].setSignalRes(getSignalGoodResourceId(index));
        }else if (quality == Constants.QualityRating.Poor){
            mUserViewArray[index].setSignalRes(getSignalPoorResourceId(index));
        }else{
            mUserViewArray[index].setSignalRes(getSignalLowResourceId(index));
        }
    }

}
