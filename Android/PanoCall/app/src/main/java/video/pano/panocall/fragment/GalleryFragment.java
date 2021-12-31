package video.pano.panocall.fragment;

import static video.pano.panocall.info.Constant.KEY_GRID_PAGE_NUM;
import static video.pano.panocall.info.Constant.KEY_LOCAL_ANNOTATION_START;
import static video.pano.panocall.info.Constant.KEY_SHARE_ANNOTATION_START;
import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_USE_PIN_VIDEO;
import static video.pano.panocall.info.Constant.KEY_VIDEO_ANNOTATION_START;
import static video.pano.panocall.rtc.MeetingViewFactory.MODE_REMOTE_SCREEN;
import static video.pano.panocall.rtc.MeetingViewFactory.MODE_REMOTE_VIDEO;
import static video.pano.panocall.rtc.MeetingViewFactory.TYPE_MIDDLE_VIEW;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavDestination;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.model.RtcAudioLevel;

import java.util.ArrayList;
import java.util.List;

import video.pano.panocall.R;
import video.pano.panocall.info.UserManager;
import video.pano.panocall.listener.OnPanoTouchListener;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.rtc.MeetingViewFactory;
import video.pano.panocall.rtc.MeetingViewInfo;
import video.pano.panocall.utils.AnnotationHelper;

public class GalleryFragment extends MeetingFragment {

    private final static int VOLUME_LIMIT = 500;
    private final static int VIEW_COUNT = 4;
    private int mCurrentPageNum;

    private List<MeetingViewInfo> mMeetingViewList = new ArrayList<>();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getExtra();
        initViews(view);
        setupUserVideoView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (MeetingViewInfo viewInfo : mMeetingViewList) {
            getLifecycle().removeObserver(viewInfo);
        }
    }

    @Override
    public void onCheckState() {
        refreshVideoView();
    }

    private void getExtra() {
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(KEY_GRID_PAGE_NUM)) {
            mCurrentPageNum = bundle.getInt(KEY_GRID_PAGE_NUM);
        }
    }

    private void initViews(@NonNull View view) {
        List<ViewGroup> containerList = new ArrayList<>();

        containerList.add(view.findViewById(R.id.cl_layout_lefttop));
        containerList.add(view.findViewById(R.id.cl_layout_righttop));
        containerList.add(view.findViewById(R.id.cl_layout_leftbottom));
        containerList.add(view.findViewById(R.id.cl_layout_rightbottom));

        view.setOnTouchListener(new VideoItemTouchListener(getActivity(), 0L));

        mMeetingViewList.clear();
        for (ViewGroup container : containerList) {
            container.removeAllViews();
            MeetingViewInfo viewInfo = MeetingViewFactory.createMeetingViewInfo(getContext(),
                    TYPE_MIDDLE_VIEW);
            container.addView(viewInfo.getInfoView());
            viewInfo.setParentView(container);
            getLifecycle().addObserver(viewInfo);
            mMeetingViewList.add(viewInfo);
        }
        mLocalMeetingView = mMeetingViewList.get(0);

    }

    private void setupUserVideoView() {
        // 初始化宫格视图数组，目前支持四宫格
        refreshVideoView();

        // 设置长按小图时将此小图的用户和大图用户交换视图
        for (MeetingViewInfo viewInfo : mMeetingViewList) {
            viewInfo.setOnTouchListener(new VideoItemTouchListener(getActivity(), viewInfo.getUserId()));
        }
    }

    private void refreshVideoView() {
        UserInfo localUser = UserManager.getIns().getLocalUser();

        if (mViewModel.mIsRoomJoined || localUser.isVideoStarted()) {
            subscribeLocalVideo(mLocalMeetingView, localUser);
            updateUserAudioState(localUser);

            int rIndex = (mCurrentPageNum - 1) * 3;
            int sIndex = 0;
            int viewIndex = 1;
            MeetingViewInfo viewInfo;
            UserInfo userInfo;

            while (viewIndex < VIEW_COUNT) {
                viewInfo = mMeetingViewList.get(viewIndex);

                if (!UserManager.getIns().isRemoteEmpty()) {
                    if (rIndex < UserManager.getIns().getRemoteSize()) {
                        userInfo = UserManager.getIns().getRemoteUsers().valueAt(rIndex++);
                        buildViewInfo(viewInfo, false, userInfo, MODE_REMOTE_VIDEO).subscribeVideo();
                        viewInfo.switchRtcVisible(userInfo.isVideoStarted());
                        updateUserAudioState(userInfo);
                        viewIndex++;
                        continue;
                    }
                }
                if (!UserManager.getIns().isScreenEmpty()) {
                    if (sIndex < UserManager.getIns().getScreenSize()) {
                        userInfo = UserManager.getIns().getScreenUsers().get(sIndex++);
                        if (userInfo.userId != mViewModel.mCurrentScreenUserId) {
                            buildViewInfo(viewInfo, false, userInfo, MODE_REMOTE_SCREEN).subscribeVideo();
                            updateUserAudioState(userInfo);
                            viewIndex++;
                        }
                        continue;
                    }
                }
                viewInfo.setInfoViewVisible(false);
                viewInfo.release();
                viewIndex++;
            }
        } else {
            // 启动视频预览，并且显示到大图
            startPreviewLocalUser();
        }
    }

    @Override
    protected void unSubscribeAllMeetingView() {
        for (MeetingViewInfo viewInfo : mMeetingViewList) {
            unSubscribeVideo(viewInfo);
            viewInfo.setInfoViewVisible(false);
            viewInfo.release();
        }
    }

    /***************************Indication Start***********************************/
    @Override
    public void onUserJoinIndication(UserInfo user) {
        for (MeetingViewInfo viewInfo : mMeetingViewList) {
            if (viewInfo.isEmptyUserInfo()) {
                switchViewInfoData(viewInfo, user);
                break;
            }
        }
    }

    @Override
    public void onUserLeaveIndication(UserInfo user) {
        if (UserManager.getIns().getRemoteSize() < 2) {
            navigate2Speaker(0L,null,false,0);
        } else {
            boolean refresh = false;
            for (MeetingViewInfo viewInfo : mMeetingViewList) {
                if (viewInfo.checkUserInfo(user)) {
                    refresh = true;
                }
            }

            if (refresh) {
                for (MeetingViewInfo viewInfo : mMeetingViewList) {
                    unSubscribeVideo(viewInfo);
                    viewInfo.setInfoViewVisible(false);
                    viewInfo.release();
                }
                refreshVideoView();
            }
        }
    }

    @Override
    public void onUserVideoStart(UserInfo user) {
        boolean find = false;
        int index = -1;
        for (int i = 0; i < VIEW_COUNT; i++) {
            MeetingViewInfo viewInfo = mMeetingViewList.get(i);
            if (viewInfo.checkUserInfo(user)) {
                viewInfo = buildViewInfo(viewInfo, false, user, MODE_REMOTE_VIDEO);
                subscribeVideo(viewInfo);
                find = true;
                break;
            }
            if (viewInfo.isEmptyUserInfo()) {
                index = i;
            }
        }
        if (!find && index >= 0) {
            MeetingViewInfo viewInfo = refreshViewInfo(mMeetingViewList.get(index), user);
            subscribeVideo(viewInfo);
        }

    }

    @Override
    public void onUserVideoStop(UserInfo user) {
        // 取消订阅此用户视频
        for (int i = 0; i < VIEW_COUNT; i++) {
            MeetingViewInfo viewInfo = mMeetingViewList.get(i);
            if (viewInfo.checkUserInfo(user)) {
                unSubscribeVideo(viewInfo);
                break;
            }
        }
    }
    /******************************Indication End****************************************/
    /******************************Screen Start*********************************************/
    @Override
    public void onUserScreenStart(long userId) {
        super.onUserScreenStart(userId);
        if (AnnotationHelper.getIns().annotationEnable()) {
            return;
        }
        navigate2Speaker(userId, null, false,0);
    }

    @Override
    public void onUserScreenStop(UserInfo user) {
        super.onUserScreenStop(user);
        if (UserManager.getIns().getRemoteSize() < 2) {
            navigate2Speaker(0L, null, false, 0);
            return ;
        }
        refreshVideoView();
        int emptyCount = 0;
        for (MeetingViewInfo viewInfo : mMeetingViewList) {
            if (viewInfo.isEmptyUserInfo()) {
                emptyCount++;
            }
        }
        if (emptyCount > VIEW_COUNT - 2) {
            swipeRight();
        }

    }
    /******************************Screen Start*********************************************/
    /***************************ASL Start***********************************************************/
    @Override
    public void onNetworkQuality(long userId, Constants.QualityRating quality) {
        for (MeetingViewInfo viewInfo : mMeetingViewList) {
            if (viewInfo.checkUserInfo(userId)) {
                if (quality == Constants.QualityRating.Excellent || quality == Constants.QualityRating.Good) {
                    viewInfo.updateSignalImg(getSignalGoodResourceId(false));
                } else if (quality == Constants.QualityRating.Poor) {
                    viewInfo.updateSignalImg(getSignalPoorResourceId(false));
                } else {
                    viewInfo.updateSignalImg(getSignalLowResourceId(false));
                }
            }
        }
    }

    /***************************ASL End***********************************************************/

    /***************************Start/Stop LocalVideo*******************************************/

    @Override
    public void stopLocalVideo() {
        unSubscribeVideo(mLocalMeetingView);
    }

    @Override
    public void startLocalVideo() {
        mLocalMeetingView.switchRtcVisible(true);
        subscribeVideo(mLocalMeetingView);
    }

    /***************************Start/Stop LocalVideo*******************************************/
    /******************************Annotation Start*********************************************/
    @Override
    public void onVideoAnnotationStart(long userId, int streamIds) {
        if(!isResumed()) {
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_USER_ID, userId);
            bundle.putBoolean(KEY_VIDEO_ANNOTATION_START, true);

            mViewModel.mAnnotationExtra = bundle;
            mViewModel.mAnnotationActionId = R.id.action_GalleryFragment_to_SpeakerFragment;
            mViewModel.mAnnotationStart = true;
        }else{
            navigate2Speaker(userId, KEY_VIDEO_ANNOTATION_START, true, 0);
        }
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        if(!isResumed()) {
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_USER_ID, userId);
            bundle.putBoolean(KEY_SHARE_ANNOTATION_START, true);

            mViewModel.mAnnotationExtra = bundle;
            mViewModel.mAnnotationActionId = R.id.action_GalleryFragment_to_SpeakerFragment;
            mViewModel.mAnnotationStart = true;
        }else{
            navigate2Speaker(userId, KEY_SHARE_ANNOTATION_START, true, 0);
        }
    }

    /******************************Annotation End*********************************************/

    @Override
    public void updateUserAudioState(UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        for (MeetingViewInfo viewInfo : mMeetingViewList) {
            if (viewInfo.checkUserInfo(userInfo)) {
                int largeAudioResource = -1;
                if (userInfo.isPSTNAudioType()) {
                    largeAudioResource = userInfo.isAudioMuted() ? getAudioMutePSTNResourceId(false) : getAudioNormalPSTNResourceId(false);
                } else {
                    largeAudioResource = userInfo.isAudioMuted() ? getAudioMutedResourceId(false) : getAudioNormalResourceId(false);
                }
                viewInfo.updateAudioImg(largeAudioResource);
            }
        }
    }

    private void navigate2Speaker(long userId, String extraKey, boolean extraValue,int pageNum) {
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_USER_ID, userId);
        bundle.putInt(KEY_GRID_PAGE_NUM, pageNum);
        if (!TextUtils.isEmpty(extraKey)) {
            bundle.putBoolean(extraKey, extraValue);
        }
        NavDestination currentDestination = mNavController.getCurrentDestination();
        if(currentDestination == null){
            return ;
        }
        if (currentDestination.getId() == R.id.GalleryFragment) {
            mNavController.navigate(R.id.action_GalleryFragment_to_SpeakerFragment, bundle);
        }
    }

    private void navigate2Gallery(int pageNum){
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_GRID_PAGE_NUM, pageNum);
        NavDestination currentDestination = mNavController.getCurrentDestination();
        if(currentDestination == null){
            return ;
        }
        if (currentDestination.getId() == R.id.GalleryFragment) {
            mNavController.navigate(R.id.action_GalleryFragment_to_GalleryFragment, bundle);
        }
    }

    private void swipeRight() {
        NavDestination currentDestination = mNavController.getCurrentDestination();
        if(currentDestination == null){
            return ;
        }
        if (currentDestination.getId() != R.id.GalleryFragment) {
            return ;
        }
        if (mCurrentPageNum > 0) mCurrentPageNum--;

        if (mCurrentPageNum == 0) {
            navigate2Speaker(0L,null,false,mCurrentPageNum);
        } else {
            navigate2Gallery(mCurrentPageNum);
        }
    }

    class VideoItemTouchListener extends OnPanoTouchListener {

        private final long mUserId;

        public VideoItemTouchListener(Context c, long userId) {
            super(c);
            mUserId = userId;
        }

        @Override
        public void onSingleTap() {
            if (mViewModel != null) {
                mViewModel.onShowHideControlPanel();
            }
        }

        @Override
        public void onSwipeLeft() {
            int userCount = mViewModel.getShowUserCount();

            int showCount = mCurrentPageNum * 4;
            if (userCount - showCount <= 0) {
                return;
            }
            int nextPageNum = mCurrentPageNum + 1;

            navigate2Gallery(nextPageNum);
        }

        @Override
        public void onSwipeRight() {
            swipeRight();
        }

        @Override
        public void onDoubleTap() {
            if (mUserId == UserManager.getIns().getLocalUserId() || mUserId == 0L) {
                return;
            }
            navigate2Speaker(mUserId, KEY_USE_PIN_VIDEO, true,0);
        }
    }
}
