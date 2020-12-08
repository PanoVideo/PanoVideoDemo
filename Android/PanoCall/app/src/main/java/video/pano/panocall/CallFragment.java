package video.pano.panocall;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcView;


public class CallFragment extends Fragment implements CallViewModel.CallEventHandler {

    CallViewModel mViewModel;
    OnControlEventListener mListener;
    RtcView mLocalView;

    // 视图数组，浮动视同和宫格视图
    int mUserViewCount = 0;
    UserViewInfo[] mUserViewArray;
    int mLocalViewIndex = 0;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(getActivity()).get(CallViewModel.class);
        mViewModel.setCallEventHandler(this);
        setupUserViewArray();
        PanoApplication app = (PanoApplication)getActivity().getApplication();
        app.getLocalVideoProfile().observe(getViewLifecycleOwner(), profile -> {
            if (mViewModel.mLocalProfile != profile) {
                mViewModel.mLocalProfile = profile;
                if (app.mIsLocalVideoStarted) {
                    mViewModel.rtcEngine().startVideo(mViewModel.mLocalProfile, mViewModel.mIsFrontCamera);
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnControlEventListener) {
            mListener = (OnControlEventListener) context;
        } else {
            //throw new RuntimeException(context.toString()
            //        + " must implement OnControlEventListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UserManager.UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (localUser.isVideoStarted()) {
            mViewModel.rtcEngine().setLocalVideoRender(mLocalView);
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

    void onSwitchCamera() {
        if (mLocalView != null) {
            mLocalView.setMirror(mViewModel.mIsFrontCamera);
        }
    }

    void onLocalVideo(boolean closed) {
        if (closed) {
            stopLocalVideo();
        } else {
            startLocalVideo();
        }
    }

    void onLocalAudio(boolean muted) {
        UserManager.UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        localUser.updateAudioMuteState(muted);
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
            if (mUserViewArray[i].view == null || mUserViewArray[i].isFree) {
                continue;
            }
            if (mUserViewArray[i].userId == mViewModel.getLocalUserId()) {
                mViewModel.rtcEngine().setLocalVideoRender(null);
            } else if (mUserViewArray[i].isScreen) {
                mViewModel.rtcEngine().setRemoteScreenRender(mUserViewArray[i].userId, null);
            } else {
                mViewModel.rtcEngine().setRemoteVideoRender(mUserViewArray[i].userId, null);
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
        UserManager.UserInfo localUser = mViewModel.getUserManager().getLocalUser();

        if (mViewModel.mIsRoomJoined || localUser.isVideoStarted()) {
            //if (localUser.isVideoStarted())
            { // always show avatar even if video is not started
                mLocalViewIndex = localIndex;
                mUserViewArray[mLocalViewIndex].isFree = false;
                mUserViewArray[mLocalViewIndex].setUser(localUser.userId, localUser.userName);
                if (localUser.isVideoStarted()) {
                    mUserViewArray[mLocalViewIndex].setVisible(true);
                } else {
                    mUserViewArray[mLocalViewIndex].setUserVisible(true);
                }
                updateLocalVideoRender(mUserViewArray[mLocalViewIndex].view, mLocalViewIndex);
                updateUserAudioState(mLocalViewIndex);
            }
            int count = mUserViewCount - 1;
            // 1. add screen user
            if (showScreen) {
                int screenCount = mViewModel.mScreenUsers.size();
                for(int i=0; i<screenCount; i++){
                    CallViewModel.VideoUserInfo vui = mViewModel.mScreenUsers.valueAt(i);
                    UserManager.UserInfo user = mViewModel.getUserManager().getRemoteUser(vui.userId);
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
            int videoCount = mViewModel.mVideoUsers.size();
            for(int i=0; i<videoCount && count>0; i++){
                CallViewModel.VideoUserInfo vui = mViewModel.mVideoUsers.valueAt(i);
                UserManager.UserInfo user = mViewModel.getUserManager().getRemoteUser(vui.userId);
                if (user.isVideoStarted()) {
                    setupUserVideoView(user);
                    --count;
                }
            }
            // 3. add non-video user
            if (count > 0) {
                LongSparseArray<UserManager.UserInfo> remoteUsers = mViewModel.getUserManager().getRemoteUsers();
                int userCount = remoteUsers.size();
                for(int i=0; i<userCount && count>0; i++){
                    UserManager.UserInfo user = remoteUsers.valueAt(i);
                    if (getIndexForUserView(user.userId) == -1) {
                        setupUserView(user);
                        --count;
                    }
                }
            }
        } else {
            // 启动视频预览，并且显示到大图
            if (mViewModel.mAutoStartCamera && mViewModel.getUserManager().isEmpty()) {
                mUserViewArray[0].isFree = false;
                mUserViewArray[0].setUser(localUser.userId, localUser.userName);
                mUserViewArray[0].setVisible(true);
                updateLocalVideoRender(mUserViewArray[0].view, 0);
                updateUserAudioState(mLocalViewIndex);
                PanoApplication app = (PanoApplication)getActivity().getApplication();
                if (!app.mIsLocalVideoStarted) {
                    mViewModel.rtcEngine().startPreview(mViewModel.mLocalProfile, mViewModel.mIsFrontCamera);
                    app.mIsLocalVideoStarted = true;
                }
            }
        }
    }
    private void resetUserViewArray(){
        for (int i=0; i < mUserViewCount; i++) {
            if (mUserViewArray[i].view != null) {
                mUserViewArray[i].view.release();
                mUserViewArray[i].view = null;
            }
            mUserViewArray[i].isFree = true;
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
    int getIndexForUserView(long userId) {
        for (int j=0; j < mUserViewCount; j++) {
            if (userId == mUserViewArray[j].userId && !mUserViewArray[j].isFree) {
                return j;
            }
        }

        return -1;
    }
    // 订阅用户视频
    boolean subscribeUserVideo(long userId, String userName, UserViewInfo viewInfo, Constants.VideoProfileType profile) {
        viewInfo.view.setMirror(false);
        viewInfo.view.setScalingType(mViewModel.mScalingType);
        viewInfo.setUser(userId, userName);
        viewInfo.setVisible(true);
        viewInfo.isFree = false;
        viewInfo.isScreen = false;
        mViewModel.rtcEngine().setRemoteVideoRender(userId, viewInfo.view);

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

    void unsubscribeUserVideo(long userId, UserViewInfo viewInfo) {
        mViewModel.rtcEngine().unsubscribeVideo(userId);
        mViewModel.rtcEngine().setRemoteVideoRender(userId, null);
        viewInfo.isSubscribed = false;
    }

    // 订阅用户桌面共享
    boolean subscribeUserScreen(long userId, String userName, UserViewInfo viewInfo) {
        viewInfo.view.setMirror(false);
        viewInfo.setUser(userId, userName);
        viewInfo.setVisible(true);
        viewInfo.isFree = false;
        viewInfo.isScreen = true;
        mViewModel.rtcEngine().setRemoteScreenRender(userId, viewInfo.view);

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

    void unsubscribeUserScreen(long userId, UserViewInfo viewInfo) {
        mViewModel.rtcEngine().unsubscribeScreen(userId);
        mViewModel.rtcEngine().setRemoteScreenRender(userId, null);
        viewInfo.isScreen = false;
        viewInfo.isSubscribed = false;
    }

    // 更新本地用户视频的视图
    void updateLocalVideoRender(RtcView view, int index) {
        mLocalView = view;
        mLocalViewIndex = index;
        if (mLocalView != null) {
            mLocalView.setMirror(mViewModel.mIsFrontCamera);
        }
        mViewModel.rtcEngine().setLocalVideoRender(mLocalView);
    }

    private void stopAllUsersVideoView() {
        for (int i=0; i < mUserViewCount; i++) {
            if (mUserViewArray[i].isFree) {
                continue;
            }
            if (mUserViewArray[i].userId == mViewModel.getLocalUserId()) {
                mViewModel.rtcEngine().setLocalVideoRender(null);
            } else if (!mUserViewArray[i].isScreen) {
                mViewModel.rtcEngine().setRemoteVideoRender(mUserViewArray[i].userId, null);
            } else {
                mViewModel.rtcEngine().setRemoteScreenRender(mUserViewArray[i].userId, null);
            }
            mUserViewArray[i].setVisible(false);
            mUserViewArray[i].reset();
        }
    }

    private void stopLocalVideo() {
        UserManager.UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (mViewModel.mIsRoomJoined) {
            mViewModel.rtcEngine().stopVideo();
            localUser.updateVideoState(false);
        } else {
            mViewModel.rtcEngine().stopPreview();
        }
        mViewModel.rtcEngine().setLocalVideoRender(null);
        if (mLocalView == mUserViewArray[mLocalViewIndex].view) {
            mUserViewArray[mLocalViewIndex].setVideoVisible(false);
            mUserViewArray[mLocalViewIndex].isFree = true;
        }
        PanoApplication app = (PanoApplication)getActivity().getApplication();
        app.mIsLocalVideoStarted = false;
    }

    private void startLocalVideo() {
        UserManager.UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (mLocalViewIndex < mUserViewCount) {
            if (mUserViewArray[mLocalViewIndex].isFree) {
                mUserViewArray[mLocalViewIndex].isFree = false;
                mUserViewArray[mLocalViewIndex].setUser(localUser.userId, localUser.userName);
                updateLocalVideoRender(mUserViewArray[mLocalViewIndex].view, mLocalViewIndex);
            }
            mUserViewArray[mLocalViewIndex].setVisible(true);
        }

        if (mViewModel.mIsRoomJoined) {
            mViewModel.rtcEngine().startVideo(mViewModel.mLocalProfile, mViewModel.mIsFrontCamera);
            localUser.updateVideoState(true);
        } else {
            mViewModel.rtcEngine().startPreview(mViewModel.mLocalProfile, mViewModel.mIsFrontCamera);
        }
        PanoApplication app = (PanoApplication)getActivity().getApplication();
        app.mIsLocalVideoStarted = true;
    }

    void updateUserAudioState(int index) {
        if (-1 == index) {
            return;
        }
        boolean muted = false;
        UserManager.UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (mUserViewArray[index].userId == localUser.userId) {
            muted = localUser.isAudioMuted();
        } else {
            UserManager.UserInfo user = mViewModel.getUserManager().getRemoteUser(mUserViewArray[index].userId);
            if (user == null) {
                return;
            }
            muted = user.isAudioMuted();
        }
        int audioResourceId = muted ? getAudioMutedResourceId(index) : getAudioNormalResourceId(index);
        mUserViewArray[index].imgView.setImageResource(audioResourceId);
    }

    CallViewModel.VideoUserInfo getUnsubscribedVideoUser() {
        int count = mViewModel.mVideoUsers.size();
        for(int i=0; i<count; i++){
            CallViewModel.VideoUserInfo vui = mViewModel.mVideoUsers.valueAt(i);
            if (getIndexForUserView(vui.userId) == -1) {
                return vui;
            }
        }
        return null;
    }

    CallViewModel.VideoUserInfo getUnsubscribedScreenUser() {
        int count = mViewModel.mScreenUsers.size();
        for(int i=0; i<count; i++){
            CallViewModel.VideoUserInfo vui = mViewModel.mScreenUsers.valueAt(i);
            if (getIndexForUserView(vui.userId) == -1) {
                return vui;
            }
        }
        return null;
    }



    private void stopUser(UserManager.UserInfo user) {
        int viewIndex = getIndexForUser(user.userId);
        if (viewIndex != -1) {
            stopUserView(user.userId, viewIndex);
        }
    }

    // 取消订阅用户视频
    private void stopUserVideo(UserManager.UserInfo user) {
        for (int i=0; i < mUserViewCount; i++) {
            if (user.userId == mUserViewArray[i].userId && !mUserViewArray[i].isScreen) {
                mUserViewArray[i].isSubscribed = false;
                stopUserVideoView(user.userId, i);
                return;
            }
        }
    }

    // 取消订阅用户桌面共享
    private void stopUserScreen(UserManager.UserInfo user) {
        if (user.userId == mUserViewArray[0].userId && mUserViewArray[0].isScreen) {
            mUserViewArray[0].isScreen = false;
            mUserViewArray[0].isSubscribed = false;
            if (user.isVideoStarted()) {
                Constants.VideoProfileType profile = getProfileForVideoView(0, user.maxProfile);
                subscribeUserVideo(mUserViewArray[0].userId, mUserViewArray[0].userName, mUserViewArray[0], profile);
                return;
            }
            stopUserVideoView(user.userId, 0);
        }
    }

    void stopUserView(long userId, int index) {
        // check if there is unsubscribed video user
        CallViewModel.VideoUserInfo vui = getUnsubscribedVideoUser();
        if (vui != null) {
            Constants.VideoProfileType profile = getProfileForVideoView(index, vui.maxProfile);
            if (subscribeUserVideo(vui.userId, vui.userName, mUserViewArray[index], profile)) {
                updateUserAudioState(index);
                return;
            }
        }

        mUserViewArray[index].isFree = true;
        mUserViewArray[index].setVisible(false);
    }

    private void stopUserVideoView(long userId, int index) {
        // check if there is unsubscribed video user
        CallViewModel.VideoUserInfo vui = getUnsubscribedVideoUser();
        if (vui != null) {
            Constants.VideoProfileType profile = getProfileForVideoView(index, vui.maxProfile);
            if (subscribeUserVideo(vui.userId, vui.userName, mUserViewArray[index], profile)) {
                updateUserAudioState(index);
                return;
            }
        }

        mUserViewArray[index].setVideoVisible(false);
    }

    int allocIndexForRemoteUser(UserManager.UserInfo user) {
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

    void setupUserView(UserManager.UserInfo user) {
        int index = allocIndexForRemoteUser(user);
        if (index != -1) {
            mUserViewArray[index].setUser(user.userId, user.userName);
            mUserViewArray[index].isFree = false;
            mUserViewArray[index].isSubscribed = false;
            mUserViewArray[index].setUserVisible(true);
            mUserViewArray[index].setVideoVisible(false);
            updateUserAudioState(index);
        }
    }

    private void setupUserVideoView(UserManager.UserInfo user) {
        int index = allocIndexForRemoteUser(user);
        if (index != -1) {
            if (!mUserViewArray[index].isFree && mUserViewArray[index].isScreen) {
                // the user screen is already subscribed
                return;
            }
            Constants.VideoProfileType profile = getProfileForVideoView(index, user.maxProfile);
            // 订阅此用户视频到指定视图
            if (subscribeUserVideo(user.userId, user.userName, mUserViewArray[index], profile)) {
                updateUserAudioState(index);
            }
        }
    }


    Constants.VideoProfileType getMaxProfileForVideoUser(long userId) {
        CallViewModel.VideoUserInfo vui = mViewModel.mVideoUsers.get(userId);
        if (vui != null) {
            return vui.maxProfile;
        }
        return Constants.VideoProfileType.HD720P;
    }
    Constants.VideoProfileType getProfileForVideoView(int index, Constants.VideoProfileType maxProfile) {
        return maxProfile;
    }
    void setupUserViewArray() {}
    void setupUserScreenView(UserManager.UserInfo user) {}
    int getAudioMutedResourceId(int index) { return R.drawable.small_microphone_muted; }
    int getAudioNormalResourceId(int index) { return R.drawable.small_microphone_normal; }
    void onUserLeft(long userId) {}



    @Override
    public void onRoomJoinConfirm(Constants.QResult result) {
        if (result == Constants.QResult.OK) {
            mViewModel.mIsRoomJoined = true;
            if (mViewModel.mEnableDebugMode) {
                mViewModel.rtcEngine().startAudioDump(PanoApplication.kMaxAudioDumpSize);
            }
            mViewModel.rtcEngine().stopPreview();
            PanoApplication app = (PanoApplication)getActivity().getApplication();
            app.mIsLocalVideoStarted = false;
            UserManager.UserInfo localUser = mViewModel.getUserManager().getLocalUser();
            // 启动本地视频
            mViewModel.mIsVideoClosed = !mViewModel.mAutoStartCamera;
            if(mViewModel.mAutoStartCamera){
                updateLocalVideoRender(mUserViewArray[0].view, 0);
                mViewModel.rtcEngine().startVideo(mViewModel.mLocalProfile, mViewModel.mIsFrontCamera);
                localUser.updateVideoState(true);
                app.mIsLocalVideoStarted = true;
            }
            // 启动本地音频
            mViewModel.rtcEngine().startAudio();
            localUser.updateAudioState(true);
            if (mViewModel.mAutoMuteAudio) {
                mViewModel.mIsAudioMuted = true;
                onLocalAudio(true);
            }
        } else {
            mViewModel.mIsRoomJoined = false;
        }
    }
    @Override
    public void onRoomLeaveIndication(Constants.QResult result) {
        mViewModel.mIsRoomJoined = false;
    }
    @Override
    public void onUserJoinIndication(UserManager.UserInfo user) {
        setupUserView(user);
    }
    @Override
    public void onUserLeaveIndication(UserManager.UserInfo user, Constants.UserLeaveReason reason) {
        stopUserVideo(user);
        stopUserScreen(user);
        stopUser(user);
        onUserLeft(user.userId);
    }
    @Override
    public void onUserAudioStart(UserManager.UserInfo user) {}
    @Override
    public void onUserAudioStop(UserManager.UserInfo user) {}
    @Override
    public void onUserAudioMute(UserManager.UserInfo user) {
        updateUserAudioState(getIndexForUser(user.userId));
    }
    @Override
    public void onUserAudioUnmute(UserManager.UserInfo user) {
        updateUserAudioState(getIndexForUser(user.userId));
    }
    @Override
    public void onUserVideoStart(UserManager.UserInfo user) {
        setupUserVideoView(user);
    }
    @Override
    public void onUserVideoStop(UserManager.UserInfo user) {
        // 取消订阅此用户视频
        if(mViewModel.rtcEngine().unsubscribeVideo(user.userId) == Constants.QResult.OK){
            stopUserVideo(user);
        }
    }
    @Override
    public void onUserVideoMute(UserManager.UserInfo user) {}
    @Override
    public void onUserVideoUnmute(UserManager.UserInfo user) {}
    @Override
    public void onUserScreenStart(UserManager.UserInfo user) {
        setupUserScreenView(user);
    }
    @Override
    public void onUserScreenStop(UserManager.UserInfo user) {
        // 取消订阅此用户桌面共享
        if(mViewModel.rtcEngine().unsubscribeScreen(user.userId) == Constants.QResult.OK){
            stopUserScreen(user);
        }
    }
    @Override
    public void onUserScreenMute(UserManager.UserInfo user) {}
    @Override
    public void onUserScreenUnmute(UserManager.UserInfo user) {}
    @Override
    public void onWhiteboardUnavailable() {}



    // listener
    public interface OnControlEventListener {
        void onShowHideControlPanel();
    }
}
