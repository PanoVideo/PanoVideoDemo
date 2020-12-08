package video.pano.panocall;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.pano.rtc.api.Constants;

public class GridFragment extends CallFragment {

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
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    void setupUserViewArray() {
        // 初始化宫格视图数组，目前支持四宫格
        initUserViewArray(4);

        View view = getView();

        // 设置点击view时显示或隐藏控制按钮
        view.setOnTouchListener(new OnPanoTouchListener(getActivity()) {
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
        });

        // 配置左上角小图的视图参数, 此视图一般用于显示本地用户视频
        mUserViewArray[0].initView(0,
                view.findViewById(R.id.medium_view_lefttop),
                view.findViewById(R.id.tv_medium_view_lefttop_user),
                view.findViewById(R.id.img_medium_view_lefttop_audio),
                null,
                mViewModel.mScalingType);

        // 配置右上角视图参数
        mUserViewArray[1].initView(1,
                view.findViewById(R.id.medium_view_righttop),
                view.findViewById(R.id.tv_medium_view_righttop_user),
                view.findViewById(R.id.img_medium_view_righttop_audio),
                null,
                mViewModel.mScalingType);

        // 配置左下角视图参数
        mUserViewArray[2].initView(2,
                view.findViewById(R.id.medium_view_leftbottom),
                view.findViewById(R.id.tv_medium_view_leftbottom_user),
                view.findViewById(R.id.img_medium_view_leftbottom_audio),
                null,
                mViewModel.mScalingType);

        // 配置右下角视图参数
        mUserViewArray[3].initView(3,
                view.findViewById(R.id.medium_view_rightbottom),
                view.findViewById(R.id.tv_medium_view_rightbottom_user),
                view.findViewById(R.id.img_medium_view_rightbottom_audio),
                null,
                mViewModel.mScalingType);

        // 设置长按小图时将此小图的用户和大图用户交换视图
        for (int i=0; i<mUserViewCount; i++) {
            mUserViewArray[i].view.setOnTouchListener(new OnPanoTouchListener(getActivity()) {
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
            });
        }

        initUserVideoView(0, false);
    }

    @Override
    Constants.VideoProfileType getProfileForVideoView(int index, Constants.VideoProfileType maxProfile) {
        return Constants.VideoProfileType.Standard;
    }

    @Override
    void onUserLeft(long userId) {
        if (mViewModel.getUserManager().getRemoteUsers().size() < 2) {
            toFloatView();
        }
    }


    private void toFloatView() {
        NavHostFragment.findNavController(GridFragment.this)
                .navigate(R.id.action_GridFragment_to_FloatFragment);
    }
}
