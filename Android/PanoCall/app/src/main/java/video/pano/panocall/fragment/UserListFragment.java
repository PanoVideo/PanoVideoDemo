package video.pano.panocall.fragment;

import static video.pano.panocall.info.Constant.KEY_GRID_POS;
import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_VIDEO_ANNOTATION_START;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pano.rtc.api.Constants;

import video.pano.panocall.R;
import video.pano.panocall.adapter.UserListItemAdapter;
import video.pano.panocall.info.UserManager;
import video.pano.panocall.model.UserInfo;

public class UserListFragment extends CallFragment {


    private UserListItemAdapter mUserListItemAdapter;
    private TextView mTitleView;

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

        int userCount = UserManager.getIns().getRemoteSize() + 1;
        mTitleView.setText(getString(R.string.title_call_user_list, userCount));

        leftIcon.setVisibility(View.VISIBLE);
        leftIcon.setImageResource(R.drawable.svg_icon_back);
        leftIcon.setOnClickListener(v -> {
                    if(UserManager.getIns().getRemoteSize() < 2){
                        NavHostFragment.findNavController(UserListFragment.this).navigate(R.id.action_UserListFragment_to_FloatFragment);
                    }else{
                        NavHostFragment.findNavController(UserListFragment.this).navigateUp();
                    }
                }
        );
    }

    private void initView(View view) {
        RecyclerView rvUserList = view.findViewById(R.id.rv_user_list);
        rvUserList.setLayoutManager(new LinearLayoutManager(getContext()));
        mUserListItemAdapter = new UserListItemAdapter(getContext());
        rvUserList.setAdapter(mUserListItemAdapter);
        mUserListItemAdapter.refreshData();
    }

    /***************************Indication Start*******************************************/
    @Override
    public void onUserLeaveIndication(UserInfo user, Constants.UserLeaveReason reason) {
        refreshView();
    }

    @Override
    public void onUserJoinIndication(UserInfo user) {
        super.onUserJoinIndication(user);
        refreshView();
    }

    /***************************Indication End*******************************************/

    /******************************Annotation Start*********************************************/
    @Override
    public void onVideoAnnotationStart(long userId, int streamId) {
        NavController navController = NavHostFragment.findNavController(UserListFragment.this);
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_USER_ID,userId);
        bundle.putBoolean(KEY_VIDEO_ANNOTATION_START,true);
        bundle.putInt(KEY_GRID_POS,0);
        navController.navigate(R.id.action_UserListFragment_to_FloatFragment,bundle);
    }
    /******************************Annotation End*********************************************/

    private void refreshView() {
        int userCount = UserManager.getIns().getRemoteSize() + 1;
        mTitleView.setText(getString(R.string.title_call_user_list, userCount));
        if (mUserListItemAdapter != null) mUserListItemAdapter.refreshData();
    }
}
