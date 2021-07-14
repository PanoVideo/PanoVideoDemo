package video.pano.panocall.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import video.pano.panocall.R;
import video.pano.panocall.listener.AnnotationListener;
import video.pano.panocall.listener.OnPanoTouchListener;
import video.pano.panocall.listener.PanoAnnotationHandler;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.model.UserViewInfo;
import video.pano.panocall.utils.AnnotationHelper;

import static video.pano.panocall.fragment.AnnotationControlPanelFragment.DEFAULT_COLOR;
import static video.pano.panocall.fragment.AnnotationControlPanelFragment.DEFAULT_WIDTH;
import static video.pano.panocall.info.Constant.COLORS;
import static video.pano.panocall.info.Constant.KEY_ANNOTATION_COLOR;
import static video.pano.panocall.info.Constant.KEY_ANNOTATION_COLOR_ID;
import static video.pano.panocall.info.Constant.KEY_ANNOTATION_INTENSITY;
import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_USE_PIN_VIDEO;


public class FloatFragment extends CallFragment implements PanoAnnotationHandler {

    private static final String TAG = "FloatFragment";

    private static final int DELAY_TIME = 1000;

    private long mAnnotationUser;
    private int mCurrentColor;
    private int mCurrentWidth;
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
            Bundle savedInstanceState
    ) {
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mCurrentColor = prefs.getInt(KEY_ANNOTATION_COLOR, DEFAULT_COLOR);
        mCurrentWidth = prefs.getInt(KEY_ANNOTATION_INTENSITY, DEFAULT_WIDTH);

        getExtra();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
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
        int localIndex = remoteUsers.size() > 0 ? 1 : 0;
        initUserVideoView(localIndex,true);
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
        if (mUserViewArray[0].isFree || mUserViewArray[0].userId == localUser.userId) {
            // large view is free or used by local user, then make this user to large view
            // 如果大图被本地用户占用，则将本地用户移到小图，如果没有空闲的小图，则不显示本地用户视频
            if (mUserViewArray[0].userId == localUser.userId) {
                // move local user to last view
                mLocalView = null;
                if (mUserViewArray[mUserViewCount - 1].isFree) {
                    mLocalView = mUserViewArray[mUserViewCount - 1].rtcView;
                    mUserViewArray[mUserViewCount - 1].setVisible(true);
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
        if (mAnnotationUser == userId) {
            PanoAnnotation annotation = AnnotationHelper.getInstance().getAnnotation();
            if (annotation != null) {
                annotation.stopAnnotation();
                AnnotationHelper.getInstance().setAnnotation(null);
            }
        }
    }

    @Override
    public void onClickPencil(boolean checked) {
        PanoAnnotation annotation = AnnotationHelper.getInstance().getAnnotation();
        if (checked) {
            annotation.setToolType(Constants.WBToolType.Path);
            annotation.setLineWidth(mCurrentWidth);
            annotation.setColor(mCurrentColor);
        } else {
            annotation.setToolType(Constants.WBToolType.None);
        }
        mRtcWbView.setPassThrough(!checked);
    }

    @Override
    public void onClickArrow(boolean checked) {
        PanoAnnotation annotation = AnnotationHelper.getInstance().getAnnotation();
        if (checked) {
            annotation.setToolType(Constants.WBToolType.Arrow);
            annotation.setLineWidth(mCurrentWidth);
            annotation.setColor(mCurrentColor);
        } else {
            annotation.setToolType(Constants.WBToolType.None);
        }
        mRtcWbView.setPassThrough(!checked);
    }

    @Override
    public void onClickEraser(boolean checked) {
        PanoAnnotation annotation = AnnotationHelper.getInstance().getAnnotation();
        if (checked) {
            annotation.setToolType(Constants.WBToolType.Eraser);
            annotation.setLineWidth(mCurrentWidth);
            annotation.setColor(mCurrentColor);
        } else {
            annotation.setToolType(Constants.WBToolType.None);
        }
        mRtcWbView.setPassThrough(!checked);
    }

    @Override
    public void onProgressChange(int progress) {
        if (progress < 0) {
            return;
        }
        mCurrentWidth = progress;
        PanoAnnotation annotation = AnnotationHelper.getInstance().getAnnotation();
        annotation.setLineWidth(mCurrentWidth);
        annotation.setToolType(Constants.WBToolType.Path);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_ANNOTATION_INTENSITY, progress);
        editor.apply();
    }

    @Override
    public void onCheckedColor(int index, int colorId) {
        if (index < 0) {
            return;
        }
        mCurrentColor = COLORS[index];
        PanoAnnotation annotation = AnnotationHelper.getInstance().getAnnotation();
        annotation.setColor(mCurrentColor);
        annotation.setToolType(Constants.WBToolType.Path);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_ANNOTATION_COLOR, mCurrentColor);
        editor.putInt(KEY_ANNOTATION_COLOR_ID, colorId);
        editor.apply();
    }

    /***************************Indication Start*******************************************/
    @Override
    public void onUserLeaveIndication(UserInfo user, Constants.UserLeaveReason reason) {
        //Local View refresh
        if (mUserViewArray[0].userId == user.userId) {
            switchUserViewInfoToNext();
        //Remote View refresh
        } else if (mUserViewArray[1].userId == user.userId) {
            clearViewRender(mUserViewArray[1]);
            refreshRemoteView();
        }
        onUserLeave(user.userId);
    }

    /******************************Screen Start*********************************************/
    @Override
    void setupUserScreenView(UserInfo user) {
        if (AnnotationHelper.getInstance().annotationEnable()) {
            return;
        }
        UserViewInfo largeInfo = mUserViewArray[0];
        UserViewInfo smallInfo = mUserViewArray[1];
        clearViewRender(largeInfo);
        clearRemoteVideoRender(largeInfo.userId);
        clearViewRender(smallInfo);

        //LocalView refresh
        subscribeUserScreen(user.userId, user.userName, largeInfo);
        updateUserAudioState(0);

        //Remote View refresh
        refreshRemoteView();
    }

    @Override
    public void onUserScreenStop(UserInfo user) {
        switchUserViewInfoToNext();
    }

    /******************************Screen Start*********************************************/


    /******************************Annotation Start*********************************************/
    @Override
    public void onAnnotationStop() {
        AnnotationHelper.getInstance().setAnnotationEnable(false);
        removeAnnotationView();
        PanoAnnotation annotation = AnnotationHelper.getInstance().getAnnotation();
        if (AnnotationHelper.getInstance().annotationHost() && annotation != null) {
            switchUserViewInfoToNext();
            annotation.stopAnnotation();
        }
        if (mAnnotationListener != null) {
            mAnnotationListener.onAnnotationClose();
        }
    }

    @Override
    public void onAnnotationStart() {
        PanoAnnotation annotation = AnnotationHelper.getInstance().getAnnotation();
        Log.i(TAG, "startAnnotation annotation " + annotation);
        if (annotation == null) {
            addAnnotationView();
            onAnnotationBegin(mViewModel.getLocalUserId());

            annotation = mViewModel.rtcEngine().getAnnotationMgr().getVideoAnnotation(mViewModel.getLocalUserId(), 0);
            annotation.startAnnotation(mRtcWbView);
            AnnotationHelper.getInstance().setAnnotation(annotation);
            AnnotationHelper.getInstance().setAnnotationHost(true);
        } else {
            Log.i(TAG, "remote annotation: " + annotation.getClass().getName());
            AnnotationHelper.getInstance().setAnnotationHost(false);
            mRtcWbView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onVideoAnnotationStart(long userId, int streamId) {
        Log.i(TAG, "onVideoAnnotationStart");
        AnnotationHelper.getInstance().setAnnotationHost(false);
        mHandler.postDelayed(() -> {
            UserInfo user = mViewModel.getUserManager().getRemoteUser(userId);
            if (user == null) {
                return;
            }
            mAnnotationUser = userId;
            addAnnotationView();

            AnnotationHelper.getInstance().getAnnotation().startAnnotation(mRtcWbView);
            Log.i(TAG, "switchToLargeView " + userId);

            onAnnotationBegin(userId);
            Toast.makeText(getActivity(), getString(R.string.annotation_user_start, user.userName), Toast.LENGTH_SHORT).show();
            if (mAnnotationListener != null) {
                mAnnotationListener.onAnnotationStart();
            }
        }, DELAY_TIME);
    }

    @Override
    public void onVideoAnnotationStop(long userId, int streamId) {
        Log.i(TAG, "onVideoAnnotationStop");
        AnnotationHelper.getInstance().setAnnotationEnable(false);
        mHandler.post(() -> {
            mAnnotationUser = 0L;

            removeAnnotationView();

            PanoAnnotation annotation = AnnotationHelper.getInstance().getAnnotation();
            if (annotation != null) {
                annotation.stopAnnotation();
                AnnotationHelper.getInstance().setAnnotation(null);
            }
            UserInfo user = mViewModel.getUserManager().getRemoteUser(userId);
            if (user != null) {
                Toast.makeText(getActivity(), getString(R.string.annotation_user_stop, user.userName), Toast.LENGTH_SHORT).show();
            }
            switchUserViewInfoToNext();
            if (mAnnotationListener != null) {
                mAnnotationListener.onAnnotationClose();
            }
        });
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        AnnotationHelper.getInstance().setAnnotationHost(false);
        Log.i(TAG, "onShareAnnotationStart userId = " + userId);
        mHandler.postDelayed(() -> {
            UserInfo user = mViewModel.getUserManager().getRemoteUser(userId);
            if (user == null) {
                return;
            }
            mAnnotationUser = userId;
            if (mUserViewArray != null && mUserViewArray.length > 0) {
                mUserViewArray[0].rtcView.setScalingRatio(IVideoRender.ScalingRatio.SCALE_RATIO_FIT);
            }
            addAnnotationView();

            AnnotationHelper.getInstance().getAnnotation().startAnnotation(mRtcWbView);
            Toast.makeText(getActivity(), getString(R.string.annotation_user_start, user.userName), Toast.LENGTH_SHORT).show();
            if (mAnnotationListener != null) {
                mAnnotationListener.onAnnotationStart();
            }
        }, DELAY_TIME);
    }

    @Override
    public void onShareAnnotationStop(long userId) {
        Log.i(TAG, "onShareAnnotationStop");
        AnnotationHelper.getInstance().setAnnotationEnable(false);
        mHandler.post(() -> {
            mAnnotationUser = 0L;

            removeAnnotationView();

            PanoAnnotation annotation = AnnotationHelper.getInstance().getAnnotation();
            if (annotation != null) {
                annotation.stopAnnotation();
                AnnotationHelper.getInstance().setAnnotation(null);
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

    /******************************Annotation Stop*********************************************/

    /******************************Pin Video Start*********************************************/

    private boolean onPinVideoItemClick(long userId) {
        if (userId == mUserViewArray[0].userId) {
            return false;
        }
        if (mUserViewArray[1].isFree) {
            return false;
        }
        Toast.makeText(getActivity(),R.string.msg_pin_video_tips,Toast.LENGTH_LONG).show();

        UserViewInfo largeInfo = mUserViewArray[0];
        UserViewInfo smallInfo = mUserViewArray[1];

        UserInfo localUser = mViewModel.getUserManager().getLocalUser();

        clearViewRender(largeInfo);
        clearViewRender(smallInfo);

        //refresh Local View
        if (userId == localUser.userId) {
            subscribeLocalVideo(largeInfo, localUser, false);
        } else {
            UserInfo remoteUser = mViewModel.getUserManager().getRemoteUser(userId);
            subscribeUserVideo(remoteUser.userId, remoteUser.userName, largeInfo, getProfileForVideoView(0));
        }
        updateUserAudioState(0);

        //refresh Remote View
        if (userId == localUser.userId) {
            if (mViewModel.getUserManager().isRemoteEmpty()) {
                smallInfo.isScreen = false;
                smallInfo.isSubscribed = false;
                smallInfo.isFree = true;
                smallInfo.setVisible(false);
            } else {
                UserInfo remoteUser = mViewModel.getUserManager().getRemoteUsers().valueAt(0);
                subscribeUserVideo(remoteUser.userId, remoteUser.userName, smallInfo, getProfileForVideoView(1));
            }
        } else {
            subscribeLocalVideo(smallInfo, localUser, false);
        }

        updateUserAudioState(1);
        return true ;
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

    private void refreshLocalView() {
        boolean isScreenEmpty = mViewModel.getUserManager().isScreenEmpty();
        boolean isRemoteEmpty = mViewModel.getUserManager().isRemoteEmpty();
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();

        UserViewInfo largeInfo = mUserViewArray[0];

        if (!isScreenEmpty) {
            UserInfo screenUser = mViewModel.getUserManager().getScreenUsers().valueAt(0);
            subscribeUserScreen(screenUser.userId, screenUser.userName, largeInfo);
        } else if (!isRemoteEmpty) {
            UserInfo remoteUser = mViewModel.getUserManager().getRemoteUsers().valueAt(0);
            subscribeUserVideo(remoteUser.userId, remoteUser.userName, largeInfo, getProfileForVideoView(0));
        } else {
            subscribeLocalVideo(largeInfo, localUser, true);
        }
        updateUserAudioState(0);
    }

    private void refreshRemoteView() {
        boolean isRemoteEmpty = mViewModel.getUserManager().isRemoteEmpty();
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();

        UserViewInfo smallInfo = mUserViewArray[1];

        //Remote View refresh
       if (isRemoteEmpty) {
            smallInfo.isScreen = false;
            smallInfo.isSubscribed = false;
            smallInfo.isFree = true;
            smallInfo.setVisible(false);
        } else {
            subscribeLocalVideo(smallInfo, localUser, false);
        }
        updateUserAudioState(1);
    }

    private void switchUserViewInfoToNext() {
        clearViewRender(mUserViewArray[0]);
        clearViewRender(mUserViewArray[1]);
        refreshLocalView();
        refreshRemoteView();
    }

    private void subscribeLocalVideo(UserViewInfo userViewInfo, UserInfo userInfo, boolean isMirror) {
        userViewInfo.setUser(userInfo.userId, userInfo.userName,mViewModel.getUserManager().isMySelf(userInfo.userId));
        userViewInfo.setVisible(true);
        userViewInfo.isFree = false;
        userViewInfo.isScreen = false;
        userViewInfo.isSubscribed = false;
        updateLocalVideoRender(userViewInfo.rtcView, isMirror);
    }

    private void onAnnotationBegin(long userId) {
        if (userId == mUserViewArray[0].userId) {
            return;
        }
        if (mUserViewArray[1].isFree) {
            return;
        }
        boolean isRemoteEmpty = mViewModel.getUserManager().isRemoteEmpty();

        UserViewInfo largeInfo = mUserViewArray[0];
        UserViewInfo smallInfo = mUserViewArray[1];

        UserInfo localUser = mViewModel.getUserManager().getLocalUser();

        clearViewRender(largeInfo);
        clearViewRender(smallInfo);

        //refresh Local View
        if (userId == localUser.userId) {
            subscribeLocalVideo(largeInfo, localUser, false);
        } else {
            UserInfo remoteUser = mViewModel.getUserManager().getRemoteUser(userId);
            subscribeUserVideo(remoteUser.userId, remoteUser.userName, largeInfo, getProfileForVideoView(0));
        }
        updateUserAudioState(0);

        //refresh Remote View
        if (userId == localUser.userId) {
            if (isRemoteEmpty) {
                smallInfo.isScreen = false;
                smallInfo.isSubscribed = false;
                smallInfo.isFree = true;
                smallInfo.setVisible(false);
            } else {
                UserInfo remoteUser = mViewModel.getUserManager().getRemoteUsers().valueAt(0);
                subscribeUserVideo(remoteUser.userId, remoteUser.userName, smallInfo, getProfileForVideoView(1));
            }
        } else {
            subscribeLocalVideo(smallInfo, localUser, false);
        }

        updateUserAudioState(1);
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


    /****************************  RefreshView  End******************************************/

    int getAudioMutedResourceId(int index) {
        return index == 0 ? R.drawable.svg_icon_audio_mute : R.drawable.svg_icon_small_audio_mute;
    }

    int getAudioNormalResourceId(int index) {
        return index == 0 ? R.drawable.svg_icon_audio_normal : R.drawable.svg_icon_small_audio_normal;
    }

    int getSignalLowResourceId(int index){
        return index == 0 ? R.drawable.svg_icon_signal_low : R.drawable.svg_icon_small_signal_low;
    }

    int getSignalPoorResourceId(int index){
        return index == 0 ? R.drawable.svg_icon_signal_poor : R.drawable.svg_icon_small_signal_poor;
    }

    int getSignalGoodResourceId(int index){
        return index == 0 ? R.drawable.svg_icon_signal_good : R.drawable.svg_icon_small_signal_good;
    }

    private void toGridView() {
        if (mViewModel.getUserManager().getRemoteUsers().size() < 2) {
            return;
        }
        NavHostFragment.findNavController(FloatFragment.this)
                .navigate(R.id.action_FloatFragment_to_GridFragment);
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
            toGridView();
        }

        @Override
        public void onDoubleTap() {
            if(mPinVideoSuccess){
                switchUserViewInfoToNext();
                Toast.makeText(getActivity(),R.string.msg_exit_pin_video_tips,Toast.LENGTH_LONG).show();
            }
        }
    }
}
