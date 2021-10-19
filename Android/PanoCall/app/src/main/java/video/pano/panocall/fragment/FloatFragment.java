package video.pano.panocall.fragment;

import static video.pano.panocall.info.Constant.KEY_GRID_POS;
import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_USE_PIN_VIDEO;
import static video.pano.panocall.info.Constant.KEY_VIDEO_ANNOTATION_START;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.IVideoRender;
import com.pano.rtc.api.PanoAnnotation;
import com.pano.rtc.api.RtcWbView;

import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.activity.CallActivity;
import video.pano.panocall.listener.AnnotationListener;
import video.pano.panocall.listener.OnPanoTouchListener;
import video.pano.panocall.listener.PanoAnnotationHandler;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.model.UserViewInfo;
import video.pano.panocall.utils.AnnotationHelper;
import video.pano.panocall.utils.Utils;


public class FloatFragment extends CallFragment implements PanoAnnotationHandler {

    private static final String TAG = "FloatFragment";

    private static final int DELAY_TIME = 1000;

    private static final int ALL_MODE = 1 ;
    private static final int ANNOTATION_MODE = 2 ;
    private static final int SCREEN_MODE = 3 ;
    private static final int PIN_VIDEO_MODE = 5 ;
    private static final int USER_LEAVE_MODE = 6 ;


    private RtcWbView mRtcWbView;
    private FrameLayout mRtcWbViewContainer ;
    private AnnotationListener mAnnotationListener;

    private boolean mPinVideoSuccess;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AnnotationListener) {
            mAnnotationListener = (AnnotationListener) context;
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_float, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.seAnnotationEventHandler(this);
        mRtcWbViewContainer = view.findViewById(R.id.large_whiteboard_view_container);
        mRtcWbView = new RtcWbView(getContext());
        mRtcWbViewContainer.addView(mRtcWbView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getExtra();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (localUser.isVideoStarted()) {
            if(mUserViewArray[0].userId == localUser.userId){
                updateLocalVideoRender(mLocalView,0,mLocalViewMirror);
            }else if(mUserViewArray[1].userId == localUser.userId){
                updateLocalVideoRender(mLocalView,1,mLocalViewMirror);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // save large view user
        mViewModel.mLargeViewUserId = mUserViewArray[0].userId;

    }

    private void getExtra(){
        Bundle bundle = getArguments();
        if(bundle != null){
            long userId = bundle.getLong(KEY_USER_ID);
            boolean usePinVideo = bundle.getBoolean(KEY_USE_PIN_VIDEO);
            if(usePinVideo && userId > 0){
                mPinVideoSuccess = onPinVideoItemClick(userId);
            }
            boolean annotationStart = bundle.getBoolean(KEY_VIDEO_ANNOTATION_START);
            if(annotationStart && userId > 0){
                onVideoAnnotationStart(userId, -1);
            }
        }
    }

    @Override
    void setupUserViewArray() {
        // 初始化视图数组，总共5个视频视图，1个大图4个小图，0为大图，其他为小图
        initUserViewArray(2);

        View view = getView();

        // 设置点击view时显示或隐藏控制按钮
        view.findViewById(R.id.cl_large_view).setOnTouchListener(new VideoTouchListener(getActivity()));

        // 配置大图的视图参数
        mUserViewArray[0].initView(
                view.findViewById(R.id.large_view),
                view.findViewById(R.id.tv_large_view_user),
                view.findViewById(R.id.img_large_view_audio),
                view.findViewById(R.id.img_large_view_default_head),
                view.findViewById(R.id.img_large_view_signal));

        mUserViewArray[0].rtcView.setOnLongClickListener(null);

        // 设置点击大图时显示或隐藏控制按钮
        mUserViewArray[0].rtcView.setOnTouchListener(new VideoTouchListener(getActivity()));

        // 配置右上角小图的视图参数
        mUserViewArray[1].initView(
                view.findViewById(R.id.small_view_righttop),
                true,
                view.findViewById(R.id.tv_small_view_righttop_user),
                view.findViewById(R.id.img_small_view_righttop_audio),
                view.findViewById(R.id.img_small_view_default_head),
                view.findViewById(R.id.img_small_view_righttop_signal),
                view.findViewById(R.id.cl_small_view_righttop));

        LongSparseArray<UserInfo> remoteUsers = mViewModel.getUserManager().getRemoteUsers();
        mLocalViewIndex = remoteUsers.size() > 0 ? 1 : 0;
        initUserVideoView();
    }

    void initUserVideoView() {
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();

        if (mViewModel.mIsRoomJoined || localUser.isVideoStarted()) {
            refreshVideoView(ALL_MODE,0L);
        } else {
            // 启动视频预览，并且显示到大图
            if (mViewModel.getUserManager().isRemoteEmpty()) {
                mUserViewArray[0].isFree = false;
                mUserViewArray[0].setUser(localUser.userId, localUser.userName,mViewModel.getUserManager().isMySelf(localUser.userId));
                if (mViewModel.mAutoStartCamera) {
                    mUserViewArray[0].setVisible(true);
                } else {
                    mUserViewArray[0].setDefaultHeadVisible(true);
                    mUserViewArray[0].setUserVisible(true);
                }
                updateUserAudioState(0);

                if(mViewModel.mAutoStartCamera){
                    updateLocalVideoRender(mUserViewArray[0].rtcView, 0);
                    PanoApplication app = (PanoApplication)Utils.getApp();
                    if (!app.mIsLocalVideoStarted) {
                        mViewModel.rtcEngine().startPreview(mViewModel.mLocalProfile, mViewModel.mIsFrontCamera);
                        app.mIsLocalVideoStarted = true;
                    }
                }
            }
        }
    }


    @Override
    Constants.VideoProfileType getProfileForVideoView(int index) {
        Constants.VideoProfileType profile = Constants.VideoProfileType.Low;
        if (index == 0) {
            profile = Constants.VideoProfileType.HD1080P;
        }
        return profile;
    }

    @Override
    int getIndexForFreeRemoteUser(UserInfo user) {
        int viewIndex = getIndexForUser(user.userId);
        if (viewIndex != -1) {
            return viewIndex;
        }
        // 此用户还未显示，找到一个空闲的显示位置
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        // 先检查大图是否空闲，如空闲则将此用户显示到大图
        if (mUserViewArray[0].isFree || (mUserViewArray[0].userId == localUser.userId && !AnnotationHelper.getIns().annotationHost())) {
            // large view is free or used by local user, then make this user to large view
            // 如果大图被本地用户占用，则将本地用户移到小图，如果没有空闲的小图，则不显示本地用户视频
            if (mUserViewArray[0].userId == localUser.userId) {
                // move local user to last view
                mLocalView = null;
                if (mUserViewArray[mUserViewCount - 1].isFree) {
                    mLocalView = mUserViewArray[mUserViewCount - 1].rtcView;
                    if (localUser.isVideoStarted()) {
                        mUserViewArray[mUserViewCount - 1].setVisible(true);
                    } else {
                        mUserViewArray[mUserViewCount - 1].setDefaultHeadVisible(true);
                        mUserViewArray[mUserViewCount - 1].setUserVisible(true);
                    }
                    mUserViewArray[mUserViewCount - 1].isFree = false;
                    mUserViewArray[mUserViewCount - 1].isScreen = false;
                    mUserViewArray[mUserViewCount - 1].setUser(localUser.userId, localUser.userName,mViewModel.getUserManager().isMySelf(localUser.userId));
                    updateUserAudioState(mUserViewCount - 1);
                }
                updateLocalVideoRender(mLocalView, mUserViewCount - 1);
            }
            viewIndex = 0;
        } else {
            // 如大图不空闲，则尝试找到一个空闲的小图
            // try to find a free small view
            if (mUserViewArray[1].isFree) {
                viewIndex = 1;
            }
        }

        if (viewIndex != -1) {
            mUserViewArray[viewIndex].isFree = true;
            mUserViewArray[viewIndex].isScreen = false;
            mUserViewArray[viewIndex].isSubscribed = false;
        }
        return viewIndex;
    }


    @Override
    void onUserLeave(long userId) {
        if (AnnotationHelper.getIns().getAnnotationUserId() == userId) {
            onVideoAnnotationStop(userId,-1);
        }
    }


    @Override
    public void onAnnotationToolsClick() {
        mRtcWbView.setPassThrough(false);
    }

    /***************************Start/Stop LocalVideo*******************************************/
    @Override
    protected void stopLocalVideo() {
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (mViewModel.mIsRoomJoined) {
            mViewModel.rtcEngine().stopVideo();
            localUser.setVideoStarted(false);
        } else {
            mViewModel.rtcEngine().stopPreview();
        }
        clearLocalVideoRender();
        if (localUser.userId == mUserViewArray[mLocalViewIndex].userId) {
            mUserViewArray[mLocalViewIndex].setVideoVisible(false);
            mUserViewArray[mLocalViewIndex].isFree = true;
        }
        PanoApplication app = (PanoApplication)Utils.getApp();
        app.mIsLocalVideoStarted = false;
    }

    @Override
    protected void startLocalVideo() {
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (mViewModel.mIsRoomJoined) {
            mViewModel.rtcEngine().startVideo(mViewModel.mLocalProfile,mViewModel.mIsFrontCamera);
            localUser.setVideoStarted(true);
        } else {
            mViewModel.rtcEngine().startPreview(mViewModel.mLocalProfile, mViewModel.mIsFrontCamera);
        }

        for (int i = 0; i < mUserViewCount; i++) {
            if (mUserViewArray[i].userId == localUser.userId) {
                mUserViewArray[i].isFree = false;
                mUserViewArray[i].setUser(localUser.userId, localUser.userName,mViewModel.getUserManager().isMySelf(localUser.userId));
                mUserViewArray[i].setVisible(localUser.isVideoStarted());
                updateLocalVideoRender(mUserViewArray[i].rtcView, i);
                break;
            }
        }

        PanoApplication app = (PanoApplication)Utils.getApp();
        app.mIsLocalVideoStarted = true;
    }

    /***************************Start/Stop LocalVideo*******************************************/

    /***************************Indication Start*******************************************/
    @Override
    public void onUserLeaveIndication(UserInfo user, Constants.UserLeaveReason reason) {
        refreshVideoView(USER_LEAVE_MODE,user.userId);
        onUserLeave(user.userId);
    }

    /******************************Screen Start*********************************************/
    @Override
    public void onUserScreenStart(long userId) {
        refreshVideoView(SCREEN_MODE,userId);
    }

    @Override
    public void onUserScreenStop(UserInfo user) {
        if (AnnotationHelper.getIns().annotationEnable()) {
            return;
        }
        refreshVideoView(ALL_MODE,0L);
    }

    /******************************Screen Start*********************************************/


    /******************************Annotation Start*********************************************/
    @Override
    public void onClickAnnotationStop() {
        AnnotationHelper.getIns().setAnnotationEnable(false);
        removeAnnotationView();
        PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
        if (AnnotationHelper.getIns().annotationHost() && annotation != null) {
            refreshVideoView(ALL_MODE,0L);
            annotation.stopAnnotation();
            AnnotationHelper.getIns().setAnnotation(null);
            AnnotationHelper.getIns().setAnnotationUserId(0L);
            AnnotationHelper.getIns().setAnnotationHost(false);
        }
        if (mAnnotationListener != null) {
            mAnnotationListener.onAnnotationClose();
        }
    }

    @Override
    public void onClickAnnotationStart() {
        PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
        Log.i(TAG, "startAnnotation annotation " + annotation);
        if (annotation == null) {
            addAnnotationView();
            refreshVideoView(ANNOTATION_MODE,mViewModel.getLocalUserId());

            annotation = mViewModel.rtcEngine().getAnnotationMgr().getVideoAnnotation(mViewModel.getLocalUserId(), 0);
            annotation.startAnnotation(mRtcWbView);
            AnnotationHelper.getIns().setAnnotation(annotation);
            AnnotationHelper.getIns().setAnnotationHost(true);
            AnnotationHelper.getIns().setAnnotationUserId(mViewModel.getLocalUserId());
        } else {
            Log.i(TAG, "remote annotation: " + annotation.getClass().getName());
            AnnotationHelper.getIns().setAnnotationHost(false);
            refreshVideoView(ANNOTATION_MODE,AnnotationHelper.getIns().getAnnotationUserId());
            mRtcWbView.setVisibility(View.VISIBLE);
            annotation.startAnnotation(mRtcWbView);
        }
    }

    @Override
    public void onVideoAnnotationStart(long userId, int streamId) {
        Log.i(TAG, "onVideoAnnotationStart");
        mHandler.postDelayed(() -> {
            UserInfo user = mViewModel.getUserManager().getRemoteUser(userId);
            if (user == null) {
                return;
            }
            PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
            if(annotation == null){
                return ;
            }
            AnnotationHelper.getIns().setAnnotationUserId(userId);
            AnnotationHelper.getIns().setAnnotationHost(false);
            addAnnotationView();
            annotation.startAnnotation(mRtcWbView);
            Log.i(TAG, "switchToLargeView " + userId);
            refreshVideoView(ANNOTATION_MODE,userId);

            Toast.makeText(getActivity(), getString(R.string.annotation_user_start, user.userName), Toast.LENGTH_SHORT).show();
            if (mAnnotationListener != null) {
                mAnnotationListener.onAnnotationStart();
            }
        }, DELAY_TIME);
    }

    @Override
    public void onVideoAnnotationStop(long userId, int streamId) {
        Log.i(TAG, "onVideoAnnotationStop");
        AnnotationHelper.getIns().setAnnotationEnable(false);
        mHandler.post(() -> {
            AnnotationHelper.getIns().setAnnotationUserId(0L);
            removeAnnotationView();
            PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
            if (annotation != null) {
                annotation.stopAnnotation();
                AnnotationHelper.getIns().setAnnotation(null);
            }
            UserInfo user = mViewModel.getUserManager().getRemoteUser(userId);
            if (user != null) {
                Toast.makeText(getActivity(), getString(R.string.annotation_user_stop, user.userName), Toast.LENGTH_SHORT).show();
            }
            refreshVideoView(ALL_MODE,0L);
            if (mAnnotationListener != null) {
                mAnnotationListener.onAnnotationClose();
            }
        });
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        AnnotationHelper.getIns().setAnnotationHost(false);
        Log.i(TAG, "onShareAnnotationStart userId = " + userId);
        mHandler.postDelayed(() -> {
            UserInfo user = mViewModel.getUserManager().getRemoteUser(userId);
            if (user == null) {
                return;
            }
            AnnotationHelper.getIns().setAnnotationUserId(userId);
            if (mUserViewArray != null && mUserViewArray.length > 0) {
                mUserViewArray[0].rtcView.setScalingRatio(IVideoRender.ScalingRatio.SCALE_RATIO_FIT);
            }
            addAnnotationView();

            AnnotationHelper.getIns().getAnnotation().startAnnotation(mRtcWbView);
            Toast.makeText(getActivity(), getString(R.string.annotation_user_start, user.userName), Toast.LENGTH_SHORT).show();
            if (mAnnotationListener != null) {
                mAnnotationListener.onAnnotationStart();
            }
        }, DELAY_TIME);
    }

    @Override
    public void onShareAnnotationStop(long userId) {
        Log.i(TAG, "onShareAnnotationStop");
        AnnotationHelper.getIns().setAnnotationEnable(false);
        mHandler.post(() -> {
            AnnotationHelper.getIns().setAnnotationUserId(0L);

            removeAnnotationView();

            PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
            if (annotation != null) {
                annotation.stopAnnotation();
                AnnotationHelper.getIns().setAnnotation(null);
            }
            UserInfo user = mViewModel.getUserManager().getRemoteUser(userId);
            if (user != null) {
                Toast.makeText(getActivity(), getString(R.string.annotation_user_stop, user.userName), Toast.LENGTH_SHORT).show();
            }
            if (mAnnotationListener != null) {
                mAnnotationListener.onAnnotationClose();
            }
        });
    }

    private void clearAnnotationView(){
        PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
        if (annotation != null) {
            removeAnnotationView();
            AnnotationHelper.getIns().setAnnotationEnable(false);
            if (mAnnotationListener != null) {
                mAnnotationListener.onAnnotationClose();
            }
            if (AnnotationHelper.getIns().annotationHost()) {
                annotation.stopAnnotation();
                AnnotationHelper.getIns().setAnnotation(null);
            }
        }
    }

    private void addAnnotationView(){
        mRtcWbView = new RtcWbView(getContext());
        mRtcWbView.setPassThrough(true);

        mRtcWbViewContainer.removeAllViews();
        mRtcWbViewContainer.addView(mRtcWbView);
    }

    private void removeAnnotationView(){
        mRtcWbView.setVisibility(View.GONE);
    }

    /******************************Annotation Stop*********************************************/

    /******************************Whiteboard Start*********************************************/
    @Override
    public void onWhiteboardStart() {
        clearAnnotationView();
    }
    /******************************Whiteboard Start*********************************************/

    /******************************Pin Video Start*********************************************/

    private boolean onPinVideoItemClick(long userId) {
        Toast.makeText(getActivity(),R.string.msg_pin_video_tips,Toast.LENGTH_LONG).show();
        return refreshVideoView(PIN_VIDEO_MODE,userId) ;
    }

    /******************************Pin Video Stop*********************************************/

    /****************************  RefreshView  Start******************************************/
    private void clearViewRender(UserViewInfo userViewInfo) {
        if (userViewInfo == null) {
            return;
        }
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (userViewInfo.userId == localUser.userId) {
            clearLocalVideoRender();
        } else if (userViewInfo.isScreen) {
            clearRemoteScreenRender(userViewInfo.userId);
        } else {
            clearRemoteVideoRender(userViewInfo.userId);
        }
    }

    private boolean subscribeLocalVideo(UserViewInfo userViewInfo, UserInfo userInfo, int index , boolean isMirror) {
        userViewInfo.setUser(userInfo.userId, userInfo.userName,mViewModel.getUserManager().isMySelf(userInfo.userId));
        userViewInfo.setVideoVisible(userInfo.isVideoStarted());
        userViewInfo.setUserVisible(true);
        userViewInfo.isFree = false;
        userViewInfo.isScreen = false;
        userViewInfo.isSubscribed = false;
        updateLocalVideoRender(userViewInfo.rtcView, index,isMirror);

        return true ;
    }

    @Override
    protected boolean subscribeUserVideo(long userId, String userName, UserViewInfo viewInfo, UserInfo userInfo, Constants.VideoProfileType profile) {
        viewInfo.rtcView.setMirror(false);
        viewInfo.setUser(userId, userName,mViewModel.getUserManager().isMySelf(userId));
        viewInfo.setVideoVisible(userInfo.isVideoStarted());
        viewInfo.setUserVisible(true);
        viewInfo.isFree = false;
        viewInfo.isScreen = false;
        updateRemoteVideoRender(userId,viewInfo.rtcView);

        if(userInfo.isVideoStarted()) {
            Constants.QResult ret = mViewModel.rtcEngine().subscribeVideo(userId, profile);
            if (ret == Constants.QResult.OK) {
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
        return true;
    }

    private boolean refreshVideoView(int mode , long userId){
        UserViewInfo bigViewInfo = mUserViewArray[0];
        UserViewInfo smallViewInfo = mUserViewArray[1];

        if(mode == ALL_MODE || (mode == USER_LEAVE_MODE && bigViewInfo.userId == userId)){
            clearViewRender(bigViewInfo);
            clearViewRender(smallViewInfo);

            if (!mViewModel.getUserManager().isScreenEmpty()) {
                refreshLocalView(mViewModel.getUserManager().getScreenUsers().valueAt(0),null);
            } else if (!mViewModel.getUserManager().isVideoEmpty()) {
                refreshLocalView(null,mViewModel.getUserManager().getVideoUsers().valueAt(0));
            } else if (!mViewModel.getUserManager().isRemoteEmpty()) {
                refreshLocalView(null,mViewModel.getUserManager().getRemoteUsers().valueAt(0));
            }else {
                subscribeLocalVideo(bigViewInfo, mViewModel.getUserManager().getLocalUser(), 0,true);
                updateUserAudioState(0);
            }
            refreshRemoteView();

        }else if(mode == USER_LEAVE_MODE && smallViewInfo.userId == userId){
            clearViewRender(smallViewInfo);
            refreshRemoteView();

        }else if(mode == ANNOTATION_MODE){

            clearViewRender(bigViewInfo);
            clearViewRender(smallViewInfo);

            if (userId == mViewModel.getUserManager().getLocalUser().userId) {
                subscribeLocalVideo(bigViewInfo, mViewModel.getUserManager().getLocalUser(), 0,false);
                updateUserAudioState(0);
            } else {
                refreshLocalView(null,mViewModel.getUserManager().getRemoteUser(userId));
            }
            refreshRemoteView();

        }else if(mode == SCREEN_MODE){
            if (AnnotationHelper.getIns().annotationEnable()) {
                return false;
            }
            clearViewRender(bigViewInfo);
            clearViewRender(smallViewInfo);

            refreshLocalView(mViewModel.getUserManager().getRemoteUser(userId),null);
            refreshRemoteView();

        }else if(mode == PIN_VIDEO_MODE){
            if (userId == bigViewInfo.userId) {
                return false;
            }
            if (smallViewInfo.isFree) {
                return false;
            }
            if (!bigViewInfo.isFree && bigViewInfo.isScreen) {
                return false;
            }
            clearViewRender(bigViewInfo);
            clearViewRender(smallViewInfo);

            refreshLocalView(null,mViewModel.getUserManager().getRemoteUser(userId));
            refreshRemoteView();
        }

        return true ;
    }

    private void refreshLocalView(UserInfo bigScreenUser , UserInfo bigRemoteUser){
        UserViewInfo bigViewInfo = mUserViewArray[0];

        //Refresh Local View
        if(bigScreenUser != null){
            subscribeUserScreen(bigScreenUser.userId, bigScreenUser.userName, bigViewInfo);
        }else if(bigRemoteUser != null){
            subscribeUserVideo(bigRemoteUser.userId, bigRemoteUser.userName, bigViewInfo, bigRemoteUser,getProfileForVideoView(0));
        }
        updateUserAudioState(0);
    }

    private void refreshRemoteView(){
        UserViewInfo bigViewInfo = mUserViewArray[0];
        UserViewInfo smallViewInfo = mUserViewArray[1];

        if(mViewModel.getUserManager().getLocalUser().userId  != bigViewInfo.userId){
            subscribeLocalVideo(smallViewInfo, mViewModel.getUserManager().getLocalUser(), 1,false);
        } else if(!mViewModel.getUserManager().isRemoteEmpty()) {
            UserInfo remoteUser = mViewModel.getUserManager().getRemoteUsers().valueAt(0);
            if (remoteUser != null && remoteUser.userId != bigViewInfo.userId) {
                subscribeUserVideo(remoteUser.userId, remoteUser.userName, smallViewInfo, remoteUser, getProfileForVideoView(1));
            }
        } else {
            smallViewInfo.isScreen = false;
            smallViewInfo.isSubscribed = false;
            smallViewInfo.isFree = true;
            smallViewInfo.setVisible(false);
        }

        updateUserAudioState(1);
    }

    /****************************  RefreshView  End******************************************/

    @Override
    int getAudioMutedResourceId(int index) {
        return index == 0 ? R.drawable.svg_icon_audio_mute : R.drawable.svg_icon_small_audio_mute;
    }

    @Override
    int getAudioNormalResourceId(int index) {
        return index == 0 ? R.drawable.svg_icon_audio_normal : R.drawable.svg_icon_small_audio_normal;
    }

    @Override
    int getSignalLowResourceId(int index){
        return index == 0 ? R.drawable.svg_icon_signal_low : R.drawable.svg_icon_small_signal_low;
    }

    @Override
    int getSignalPoorResourceId(int index){
        return index == 0 ? R.drawable.svg_icon_signal_poor : R.drawable.svg_icon_small_signal_poor;
    }

    @Override
    int getSignalGoodResourceId(int index){
        return index == 0 ? R.drawable.svg_icon_signal_good : R.drawable.svg_icon_small_signal_good;
    }

    class VideoTouchListener extends OnPanoTouchListener {

        public VideoTouchListener(Context c) {
            super(c);
        }

        @Override
        public void onSingleTap() {
            if (mListener != null) {
                mListener.onShowHideControlPanel();
            }
        }
        @Override
        public void onSwipeLeft() {
            if (mViewModel.getUserManager().getRemoteUsers().size() < 2) {
                return;
            }
            if(AnnotationHelper.getIns().annotationEnable()){
                return ;
            }
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_GRID_POS,1);
            NavHostFragment.findNavController(FloatFragment.this)
                    .navigate(R.id.action_FloatFragment_to_GridFragment,bundle);
            clearViewRender(mUserViewArray[1]);
            clearViewRender(mUserViewArray[0]);
        }

        @Override
        public void onDoubleTap() {
            if(mPinVideoSuccess){
                refreshVideoView(ALL_MODE,0L);
                Toast.makeText(getActivity(),R.string.msg_exit_pin_video_tips,Toast.LENGTH_LONG).show();
                mPinVideoSuccess = false ;

                Bundle bundle = getArguments();
                if(bundle != null){
                    bundle.putBoolean(KEY_USE_PIN_VIDEO,false);
                }
            }
        }
    }
}
