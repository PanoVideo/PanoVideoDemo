package video.pano.panocall.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.pano.rtc.api.model.RtcAudioLevel;

import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.listener.OnPanoTouchListener;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.utils.AnnotationHelper;
import video.pano.panocall.utils.Utils;

import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_USE_PIN_VIDEO;

public class GridFragment extends CallFragment {

    private final static int VOLUME_LIMIT = 500 ;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grid, container, false);
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

        initUserVideoView(0,false);

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
            initUserVideoView(0,false);
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
    void setupUserScreenView(UserInfo user) {
        if (AnnotationHelper.getInstance().annotationEnable()) {
            return;
        }
       toFloatView();
    }

    private void toFloatView() {
        NavHostFragment.findNavController(GridFragment.this)
                .navigate(R.id.action_GridFragment_to_FloatFragment);
    }

    private void toFloatViewByArgs(long userId){
        NavController navController = NavHostFragment.findNavController(GridFragment.this);
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_USER_ID,userId);
        bundle.putBoolean(KEY_USE_PIN_VIDEO,true);
        navController.navigate(R.id.action_GridFragment_to_FloatFragment,bundle);
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
        public void onSwipeRight() {
            toFloatView();
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
            toFloatViewByArgs(mUserId);
        }
    }

}
