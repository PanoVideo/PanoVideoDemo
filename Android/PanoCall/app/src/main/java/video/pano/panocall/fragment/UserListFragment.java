package video.pano.panocall.fragment;

import static video.pano.panocall.info.Constant.KEY_SHARE_ANNOTATION_START;
import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_VIDEO_ANNOTATION_START;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavDestination;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pano.rtc.api.Constants;

import video.pano.panocall.R;
import video.pano.panocall.adapter.UserListItemAdapter;
import video.pano.panocall.info.UserManager;
import video.pano.panocall.listener.OnUserListItemClickListener;
import video.pano.panocall.model.PropertyData;
import video.pano.panocall.model.UserInfo;

public class UserListFragment extends MeetingFragment implements OnUserListItemClickListener {


    private UserListItemAdapter mUserListItemAdapter;
    private TextView mTitleView;
    private View mHostBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initTitleView(view);
        initView(view);
    }


    private void initTitleView(View view) {
        mTitleView = view.findViewById(R.id.tv_title);
        ImageView leftIcon = view.findViewById(R.id.iv_left_icon);
        mHostBtn = view.findViewById(R.id.host_btn);

        int userCount = UserManager.getIns().getRemoteSize() + 1;
        mTitleView.setText(getString(R.string.title_call_user_list, userCount));

        leftIcon.setVisibility(View.VISIBLE);
        leftIcon.setImageResource(R.drawable.svg_icon_back);
        leftIcon.setOnClickListener(v -> {
                    if(UserManager.getIns().getRemoteSize() < 2){
                        NavDestination currentDestination = mNavController.getCurrentDestination();
                        if (currentDestination!= null && currentDestination.getId() == R.id.UserListFragment) {
                            mNavController.navigate(R.id.action_UserListFragment_to_SpeakerFragment);
                        }
                    }else{
                        mNavController.navigateUp();
                    }
                }
        );

        refreshHostBtn();

        mHostBtn.setOnClickListener(v -> {
            mViewModel.getPanoWhiteboard().setRoleType(Constants.WBRoleType.Admin);
            mViewModel.sendProperty(new PropertyData(String.valueOf(UserManager.getIns().getLocalUserId())));
            mHostBtn.setEnabled(false);
        });
    }

    private void initView(View view) {
        RecyclerView rvUserList = view.findViewById(R.id.rv_user_list);
        rvUserList.setLayoutManager(new LinearLayoutManager(getContext()));
        mUserListItemAdapter = new UserListItemAdapter(getContext());
        rvUserList.setAdapter(mUserListItemAdapter);
        mUserListItemAdapter.setOnItemClick(this);
        mUserListItemAdapter.refreshData();
    }

    /***************************Indication Start*******************************************/
    @Override
    public void onUserLeaveIndication(UserInfo user) {
        refreshView();
        refreshHostBtn();
    }

    @Override
    public void onUserJoinIndication(UserInfo user) {
        refreshView();
        refreshHostBtn();
    }

    @Override
    public void onUserAudioMute(UserInfo user) {
        refreshItemView(user);
    }
    @Override
    public void onUserAudioUnmute(UserInfo user) {
        refreshItemView(user);
    }

    @Override
    public void onUserAudioStart(UserInfo user) {
        refreshItemView(user);
    }

    @Override
    public void onUserAudioStop(UserInfo user) {
        refreshItemView(user);
    }

    @Override
    public void onUserVideoStart(UserInfo user) {
        refreshItemView(user);
    }
    @Override
    public void onUserVideoStop(UserInfo user) {
        refreshItemView(user);
    }

    @Override
    public void onUserAudioCallTypeChanged(UserInfo user) {
        refreshItemView(user);
    }

    @Override
    public void onHostUserIdChanged(long hostId) {
        mUserListItemAdapter.setHostUserId(hostId);
        if(hostId != 0L) mHostBtn.setEnabled(false);
    }

    /***************************Indication End*******************************************/

    /******************************Annotation Start*********************************************/
    @Override
    public void onVideoAnnotationStart(long userId, int streamIds) {
        NavDestination currentDestination = mNavController.getCurrentDestination();
        if(currentDestination == null){
            return ;
        }
        if (currentDestination.getId() == R.id.UserListFragment) {
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_USER_ID,userId);
            bundle.putBoolean(KEY_VIDEO_ANNOTATION_START,true);
            mNavController.navigate(R.id.action_UserListFragment_to_SpeakerFragment,bundle);
        }
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        NavDestination currentDestination = mNavController.getCurrentDestination();
        if(currentDestination == null){
            return ;
        }
        if (currentDestination.getId() == R.id.UserListFragment) {
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_USER_ID,userId);
            bundle.putBoolean(KEY_SHARE_ANNOTATION_START,true);
            mNavController.navigate(R.id.action_UserListFragment_to_SpeakerFragment,bundle);
        }
    }

    /******************************Annotation End*********************************************/

    private void refreshHostBtn(){
        if(UserManager.getIns().getHostId() <= 0L
                || (UserManager.getIns().getRemoteUser(UserManager.getIns().getHostId()) == null
                && UserManager.getIns().getLocalUserId() != UserManager.getIns().getHostId())){
            mHostBtn.setEnabled(true);
        }else{
            mHostBtn.setEnabled(false);
        }
    }

    private void refreshView() {
        int userCount = UserManager.getIns().getRemoteSize() + 1;
        mTitleView.setText(getString(R.string.title_call_user_list, userCount));
        if (mUserListItemAdapter != null) mUserListItemAdapter.refreshData();
    }

    private void refreshItemView(UserInfo userInfo){
        if(userInfo != null && mUserListItemAdapter != null){
            mUserListItemAdapter.refreshItemData(userInfo);
        }
    }

    @Override
    public void onItemClick(UserInfo userInfo) {
        long localUserId = UserManager.getIns().getLocalUserId();
        if(userInfo.userId == localUserId || UserManager.getIns().getHostId() != localUserId){
            return ;
        }
        AlertDialog moreDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_call_operate)
                .setItems(R.array.operate_list, (dialog, which) -> {
                    if (which == 0) {
                        onClickHostUser(userInfo);
                    }
                })
                .setNegativeButton(R.string.title_button_cancel, (dialog, which) -> dialog.dismiss())
                .create();
        if (!moreDialog.isShowing()) {
            moreDialog.show();
        }
    }

    private void onClickHostUser(UserInfo userInfo){
        if(userInfo == null) return ;
        AlertDialog setHostUserDialog = new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.user_list_set_host_user_msg,userInfo.getUserName()))
                .setPositiveButton(R.string.title_button_ok, (dialog, which) -> {
                    mViewModel.sendProperty(new PropertyData(String.valueOf(userInfo.userId)));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.title_button_cancel, null)
                .create();
        if (!setHostUserDialog.isShowing()) {
            setHostUserDialog.show();
        }

    }
}
