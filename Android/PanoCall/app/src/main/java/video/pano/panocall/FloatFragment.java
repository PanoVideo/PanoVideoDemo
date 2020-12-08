package video.pano.panocall;

import android.os.Bundle;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.pano.rtc.api.Constants;


public class FloatFragment extends CallFragment implements CallViewModel.CallEventHandler {

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
    }

    @Override
    public void onPause() {
        super.onPause();
        // save large view user
        mViewModel.mLargeViewUserId = mUserViewArray[0].userId;
    }


    @Override
    void setupUserViewArray() {
        // 初始化视图数组，总共5个视频视图，1个大图4个小图，0为大图，其他为小图
        initUserViewArray(5);

        View view = getView();

        // 设置点击view时显示或隐藏控制按钮
        view.findViewById(R.id.cl_large_view).setOnTouchListener(new OnPanoTouchListener(getActivity()) {
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
        });

        // 配置大图的视图参数
        mUserViewArray[0].initView(0,
                view.findViewById(R.id.large_view),
                view.findViewById(R.id.tv_large_view_user),
                view.findViewById(R.id.img_large_view_audio),
                null,
                mViewModel.mScalingType);
        mUserViewArray[0].view.setOnLongClickListener(null);
        // 设置点击大图时显示或隐藏控制按钮
        mUserViewArray[0].view.setOnTouchListener(new OnPanoTouchListener(getActivity()) {
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
        });

        // 配置右上角小图的视图参数
        mUserViewArray[4].initView(4,
                view.findViewById(R.id.small_view_righttop),
                view.findViewById(R.id.tv_small_view_righttop_user),
                view.findViewById(R.id.img_small_view_righttop_audio),
                view.findViewById(R.id.cl_small_view_righttop),
                mViewModel.mScalingType);

        // 配置左上角小图的视图参数
        mUserViewArray[3].initView(3,
                view.findViewById(R.id.small_view_lefttop),
                view.findViewById(R.id.tv_small_view_lefttop_user),
                view.findViewById(R.id.img_small_view_lefttop_audio),
                view.findViewById(R.id.cl_small_view_lefttop),
                mViewModel.mScalingType);

        // 配置左下角小图的视图参数
        mUserViewArray[2].initView(2,
                view.findViewById(R.id.small_view_leftbottom),
                view.findViewById(R.id.tv_small_view_leftbottom_user),
                view.findViewById(R.id.img_small_view_leftbottom_audio),
                view.findViewById(R.id.cl_small_view_leftbottom),
                mViewModel.mScalingType);

        // 配置右下角小图的视图参数，此视图一般用于显示本地用户视频
        mUserViewArray[1].initView(1,
                view.findViewById(R.id.small_view_rightbottom),
                view.findViewById(R.id.tv_small_view_rightbottom_user),
                view.findViewById(R.id.img_small_view_rightbottom_audio),
                view.findViewById(R.id.cl_small_view_rightbottom),
                mViewModel.mScalingType);

        // 设置长按小图时将此小图的用户和大图用户交换视图
        for (int i = 1; i < mUserViewCount; i++) {
            int index = i;
            mUserViewArray[i].view.setOnTouchListener(new OnPanoTouchListener(getActivity()) {
                @Override
                public void onDoubleTap() {
                    switchToLargeView(index);
                }
            });
        }

        LongSparseArray<UserManager.UserInfo> remoteUsers = mViewModel.getUserManager().getRemoteUsers();
        int localIndex = remoteUsers.size()>0 ? 4 : 0;
        if (mViewModel.mLargeViewUserId != 0 &&
                mViewModel.mLargeViewUserId != mViewModel.getLocalUserId() &&
                mViewModel.mIsRoomJoined) {
            // setup large view user
            UserManager.UserInfo user = remoteUsers.get(mViewModel.mLargeViewUserId);
            if (user != null) {
                setupUserView(user);
            }
        }
        initUserVideoView(localIndex, true);
    }

    // 将index指定的视图用户和大图用户交换
    private void switchToLargeView(int index) {
        if (index < 1 || index > 4) {
            return;
        }
        if (mUserViewArray[index].isFree) {
            return;
        }
        if (!mUserViewArray[0].isFree && mUserViewArray[0].isScreen) {
            return;
        }

        long userId = mUserViewArray[index].userId;
        String userName = mUserViewArray[index].userName;
        boolean isFree = mUserViewArray[index].isFree;
        boolean isScreen = mUserViewArray[index].isScreen;
        boolean isSubscribed = mUserViewArray[index].isSubscribed;
        mUserViewArray[index].setUser(mUserViewArray[0].userId, mUserViewArray[0].userName);
        mUserViewArray[index].isScreen = mUserViewArray[0].isScreen;
        mUserViewArray[index].isSubscribed = mUserViewArray[0].isSubscribed;
        mUserViewArray[index].isFree = mUserViewArray[0].isFree;
        if (mUserViewArray[index].userId == mViewModel.getLocalUserId()) {
            updateLocalVideoRender(mUserViewArray[index].view, index);
        } else if (mUserViewArray[index].isFree) {
            mUserViewArray[index].setVisible(false);
        } else {
            UserManager.UserInfo user = mViewModel.getUserManager().getRemoteUser(mUserViewArray[index].userId);
            if (user.isVideoStarted()) {
                subscribeUserVideo(mUserViewArray[index].userId, mUserViewArray[index].userName,
                        mUserViewArray[index], Constants.VideoProfileType.Lowest);
            } else {
                mUserViewArray[index].setVideoVisible(false);
            }
        }
        updateUserAudioState(index);

        mUserViewArray[0].setUser(userId, userName);
        mUserViewArray[0].isFree = isFree;
        mUserViewArray[0].isScreen = isScreen;
        mUserViewArray[0].isSubscribed = isSubscribed;
        if (mUserViewArray[0].userId == mViewModel.getLocalUserId()) {
            updateLocalVideoRender(mUserViewArray[0].view, 0);
            mUserViewArray[0].setVisible(true);
        } else {
            UserManager.UserInfo user = mViewModel.getUserManager().getRemoteUser(mUserViewArray[0].userId);
            if (user.isScreenStarted()) {
                if (mUserViewArray[0].isSubscribed) {
                    unsubscribeUserVideo(mUserViewArray[0].userId, mUserViewArray[0]);
                }
                subscribeUserScreen(user.userId, mUserViewArray[0].userName, mUserViewArray[0]);
            } else if (user.isVideoStarted()) {
                Constants.VideoProfileType profile = getMaxProfileForVideoUser(mUserViewArray[0].userId);
                subscribeUserVideo(mUserViewArray[0].userId, mUserViewArray[0].userName, mUserViewArray[0], profile);
            } else {
                mUserViewArray[0].setVideoVisible(false);
            }
        }
        updateUserAudioState(0);
    }

    @Override
    void stopUserView(long userId, int index) {
        // check if there is unsubscribed video user
        CallViewModel.VideoUserInfo vui = getUnsubscribedVideoUser();
        if (vui != null) {
            Constants.VideoProfileType profile = Constants.VideoProfileType.Lowest;
            if (index == 0) {
                profile = vui.maxProfile;
                if (profile.getValue() > mViewModel.mRemoteProfile.getValue()) {
                    profile = mViewModel.mRemoteProfile;
                }
            }
            if (subscribeUserVideo(vui.userId, vui.userName, mUserViewArray[index], profile)) {
                updateUserAudioState(index);
                return;
            }
        }
        // if large view user left, then try to move other remote user to large view
        if (index == 0) { // is large view
            for (int j = mUserViewCount - 1; j > 0; j--) {
                if (!mUserViewArray[j].isFree && mUserViewArray[j].userId != mViewModel.getLocalUserId()) {
                    UserManager.UserInfo user = mViewModel.getUserManager().getRemoteUser(mUserViewArray[j].userId);
                    if (user.isScreenStarted()) {
                        if (mUserViewArray[j].isSubscribed) {
                            unsubscribeUserVideo(mUserViewArray[j].userId, mUserViewArray[j]);
                        }
                        subscribeUserScreen(mUserViewArray[j].userId, mUserViewArray[j].userName, mUserViewArray[0]);
                    } else if (user.isVideoStarted()) {
                        Constants.VideoProfileType profile = getMaxProfileForVideoUser(mUserViewArray[j].userId);
                        subscribeUserVideo(mUserViewArray[j].userId, mUserViewArray[j].userName, mUserViewArray[0], profile);
                    } else {
                        mUserViewArray[0].setUser(user.userId, user.userName);
                        mUserViewArray[0].isFree = false;
                        mUserViewArray[0].isSubscribed = false;
                        mUserViewArray[0].setUserVisible(true);
                        mUserViewArray[0].setVideoVisible(false);
                    }
                    updateUserAudioState(0);
                    mUserViewArray[j].isFree = true;
                    mUserViewArray[j].isSubscribed = false;
                    mUserViewArray[j].setVisible(false);
                    return;
                }
            }
        }

        mUserViewArray[index].isFree = true;
        mUserViewArray[index].setVisible(false);
    }

    @Override
    Constants.VideoProfileType getProfileForVideoView(int index, Constants.VideoProfileType maxProfile) {
        Constants.VideoProfileType profile = Constants.VideoProfileType.Lowest;
        if (index == 0) {
            profile = maxProfile;
            if (profile.getValue() > mViewModel.mRemoteProfile.getValue()) {
                profile = mViewModel.mRemoteProfile;
            }
        }
        return profile;
    }

    @Override
    int allocIndexForRemoteUser(UserManager.UserInfo user) {
        int viewIndex = getIndexForUser(user.userId);
        if (viewIndex != -1) {
            return viewIndex;
        }
        // 此用户还未显示，找到一个空闲的显示位置
        UserManager.UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        // 先检查大图是否空闲，如空闲则将此用户显示到大图
        if (mUserViewArray[0].isFree || mUserViewArray[0].userId == localUser.userId) {
            // large view is free or used by local user, then make this user to large view
            // 如果大图被本地用户占用，则将本地用户移到小图，如果没有空闲的小图，则不显示本地用户视频
            if (mUserViewArray[0].userId == localUser.userId) {
                // move local user to last view
                mLocalView = null;
                if (mUserViewArray[mUserViewCount-1].isFree) {
                    mLocalView = mUserViewArray[mUserViewCount-1].view;
                    mUserViewArray[mUserViewCount-1].setVisible(true);
                    mUserViewArray[mUserViewCount-1].isFree = false;
                    mUserViewArray[mUserViewCount-1].isScreen = false;
                    mUserViewArray[mUserViewCount-1].setUser(localUser.userId, localUser.userName);
                    updateUserAudioState(mUserViewCount-1);
                }
                updateLocalVideoRender(mLocalView, mUserViewCount-1);
            }
            viewIndex = 0;
        } else {
            // 如大图不空闲，则尝试找到一个空闲的小图
            // try to find a free small view
            for (int i=1; i < mUserViewCount; i++) {
                if (mUserViewArray[i].isFree) {
                    viewIndex = i;
                    break;
                }
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
    void setupUserScreenView(UserManager.UserInfo user) {
        // 如果大图已经在显示桌面共享，则首先取消订阅之前的共享
        if (!mUserViewArray[0].isFree && mUserViewArray[0].isScreen) {
            unsubscribeUserScreen(mUserViewArray[0].userId, mUserViewArray[0]);
        }
        // 如果此用户已显示，则将此用户移到大图
        int viewIndex = getIndexForUser(user.userId);
        if (viewIndex > 0) { // the user is already shown
            switchToLargeView(viewIndex);
            return;
        } else if (viewIndex == 0) { // the user is already shown at index 0
            if (mUserViewArray[0].isSubscribed) {
                unsubscribeUserVideo(mUserViewArray[0].userId, mUserViewArray[0]);
            }
            subscribeUserScreen(user.userId, user.userName,  mUserViewArray[0]);
            return;
        }
        // 有用户开启桌面共享，始终显示桌面共享到大图
        if (!mUserViewArray[0].isFree) {
            UserManager.UserInfo localUser = mViewModel.getUserManager().getLocalUser();
            if (mUserViewArray[0].userId == localUser.userId) {
                // 如果大图被本地用户占用，则将本地用户移到小图，如果没有空闲的小图，则不显示本地用户视频
                // large view is used by local user, move local user to last view
                mLocalView = null;
                if (mUserViewArray[mUserViewCount-1].isFree) {
                    mLocalView = mUserViewArray[mUserViewCount-1].view;
                    mUserViewArray[mUserViewCount-1].setVisible(true);
                    mUserViewArray[mUserViewCount-1].isFree = false;
                    mUserViewArray[mUserViewCount-1].isScreen = false;
                    mUserViewArray[mUserViewCount-1].setUser(localUser.userId, localUser.userName);
                    updateUserAudioState(mUserViewCount-1);
                }
                updateLocalVideoRender(mLocalView, mUserViewCount-1);
            } else {
                // 尝试找到一个空闲的小图给当前大图用户
                // try to find a free small view
                viewIndex = -1;
                for (int i=1; i < mUserViewCount; i++) {
                    if (mUserViewArray[i].isFree) {
                        mUserViewArray[i].setUser(mUserViewArray[0].userId, mUserViewArray[0].userName);
                        mUserViewArray[i].isFree = false;
                        mUserViewArray[i].isScreen = false;
                        viewIndex = i;
                        break;
                    }
                }
                if (viewIndex != -1) {
                    // 找到了一个空闲视图，将大图用户迁移到此空闲视图
                    Constants.VideoProfileType profile = Constants.VideoProfileType.Lowest;
                    subscribeUserVideo(mUserViewArray[0].userId, mUserViewArray[0].userName, mUserViewArray[viewIndex], profile);
                    updateUserAudioState(viewIndex);
                } else {
                    // 找不到空闲视图，取消订阅此用户
                    // no view available
                    unsubscribeUserVideo(mUserViewArray[0].userId, mUserViewArray[0]);
                }
            }
            mUserViewArray[0].isFree = true;
            mUserViewArray[0].isScreen = false;
        }

        // 订阅此用户桌面共享，并显示到大图
        if (subscribeUserScreen(user.userId, user.userName, mUserViewArray[0])) {
            updateUserAudioState(0);
        } else {
            mUserViewArray[0].isFree = true;
        }
    }

    int getAudioMutedResourceId(int index) {
        return index == 0 ? R.drawable.large_microphone_muted : R.drawable.small_microphone_muted;
    }
    int getAudioNormalResourceId(int index) {
        return index == 0 ? R.drawable.large_microphone_normal : R.drawable.small_microphone_normal;
    }


    private void toGridView() {
        if (mViewModel.getUserManager().getRemoteUsers().size() < 2) {
            return;
        }
        NavHostFragment.findNavController(FloatFragment.this)
                .navigate(R.id.action_FloatFragment_to_GridFragment);
    }
}
