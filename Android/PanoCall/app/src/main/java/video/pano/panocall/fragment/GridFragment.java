package video.pano.panocall.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.pano.rtc.api.model.RtcAudioLevel;

import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.listener.OnPanoTouchListener;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.utils.AnnotationHelper;
import video.pano.panocall.utils.Utils;

import static video.pano.panocall.info.Constant.KEY_GRID_LAST_POS;
import static video.pano.panocall.info.Constant.KEY_GRID_POS;
import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_USE_PIN_VIDEO;
import static video.pano.panocall.info.Constant.KEY_VIDEO_ANNOTATION_START;

public class GridFragment extends CallFragment {

    private final static int VOLUME_LIMIT = 500 ;
    private int mCurrentPos;
    private int mLastPos;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grid, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        getExtra();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (localUser.isVideoStarted()) {
            updateLocalVideoRender(mLocalView,mLocalViewIndex,mLocalViewMirror);
        }
    }

    @Override
    void setupUserViewArray() {
        // 初始化宫格视图数组，目前支持四宫格
        initUserViewArray(4);

        View view = getView();

        // 设置点击view时显示或隐藏控制按钮
        if(view != null){
            view.setOnTouchListener(new VideoTouchListener(getActivity()));

            // 配置左上角小图的视图参数, 此视图一般用于显示本地用户视频
            mUserViewArray[0].initView(
                    view.findViewById(R.id.medium_view_lefttop),
                    true,
                    view.findViewById(R.id.tv_medium_view_lefttop_user),
                    view.findViewById(R.id.img_medium_view_lefttop_audio),
                    view.findViewById(R.id.img_medium_view_lefttop_default_head),
                    view.findViewById(R.id.img_medium_view_lefttop_signal),
                    view.findViewById(R.id.cl_layout_lefttop));

            // 配置右上角视图参数
            mUserViewArray[1].initView(
                    view.findViewById(R.id.medium_view_righttop),
                    true,
                    view.findViewById(R.id.tv_medium_view_righttop_user),
                    view.findViewById(R.id.img_medium_view_righttop_audio),
                    view.findViewById(R.id.img_medium_view_righttop_default_head),
                    view.findViewById(R.id.img_medium_view_righttop_signal),
                    view.findViewById(R.id.cl_layout_righttop));

            // 配置左下角视图参数
            mUserViewArray[2].initView(
                    view.findViewById(R.id.medium_view_leftbottom),
                    true,
                    view.findViewById(R.id.tv_medium_view_leftbottom_user),
                    view.findViewById(R.id.img_medium_view_leftbottom_audio),
                    view.findViewById(R.id.img_medium_view_leftbottom_default_head),
                    view.findViewById(R.id.img_medium_view_leftbottom_signal),
                    view.findViewById(R.id.cl_layout_leftbottom));

            // 配置右下角视图参数
            mUserViewArray[3].initView(
                    view.findViewById(R.id.medium_view_rightbottom),
                    true,
                    view.findViewById(R.id.tv_medium_view_rightbottom_user),
                    view.findViewById(R.id.img_medium_view_rightbottom_audio),
                    view.findViewById(R.id.img_medium_view_rightbottom_default_head),
                    view.findViewById(R.id.img_medium_view_rightbottom_signal),
                    view.findViewById(R.id.cl_layout_rightbottom));
        }

        initUserVideoView();

        // 设置长按小图时将此小图的用户和大图用户交换视图
        for (int i=0; i<mUserViewCount; i++) {
            mUserViewArray[i].clView.setOnTouchListener(
                    new VideoItemTouchListener(getActivity(),mUserViewArray[i].userId));
        }
    }


    @Override
    void onUserLeave(long userId) {
        if (mViewModel.getUserManager().getRemoteUsers().size() < 2) {
            toFloatView();
        }else{
            clearUserViewArray();
            initUserVideoView();
        }
    }

    private void getExtra(){
        Bundle bundle = getArguments();
        if(bundle != null && bundle.containsKey(KEY_GRID_POS)){
            mCurrentPos = bundle.getInt(KEY_GRID_POS);
        }
        if(bundle != null){
            if(bundle.containsKey(KEY_GRID_POS)){
                mCurrentPos = bundle.getInt(KEY_GRID_POS);
            }
            if(bundle.containsKey(KEY_GRID_LAST_POS)){
                mLastPos = bundle.getInt(KEY_GRID_LAST_POS);
            }
        }
    }

    void initUserVideoView() {
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();

        if (mViewModel.mIsRoomJoined || localUser.isVideoStarted()) {
            //if (localUser.isVideoStarted())
            { // always show avatar even if video is not started
                mUserViewArray[0].isFree = !localUser.isVideoStarted();
                mUserViewArray[0].setUser(localUser.userId, localUser.userName,mViewModel.getUserManager().isMySelf(localUser.userId));
                if (localUser.isVideoStarted()) {
                    mUserViewArray[0].setVisible(true);
                } else {
                    mUserViewArray[0].setDefaultHeadVisible(true);
                    mUserViewArray[0].setUserVisible(true);
                }
                updateLocalVideoRender(mUserViewArray[0].rtcView, 0);
                updateUserAudioState(0);
            }

            int startPos = ( mCurrentPos - 1 ) * 3  ;
            int count = mUserViewCount - 1;
            LongSparseArray<UserInfo> remoteUsers = mViewModel.getUserManager().getRemoteUsers();
            int userCount = remoteUsers.size();
            for(int i= startPos; i<userCount && count>0; i++){
                UserInfo user = remoteUsers.valueAt(i);
                if (user.isVideoStarted()) {
                    setupUserVideoView(user);
                    --count;
                }else{
                    if (getIndexForNotFreeUser(user.userId) == -1) {
                        setupUserView(user);
                        --count;
                    }
                }
            }

            // 5. remove other non-video user
            if(count > 0){
                for(int k = 0 ; k < mUserViewCount ; k ++){
                    if (mUserViewArray[k].userId == 0L ) {
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
                updateUserAudioState(0);
                PanoApplication app = (PanoApplication) Utils.getApp();
                if (!app.mIsLocalVideoStarted) {
                    mViewModel.rtcEngine().startPreview(mViewModel.mLocalProfile, mViewModel.mIsFrontCamera);
                    app.mIsLocalVideoStarted = true;
                }
            }
        }
    }

    private void clearUserViewArray(){
        for (int i=0; i < mUserViewCount; i++) {
            mUserViewArray[i].isFree = true;
            mUserViewArray[i].isScreen = false;
            mUserViewArray[i].isSubscribed = false;
            mUserViewArray[i].userId = 0L;
            mUserViewArray[i].userName = null ;
        }
    }

    @Override
    public void onUserScreenStart(long userId) {
        if (AnnotationHelper.getIns().annotationEnable()) {
            return;
        }
       toFloatView();
    }

    /******************************Annotation Start*********************************************/
    @Override
    public void onVideoAnnotationStart(long userId, int streamId) {
        NavController navController = NavHostFragment.findNavController(GridFragment.this);
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_USER_ID,userId);
        bundle.putBoolean(KEY_VIDEO_ANNOTATION_START,true);
        bundle.putInt(KEY_GRID_POS,0);
        navController.navigate(R.id.action_GridFragment_to_FloatFragment,bundle);
    }
    /******************************Annotation End*********************************************/

    private void toFloatView() {
        NavHostFragment.findNavController(GridFragment.this)
                .navigate(R.id.action_GridFragment_to_FloatFragment);
    }

    private void toSwipeRight(){
        Bundle bundle = new Bundle();
        if(mCurrentPos > 0) mCurrentPos-- ;

        if (mCurrentPos == 0) {
            bundle.putInt(KEY_GRID_POS,mCurrentPos);
            NavHostFragment.findNavController(GridFragment.this)
                    .navigate(R.id.action_GridFragment_to_FloatFragment,bundle);
        }else{
            bundle.putInt(KEY_GRID_POS,mCurrentPos);
            NavHostFragment.findNavController(GridFragment.this)
                    .navigate(R.id.action_GridFragment_to_GridFragment,bundle);
        }
    }

    private void toNewGridView(){
        int remoteUserSize = mViewModel.getUserManager().getRemoteUsers().size();
        if (remoteUserSize < 4) {
            return;
        }
        if(mLastPos == mCurrentPos){
            return ;
        }
        Bundle bundle = new Bundle();

        int nextPos = mCurrentPos + 1 ;
        if(remoteUserSize < nextPos * 3){
            bundle.putInt(KEY_GRID_LAST_POS,nextPos);
        }

        bundle.putInt(KEY_GRID_POS,nextPos);
        NavHostFragment.findNavController(GridFragment.this)
                .navigate(R.id.action_GridFragment_to_GridFragment,bundle);
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
            toNewGridView();
        }

        @Override
        public void onSwipeRight() {
            toSwipeRight();
        }
    }


    class VideoItemTouchListener extends VideoTouchListener {

        private final long mUserId ;

        public VideoItemTouchListener(Context c,long userId) {
            super(c);
            mUserId = userId ;
        }

        @Override
        public void onDoubleTap() {
            if(mUserId == mViewModel.getLocalUserId()){
                return ;
            }
            NavController navController = NavHostFragment.findNavController(GridFragment.this);
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_USER_ID,mUserId);
            bundle.putBoolean(KEY_USE_PIN_VIDEO,true);
            bundle.putInt(KEY_GRID_POS,0);
            navController.navigate(R.id.action_GridFragment_to_FloatFragment,bundle);
        }
    }

}
