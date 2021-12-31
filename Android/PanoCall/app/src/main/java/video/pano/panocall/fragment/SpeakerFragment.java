package video.pano.panocall.fragment;

import static video.pano.panocall.info.Constant.KEY_GRID_PAGE_NUM;
import static video.pano.panocall.info.Constant.KEY_LOCAL_ANNOTATION_START;
import static video.pano.panocall.info.Constant.KEY_SHARE_ANNOTATION_START;
import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_USE_PIN_VIDEO;
import static video.pano.panocall.info.Constant.KEY_VIDEO_ANNOTATION_START;
import static video.pano.panocall.rtc.MeetingViewFactory.MODE_LOCAL_VIDEO;
import static video.pano.panocall.rtc.MeetingViewFactory.MODE_REMOTE_SCREEN;
import static video.pano.panocall.rtc.MeetingViewFactory.MODE_REMOTE_VIDEO;
import static video.pano.panocall.rtc.MeetingViewFactory.TYPE_LARGE_VIEW;
import static video.pano.panocall.rtc.MeetingViewFactory.TYPE_SMALL_VIEW;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDestination;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.IVideoRender;
import com.pano.rtc.api.PanoAnnotation;
import com.pano.rtc.api.RtcWbView;

import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.info.Config;
import video.pano.panocall.info.UserManager;
import video.pano.panocall.listener.OnPanoTouchListener;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.rtc.MeetingViewFactory;
import video.pano.panocall.rtc.MeetingViewInfo;
import video.pano.panocall.utils.AnnotationHelper;
import video.pano.panocall.utils.Utils;

public class SpeakerFragment extends MeetingFragment  {

    private static final String TAG = "SpeakerFragment";

    private static final int ALL_MODE = 1;
    private static final int ANNOTATION_MODE = 2;
    private static final int SCREEN_START_MODE = 3;
    private static final int PIN_VIDEO_MODE = 5;
    private static final int USER_LEAVE_MODE = 6;
    private static final int SCREEN_STOP_MODE = 7;

    private RtcWbView mRtcWbView;
    private MeetingViewInfo mSmallMeetingView;

    private boolean mPinVideoSuccess;
    private int mSaveMode = ALL_MODE ;
    private long mSaveUserId = 0L ;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_speaker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();
        initViews(view);
        setupUserVideoView();
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
        if(mLocalMeetingView != null) getLifecycle().removeObserver(mLocalMeetingView);
        if(mSmallMeetingView != null) getLifecycle().removeObserver(mSmallMeetingView);
    }

    @Override
    public void onCheckState() {
        refreshVideoView(mSaveMode,mSaveUserId);
    }

    @Override
    protected void initViewModel(){
        super.initViewModel();
    }

    private void getExtra() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            long userId = bundle.getLong(KEY_USER_ID);
            boolean usePinVideo = bundle.getBoolean(KEY_USE_PIN_VIDEO);
            if (usePinVideo && userId > 0) {
                mPinVideoSuccess = onPinVideoItemClick(userId);
            }
            boolean videoAnnotation = bundle.getBoolean(KEY_VIDEO_ANNOTATION_START);
            boolean shareAnnotation = bundle.getBoolean(KEY_SHARE_ANNOTATION_START);
            if (videoAnnotation && userId > 0) {
                mViewModel.onVideoAnnotationStart(userId,0);
            }
            if (shareAnnotation && userId > 0) {
                mViewModel.onShareAnnotationStart(userId);
            }
            setArguments(null);
        }
    }

    private void initViews(@NonNull View view) {
        ViewGroup largeViewContainer = view.findViewById(R.id.cl_large_view);
        ViewGroup smallViewContainer = view.findViewById(R.id.cl_small_view);

        mLocalMeetingView = MeetingViewFactory.createMeetingViewInfo(getContext(), TYPE_LARGE_VIEW);
        mSmallMeetingView = MeetingViewFactory.createMeetingViewInfo(getContext(), TYPE_SMALL_VIEW);
        largeViewContainer.removeAllViews();
        smallViewContainer.removeAllViews();
        largeViewContainer.addView(mLocalMeetingView.getInfoView());
        smallViewContainer.addView(mSmallMeetingView.getInfoView());

        mLocalMeetingView.setParentView(largeViewContainer);
        mSmallMeetingView.setParentView(smallViewContainer);

        getLifecycle().addObserver(mLocalMeetingView);
        getLifecycle().addObserver(mSmallMeetingView);

        mLocalMeetingView.setOnTouchListener(new VideoTouchListener(getActivity()));

        mLocalMeetingView.initUserStatistics();
    }

    private void setupUserVideoView() {
        UserInfo localUser = UserManager.getIns().getLocalUser();

        if (mViewModel.mIsRoomJoined || localUser.isVideoStarted()) {
            refreshVideoView(ALL_MODE, 0L);
        } else {
            // 启动视频预览，并且显示到大图
            startPreviewLocalUser();
        }
    }

    private boolean refreshVideoView(int mode, long userId) {
        mSaveMode = mode ;
        mSaveUserId = userId ;

        if(mViewModel.mPaused){
            return false ;
        }
        UserInfo largeUser = null;
        UserInfo smallUser = null;
        switch (mode) {
            case ALL_MODE:
                largeUser = getLargeUserInfo();
                smallUser = getSmallUserInfo(largeUser.userId);
                refreshLargeView(largeUser);
                refreshSmallView(smallUser);
                break;
            case USER_LEAVE_MODE:
                if (mLocalMeetingView.checkUserInfo(userId)) {
                    largeUser = getLargeUserInfo();
                    smallUser = getSmallUserInfo(largeUser.userId);
                    refreshLargeView(largeUser);
                    refreshSmallView(smallUser);
                } else if (mSmallMeetingView.checkUserInfo(userId)) {
                    smallUser = getSmallUserInfo(mLocalMeetingView.getUserId());
                    refreshSmallView(smallUser);
                }
                break;
            case ANNOTATION_MODE:
                if (userId == UserManager.getIns().getLocalUser().userId) {
                    largeUser = UserManager.getIns().getLocalUser();
                    buildViewInfo(mLocalMeetingView, !Config.sIsFrontCamera, largeUser, MODE_LOCAL_VIDEO);
                } else {
                    largeUser = UserManager.getIns().getRemoteUser(userId);
                    refreshLargeView(largeUser);
                }
                if(largeUser != null){
                    smallUser = getSmallUserInfo(largeUser.userId);
                    refreshSmallView(smallUser);
                }
                break;
            case SCREEN_START_MODE:
                if (AnnotationHelper.getIns().annotationEnable()) {
                    return false;
                }
                largeUser = UserManager.getIns().getRemoteUser(userId);
                smallUser = getSmallUserInfo(largeUser.userId);
                refreshLargeView(largeUser);
                refreshSmallView(smallUser);
                break;
            case SCREEN_STOP_MODE:
                // 取消订阅此用户桌面共享
                if (AnnotationHelper.getIns().annotationEnable()) {
                    return false;
                }
                largeUser = getLargeUserInfo();
                smallUser = getSmallUserInfo(largeUser.userId);
                refreshLargeView(largeUser);
                refreshSmallView(smallUser);
                break;
            case PIN_VIDEO_MODE:
                if (UserManager.getIns().getScreenUser(userId) != null) {
                    largeUser = UserManager.getIns().getScreenUser(userId);
                } else {
                    largeUser = UserManager.getIns().getRemoteUser(userId);
                }
                smallUser = getSmallUserInfo(largeUser.userId);
                refreshLargeView(largeUser);
                refreshSmallView(smallUser);
                break;
            default:
                break;
        }
        subscribeVideo(mLocalMeetingView);
        subscribeVideo(mSmallMeetingView);
        updateUserAudioState(largeUser);
        updateUserAudioState(smallUser);
        return true;
    }

    private UserInfo getLargeUserInfo() {
        UserInfo largeUser ;
        if (UserManager.getIns().getScreenSize() > 0) {
            largeUser = UserManager.getIns().getScreenUsers().get(
                    UserManager.getIns().getScreenSize() - 1);
        } else if (!UserManager.getIns().isVideoEmpty()) {
            largeUser = UserManager.getIns().getVideoUsers().valueAt(0);
        } else if (!UserManager.getIns().isRemoteEmpty()) {
            largeUser = UserManager.getIns().getRemoteUsers().valueAt(0);
        } else {
            largeUser = UserManager.getIns().getLocalUser();
        }
        return largeUser;
    }

    private UserInfo getSmallUserInfo(long largeUserId) {
        UserInfo smallUser = null;
        if (UserManager.getIns().getLocalUser().userId != largeUserId) {
            smallUser = UserManager.getIns().getLocalUser();
        } else if (!UserManager.getIns().isRemoteEmpty()) {
            UserInfo remoteUser = UserManager.getIns().getRemoteUsers().valueAt(0);
            if (remoteUser != null && remoteUser.userId != largeUserId) {
                smallUser = remoteUser;
            }
        }
        return smallUser;
    }

    private MeetingViewInfo refreshLargeView(UserInfo largeUser) {
        if (largeUser == null) {
            return null;
        }
        if (largeUser.isScreenStarted()) {
            return buildViewInfo(mLocalMeetingView, false, largeUser, MODE_REMOTE_SCREEN);
        } else if (largeUser.userId != UserManager.getIns().getLocalUserId()) {
            return buildViewInfo(mLocalMeetingView, false, largeUser, MODE_REMOTE_VIDEO);
        } else {
            return buildViewInfo(mLocalMeetingView, Config.sIsFrontCamera, largeUser, MODE_LOCAL_VIDEO);
        }
    }

    private MeetingViewInfo refreshSmallView(UserInfo smallUser) {
        if (mSmallMeetingView.checkUserInfo(smallUser)) {
            return null;
        }
        if (smallUser == null) {
            mSmallMeetingView.setInfoViewVisible(false);
            mSmallMeetingView.release();
            return mSmallMeetingView;
        } else {
            mSmallMeetingView.setInfoViewVisible(true);
            if (smallUser.userId != UserManager.getIns().getLocalUserId()) {
                return buildViewInfo(mSmallMeetingView, false, smallUser, MODE_REMOTE_VIDEO);
            } else {
                return buildViewInfo(mSmallMeetingView, Config.sIsFrontCamera, smallUser, MODE_LOCAL_VIDEO);
            }
        }
    }

    @Override
    protected void unSubscribeAllMeetingView(){
        if(mLocalMeetingView != null){
            unSubscribeVideo(mLocalMeetingView);
            mLocalMeetingView.release();
        }
        if(mSmallMeetingView != null){
            unSubscribeVideo(mSmallMeetingView);
            mSmallMeetingView.release();
            mSmallMeetingView.setInfoViewVisible(false);
        }
    }

    @Override
    public void updateUserAudioState(UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        if (mLocalMeetingView.checkUserInfo(userInfo)) {
            int largeAudioResource = -1;
            if (userInfo.isPSTNAudioType()) {
                largeAudioResource = userInfo.isAudioMuted() ? getAudioMutePSTNResourceId(true) : getAudioNormalPSTNResourceId(true);
            } else {
                largeAudioResource = userInfo.isAudioMuted() ? getAudioMutedResourceId(true) : getAudioNormalResourceId(true);
            }
            mLocalMeetingView.updateAudioImg(largeAudioResource);
        }
        if (mSmallMeetingView.checkUserInfo(userInfo)) {
            int smallAudioResource = -1;
            if (userInfo.isPSTNAudioType()) {
                smallAudioResource = userInfo.isAudioMuted() ? getAudioMutePSTNResourceId(false) : getAudioNormalPSTNResourceId(false);
            } else {
                smallAudioResource = userInfo.isAudioMuted() ? getAudioMutedResourceId(false) : getAudioNormalResourceId(false);
            }
            mSmallMeetingView.updateAudioImg(smallAudioResource);
        }
    }

    /**************  RtcMediaStatsObserver  **************/
    /**************  RtcMediaStatsObserver  **************/

    /***************************Indication Start ************************************/
    @Override
    public void onUserJoinIndication(UserInfo user) {
        if (!mLocalMeetingView.isEmptyUserInfo() && !mSmallMeetingView.isEmptyUserInfo()) {
            return;
        }
        UserInfo localUser = UserManager.getIns().getLocalUser();
        if (mLocalMeetingView.isEmptyUserInfo()) {
            switchViewInfoData(mLocalMeetingView,user);
        } else if (mLocalMeetingView.checkUserInfo(localUser) && !AnnotationHelper.getIns().annotationHost()) {
            switchViewInfoData(mLocalMeetingView,user);
            if (mSmallMeetingView.isEmptyUserInfo()) {
                mSmallMeetingView.setData(localUser);
                subscribeLocalVideo(mSmallMeetingView, localUser);
                updateUserAudioState(localUser);
            }
        } else {
            switchViewInfoData(mSmallMeetingView,user);
        }
    }

    @Override
    public void onUserLeaveIndication(UserInfo user) {
        refreshVideoView(USER_LEAVE_MODE, user.userId);
        if (AnnotationHelper.getIns().getAnnotationUserId() == user.userId) {
            onVideoAnnotationStop(user.userId);
        }
    }

    @Override
    public void onUserVideoStart(UserInfo user) {
        if (mLocalMeetingView.isEmptyUserInfo()
                || (mLocalMeetingView.checkUserInfo(user) && !user.isScreenStarted())) {
            if (user.userId != UserManager.getIns().getLocalUserId()) {
                buildViewInfo(mLocalMeetingView, false, user, MODE_REMOTE_VIDEO);
            } else {
                buildViewInfo(mLocalMeetingView, Config.sIsFrontCamera, user, MODE_LOCAL_VIDEO);
            }
            subscribeVideo(mLocalMeetingView);
        } else if (mSmallMeetingView.isEmptyUserInfo() || mSmallMeetingView.checkUserInfo(user)) {
            if (user.userId != UserManager.getIns().getLocalUserId()) {
                buildViewInfo(mSmallMeetingView, false, user, MODE_REMOTE_VIDEO);
            } else {
                buildViewInfo(mSmallMeetingView, Config.sIsFrontCamera, user, MODE_LOCAL_VIDEO);
            }
            subscribeVideo(mSmallMeetingView);
        }
    }

    @Override
    public void onUserVideoStop(UserInfo user) {
        // 取消订阅此用户视频
        if (mLocalMeetingView.checkUserInfo(user) && !user.isScreenStarted()) {
            unSubscribeVideo(mLocalMeetingView);
        } else if (mSmallMeetingView.checkUserInfo(user)) {
            unSubscribeVideo(mSmallMeetingView);
        }
    }
    /******************************Indication End ****************************************/
    /******************************Screen Start*********************************************/
    @Override
    public void onUserScreenStart(long userId) {
        super.onUserScreenStart(userId);
        refreshVideoView(SCREEN_START_MODE, userId);
    }

    @Override
    public void onUserScreenStop(UserInfo user) {
        super.onUserScreenStop(user);
        refreshVideoView(SCREEN_STOP_MODE, user.userId);
    }
    /******************************Screen Start*********************************************/
    /***************************ASL Start***********************************************************/
    /***************************ASL End***********************************************************/

    @Override
    public void onNetworkQuality(long userId, Constants.QualityRating quality) {
        MeetingViewInfo meetingViewInfo = null;
        boolean isLarge = false;
        if (mLocalMeetingView.checkUserInfo(userId)) {
            meetingViewInfo = mLocalMeetingView;
            isLarge = true;
        } else if (mSmallMeetingView.checkUserInfo(userId)) {
            meetingViewInfo = mSmallMeetingView;
            isLarge = false;
        }
        if (meetingViewInfo == null) return;

        if (quality == Constants.QualityRating.Excellent || quality == Constants.QualityRating.Good) {
            meetingViewInfo.updateSignalImg(getSignalGoodResourceId(isLarge));
        } else if (quality == Constants.QualityRating.Poor) {
            meetingViewInfo.updateSignalImg(getSignalPoorResourceId(isLarge));
        } else {
            meetingViewInfo.updateSignalImg(getSignalLowResourceId(isLarge));
        }
    }

    /***************************Start/Stop LocalVideo*******************************************/

    @Override
    public void onSwitchCamera() {
        UserInfo localUser = UserManager.getIns().getLocalUser();
        if (mLocalMeetingView.checkUserInfo(localUser.userId)) {
            mLocalMeetingView.refreshMirror(Config.sIsFrontCamera);
        } else if (mSmallMeetingView.checkUserInfo(localUser.userId)) {
            mSmallMeetingView.refreshMirror(Config.sIsFrontCamera);
        }
    }

    @Override
    public void stopLocalVideo() {
        UserInfo localUser = UserManager.getIns().getLocalUser();
        if (mLocalMeetingView.checkUserInfo(localUser.userId)) {
            unSubscribeVideo(mLocalMeetingView);
        } else if (mSmallMeetingView.checkUserInfo(localUser.userId)) {
            unSubscribeVideo(mSmallMeetingView);
        }
    }

    @Override
    public void startLocalVideo() {
        UserInfo localUser = UserManager.getIns().getLocalUser();
        if (mLocalMeetingView.checkUserInfo(localUser.userId)) {
            mLocalMeetingView.switchRtcVisible(true);
            subscribeVideo(mLocalMeetingView);
        } else if (mSmallMeetingView.checkUserInfo(localUser.userId)) {
            mSmallMeetingView.switchRtcVisible(true);
            subscribeVideo(mSmallMeetingView);
        }
    }

    /***************************Start/Stop LocalVideo*******************************************/
    /******************************Whiteboard Start*********************************************/
    @Override
    public void onWhiteboardStart() {
        PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
        if (annotation != null) {
            removeAnnotationView();
            AnnotationHelper.getIns().setAnnotationEnable(false);
            if (AnnotationHelper.getIns().annotationHost()) {
                annotation.stopAnnotation();
                AnnotationHelper.getIns().setAnnotation(null);
            }
        }
    }

    /******************************Whiteboard Start*********************************************/
    /******************************Pin Video Start*********************************************/

    private boolean onPinVideoItemClick(long userId) {
        Toast.makeText(getActivity(), R.string.msg_pin_video_tips, Toast.LENGTH_LONG).show();
        return refreshVideoView(PIN_VIDEO_MODE, userId);
    }

    /******************************Pin Video Stop*********************************************/
    /******************************Annotation Start*********************************************/

    @Override
    public void onClickAnnotationStop() {
        AnnotationHelper.getIns().setAnnotationEnable(false);
        removeAnnotationView();
    }

    @Override
    public void onVideoAnnotationStart(long userId,int streamId) {
        Log.i(TAG, "onVideoAnnotationStart");
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
        if(user !=null && annotation != null){
            AnnotationHelper.getIns().setAnnotationHost(false);
            addAnnotationView();
            annotation.startAnnotation(mRtcWbView);
            refreshVideoView(ANNOTATION_MODE, userId);
            Toast.makeText(getActivity(), getString(R.string.annotation_user_start, user.userName), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onVideoAnnotationStop(long userId) {
        Log.i(TAG, "onVideoAnnotationStop");
        AnnotationHelper.getIns().setAnnotationEnable(false);
        AnnotationHelper.getIns().setAnnotationUserId(0L);
        PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
        if (annotation != null) {
            annotation.stopAnnotation();
            AnnotationHelper.getIns().setAnnotation(null);
        }
        mHandler.post(() -> {
            removeAnnotationView();
            UserInfo user = UserManager.getIns().getRemoteUser(userId);
            if (user != null) {
                Toast.makeText(getActivity(), getString(R.string.annotation_user_stop, user.userName), Toast.LENGTH_SHORT).show();
            }
            refreshVideoView(ALL_MODE, 0L);
        });
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        Log.i(TAG, "onShareAnnotationStart userId = " + userId);
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
        if(user !=null && annotation != null){
            AnnotationHelper.getIns().setAnnotationHost(false);
            mLocalMeetingView.setScalingRatio(IVideoRender.ScalingRatio.SCALE_RATIO_FIT);
            addAnnotationView();
            annotation.startAnnotation(mRtcWbView);
            Toast.makeText(getActivity(), getString(R.string.annotation_user_start, user.userName), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShareAnnotationStop(long userId) {
        Log.i(TAG, "onShareAnnotationStop");
        AnnotationHelper.getIns().setAnnotationEnable(false);
        AnnotationHelper.getIns().setAnnotationUserId(0L);
        PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
        if (annotation != null) {
            annotation.stopAnnotation();
            AnnotationHelper.getIns().setAnnotation(null);
        }
        mHandler.post(() -> {
            removeAnnotationView();
            UserInfo user = UserManager.getIns().getRemoteUser(userId);
            if (user != null) {
                Toast.makeText(getActivity(), getString(R.string.annotation_user_stop, user.userName), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAnnotationToolsClick() {
        if(mRtcWbView != null) mRtcWbView.setPassThrough(true);
    }

    private void addAnnotationView() {
        mRtcWbView = new RtcWbView(getContext());
        mRtcWbView.setVisibility(View.VISIBLE);
        mLocalMeetingView.addRtcWbView(mRtcWbView);
    }

    private void removeAnnotationView() {
        if(mRtcWbView != null) mRtcWbView.setVisibility(View.GONE);
    }

    /******************************Annotation Stop*********************************************/

    @Override
    protected int getAudioMutedResourceId(boolean isLarge) {
        return isLarge ? R.drawable.svg_icon_audio_mute : R.drawable.svg_icon_small_audio_mute;
    }

    @Override
    protected int getAudioNormalResourceId(boolean isLarge) {
        return isLarge ? R.drawable.svg_icon_audio_normal : R.drawable.svg_icon_small_audio_normal;
    }

    @Override
    protected int getAudioMutePSTNResourceId(boolean isLarge) {
        return isLarge ? R.drawable.svg_icon_audio_pstn_mute : R.drawable.svg_icon_small_audio_pstn_mute;
    }

    @Override
    protected int getAudioNormalPSTNResourceId(boolean isLarge) {
        return isLarge ? R.drawable.svg_icon_audio_pstn_normal : R.drawable.svg_icon_small_audio_pstn_normal;
    }

    @Override
    protected int getSignalLowResourceId(boolean isLarge) {
        return isLarge ? R.drawable.svg_icon_signal_low : R.drawable.svg_icon_small_signal_low;
    }

    @Override
    protected int getSignalPoorResourceId(boolean isLarge) {
        return isLarge ? R.drawable.svg_icon_signal_poor : R.drawable.svg_icon_small_signal_poor;
    }

    @Override
    protected int getSignalGoodResourceId(boolean isLarge) {
        return isLarge ? R.drawable.svg_icon_signal_good : R.drawable.svg_icon_small_signal_good;
    }

    class VideoTouchListener extends OnPanoTouchListener {

        public VideoTouchListener(Context c) {
            super(c);
        }

        @Override
        public void onSingleTap() {
            if (mViewModel != null) {
                mViewModel.onShowHideControlPanel();
            }
        }

        @Override
        public void onSwipeLeft() {
            if(mViewModel != null && mViewModel.getShowUserCount() <= 2){
                return ;
            }
            if (AnnotationHelper.getIns().annotationEnable()) {
                return;
            }
            NavDestination currentDestination = mNavController.getCurrentDestination();
            if(currentDestination == null){
                return ;
            }
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_GRID_PAGE_NUM, 1);
            if (currentDestination.getId() == R.id.SpeakerFragment) {
                mNavController.navigate(R.id.action_SpeakerFragment_to_GalleryFragment, bundle);
            }
        }

        @Override
        public void onDoubleTap() {
            if (mPinVideoSuccess) {
                refreshVideoView(ALL_MODE, 0L);
                Toast.makeText(getActivity(), R.string.msg_exit_pin_video_tips, Toast.LENGTH_LONG).show();
                mPinVideoSuccess = false;

                Bundle bundle = getArguments();
                if (bundle != null) {
                    bundle.putBoolean(KEY_USE_PIN_VIDEO, false);
                }
            }
        }
    }
}
