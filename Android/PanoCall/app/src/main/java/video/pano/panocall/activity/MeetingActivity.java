package video.pano.panocall.activity;

import static video.pano.panocall.info.Constant.KEY_GRID_PAGE_NUM;
import static video.pano.panocall.info.Constant.KEY_LEAVE_CONFIRM;
import static video.pano.panocall.info.Constant.KEY_PANO_TOKEN;
import static video.pano.panocall.info.Constant.KEY_ROOM_ID;
import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_USER_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.pano.rtc.api.PanoAnnotation;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.fragment.AnnotationControlPanelFragment;
import video.pano.panocall.fragment.BottomControlPanelFragment;
import video.pano.panocall.fragment.TopControlPanelFragment;
import video.pano.panocall.info.Constant;
import video.pano.panocall.info.UserManager;
import video.pano.panocall.listener.OnActEventListener;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.service.ScreenCaptureService;
import video.pano.panocall.utils.AnnotationHelper;
import video.pano.panocall.utils.MiscUtils;
import video.pano.panocall.utils.SPUtils;
import video.pano.panocall.utils.ThreadUtils;
import video.pano.panocall.utils.Utils;
import video.pano.panocall.view.DotsContainer;
import video.pano.panocall.viewmodel.MeetingViewModel;
import video.pano.rtc.base.util.NetworkChangeObserver;
import video.pano.rtc.base.util.NetworkChangeReceiver;
import video.pano.rtc.base.util.NetworkUtils;

public class MeetingActivity extends AppCompatActivity implements NetworkChangeObserver,
        NavController.OnDestinationChangedListener,
        OnActEventListener{

    private MeetingViewModel mViewModel;

    private View mLoadingView;
    private DotsContainer mClDots;
    private NavController mNavController;
    private TopControlPanelFragment mTopControlPanel;
    private BottomControlPanelFragment mBottomControlPanel;
    private AnnotationControlPanelFragment mAnnotationControlPanelFragment;

    private String mRoomId;
    private long mUserId;
    private String mUserName;
    private String mToken;
    private int mCurrentPos = 0;
    private int mPreToWhiteboardId = R.id.action_SpeakerFragment_to_WhiteboardFragment;
    private boolean mIsControlPanelShowed = false;
    private ScheduledThreadPoolExecutor mThreadPoolTimer;

    private NetworkChangeReceiver mNetworkChangeReceiver;
    private Intent mScreenCaptureService;
    private Observer<String> mMessageObserver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        initToolbar();
        initViewModel();
        getData();
        initViews();
        initListener();
        showControlPanel();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        MiscUtils.setScreenOnFlag(getWindow());
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkWhiteboardState();
        checkAnnotationState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetTimer();
        unregisterNetworkReceiver();
        getLifecycle().removeObserver(mViewModel);
        mViewModel.setOnActivityRtcEventListener(null);
        if(mMessageObserver != null){
            mViewModel.getMessageEvent().removeObserver(mMessageObserver);
        }
    }

    @Override
    public void onBackPressed() {
        boolean needConfirm = SPUtils.getBoolean(KEY_LEAVE_CONFIRM, true);
        if (needConfirm) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.msg_call_exit_alert);
            builder.setPositiveButton(R.string.title_button_ok, (dialog, id) -> onConfirmedBackPressed());
            builder.setNegativeButton(R.string.title_button_cancel, null);
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        onConfirmedBackPressed();
    }

    private void onConfirmedBackPressed() {
        mViewModel.onConfirmedBackPressed();
        if (mScreenCaptureService != null)
            stopService(mScreenCaptureService);

        int count = getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < count; i++) {
            getSupportFragmentManager().popBackStack();
        }
        super.onBackPressed();
        finish();
    }

    private void initViews() {
        mClDots = findViewById(R.id.cl_dots);
        mLoadingView = findViewById(R.id.loading_layout);
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mNavController.addOnDestinationChangedListener(this);

        mTopControlPanel = TopControlPanelFragment.newInstance(mRoomId);
        mBottomControlPanel = BottomControlPanelFragment.newInstance();
    }

    private void getData() {
        if (getIntent() != null) {
            mRoomId = getIntent().getStringExtra(KEY_ROOM_ID);
            mUserId = getIntent().getLongExtra(KEY_USER_ID, 0);
            mToken = getIntent().getStringExtra(KEY_PANO_TOKEN);
            mUserName = getIntent().getStringExtra(KEY_USER_NAME);
        }
        if (getResources() != null && getResources().getConfiguration() != null) {
            mViewModel.mCurrentOrientation = getResources().getConfiguration().orientation;
        }

        mViewModel.setIntentData(mRoomId, mUserId, mToken, mUserName);
    }

    private void initViewModel() {
        PanoApplication app = (PanoApplication) Utils.getApp();
        app.getLocalVideoProfile().observe(this, profile -> {
            mViewModel.refreshVideoProfile(profile);
        });

        app.getVideoFrameRateType().observe(this, videoFrameRateType -> {
            mViewModel.refreshVideoFrame(videoFrameRateType);
        });

        mViewModel = new ViewModelProvider(this).get(MeetingViewModel.class);
        getLifecycle().addObserver(mViewModel);

        //Toast Message
        mMessageObserver = liveData ->{
            Toast.makeText(MeetingActivity.this, liveData, Toast.LENGTH_LONG).show();
        };

        mViewModel.setOnActivityRtcEventListener(this);
        mViewModel.getMessageEvent().observe(this, mMessageObserver);
    }

    private void initListener() {
        setupNetworkChangeReceiver();
        mScreenCaptureService = new Intent(this, ScreenCaptureService.class);
    }

    private void checkWhiteboardState() {
        if(mViewModel.mWhiteboardStart){
            NavDestination currentDestination = mNavController.getCurrentDestination();
            if(currentDestination != null && currentDestination.getId() != R.id.WhiteboardFragment){
                mViewModel.onWhiteboardStart();
            }
        }
    }

    private void checkAnnotationState() {
        if(mViewModel.mAnnotationStart){
            NavDestination currentDestination = mNavController.getCurrentDestination();
            if(currentDestination != null){
                mNavController.navigate(mViewModel.mAnnotationActionId,mViewModel.mAnnotationExtra);
            }
        }
    }

    private void showControlPanel() {
        if (!mIsControlPanelShowed) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (!mTopControlPanel.isAdded()) {
                ft.replace(R.id.fl_top_control_panel, mTopControlPanel)
                        .addToBackStack(null);
            }
            if (!mBottomControlPanel.isAdded()) {
                ft.replace(R.id.fl_bottom_control_panel, mBottomControlPanel)
                        .addToBackStack(null);
            }
            try {
                ft.show(mTopControlPanel).show(mBottomControlPanel)
                        .commitAllowingStateLoss();
            } catch (IllegalStateException e) {
                return;
            }
            mIsControlPanelShowed = true;
            showTimer();
        }
    }


    private void hideControlPanel() {
        if(mTopControlPanel != null && mBottomControlPanel != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(mTopControlPanel).remove(mBottomControlPanel).commitAllowingStateLoss();
            mIsControlPanelShowed = false;
            resetTimer();
        }
    }

    private void hideAnnotationControlPanel() {
        if (mAnnotationControlPanelFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(mAnnotationControlPanelFragment).commitAllowingStateLoss();
            mIsControlPanelShowed = false;
        }
    }

    private void showAnnotationControlPanel() {
        mAnnotationControlPanelFragment = AnnotationControlPanelFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_control_panel, mAnnotationControlPanelFragment)
                .addToBackStack(null);
        try {
            ft.show(mAnnotationControlPanelFragment)
                    .commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mViewModel.mCurrentOrientation = newConfig.orientation;

        NavDestination current = mNavController.getCurrentDestination();
        if (current == null) {
            return;
        }
        if (Constant.WHITEBOARD_FRAGMENT_LABEL.contentEquals(current.getLabel())) {
            mNavController.navigateUp();
            mNavController.navigate(mPreToWhiteboardId);
        }
    }

    private void showTimer() {
        mThreadPoolTimer = new ScheduledThreadPoolExecutor(1);
        mThreadPoolTimer.schedule(new PanelTask(), 6, TimeUnit.SECONDS);
    }

    private void resetTimer() {
        if (mThreadPoolTimer != null) {
            mThreadPoolTimer.shutdown();
            mThreadPoolTimer.purge();
        }
    }


    private void setupNetworkChangeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.setPriority(100);

        mNetworkChangeReceiver = new NetworkChangeReceiver(this);
        mNetworkChangeReceiver.setNetworkChangeObserver(this);
        registerReceiver(mNetworkChangeReceiver, intentFilter);
    }

    private void unregisterNetworkReceiver() {
        if (mNetworkChangeReceiver != null) {
            unregisterReceiver(mNetworkChangeReceiver);
        }
    }

    @Override
    public void onNetworkChanged(NetworkUtils.NetworkType type) {
        switch (type) {
            case MOBILE:
                Toast.makeText(this, R.string.msg_cellular_data, Toast.LENGTH_SHORT).show();
                break;
            case NOT_CONNECTED:
                Toast.makeText(this, R.string.msg_no_data, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        hideControlPanel();
        int childCount = mClDots.getChildCount();
        if (arguments != null && arguments.containsKey(KEY_GRID_PAGE_NUM)) {
            int pos = arguments.getInt(KEY_GRID_PAGE_NUM);
            if (pos < childCount) {
                mClDots.checkItem(pos);
                mCurrentPos = pos;
            }
        }
        checkDotIndicatorState();
        mViewModel.onDestinationChangedListener();
    }

    private void checkDotIndicatorState() {
        int userCount = mViewModel.getShowUserCount();

        if (userCount <= 2) {
            mClDots.setVisibility(View.GONE);
        } else {
            int pageSize = 2;
            if (userCount > 4) {
                pageSize = userCount / 4 + 2;
            }
            mClDots.showDots(pageSize);
            if (mCurrentPos == pageSize) {
                mClDots.checkItem(mCurrentPos - 1);
                mNavController.navigateUp();
            } else if (mCurrentPos < pageSize) {
                mClDots.checkItem(mCurrentPos);
            }
            mClDots.setVisibility(View.VISIBLE);

            NavDestination current = mNavController.getCurrentDestination();
            if (current != null) {
                if (current.getId() == R.id.WhiteboardFragment || current.getId() == R.id.UserListFragment) {
                    mClDots.setVisibility(View.GONE);
                }
            }
        }
    }

    /*********************RtcEngine Callback******************************/
    @Override
    public void onChannelLeaveIndication() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.msg_call_left_alert);
        builder.setPositiveButton(R.string.title_button_ok, (dialog, id) -> onConfirmedBackPressed());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onUserScreenStart(UserInfo user) {
        checkDotIndicatorState();
    }

    @Override
    public void onUserScreenStop(UserInfo user) {
        checkDotIndicatorState();
    }

    @Override
    public void onUserJoinIndication() {
        checkDotIndicatorState();
    }

    @Override
    public void onUserLeaveIndication(UserInfo user) {
        if (AnnotationHelper.getIns().getAnnotationUserId() == user.userId) {
            onVideoAnnotationStop(user.userId);
        }
        checkDotIndicatorState();
    }

    @Override
    public void onWhiteboardStart() {
        NavDestination current = mNavController.getCurrentDestination();
        if (current == null) {
            return;
        }
        if (Constant.SPEAKER_FRAGMENT_LABEL.contentEquals(current.getLabel())) {
            mPreToWhiteboardId = R.id.action_SpeakerFragment_to_WhiteboardFragment;
            mNavController.navigate(mPreToWhiteboardId);
        } else if (Constant.GALLERY_FRAGMENT_LABEL.contentEquals(current.getLabel())) {
            mPreToWhiteboardId = R.id.action_GalleryFragment_to_WhiteboardFragment;
            mNavController.navigate(mPreToWhiteboardId);
        } else if (Constant.USER_LIST_FRAGMENT_LABEL.contentEquals(current.getLabel())) {
            mPreToWhiteboardId = R.id.action_UserListFragment_to_WhiteboardFragment;
            mNavController.navigate(mPreToWhiteboardId);
        }
        hideAnnotationControlPanel();
        hideControlPanel();
    }

    @Override
    public void onChannelFailover(boolean show) {
        mLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    /*********************RtcEngine Callback******************************/
    /**************  OnTopControlPanelListener  **************/
    @Override
    public void onTCPanelExit() {
        onBackPressed();
    }

    @Override
    public void onTCPanelSwitchCamera() {
    }
    /**************  OnTopControlPanelListener  **************/

    /**************  OnBottomControlPanelListener  **************/
    @Override
    public void onBCPanelShare() {
        AlertDialog shareDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_call_share)
                .setItems(R.array.share_settings, (dialog, which) -> {
                    if (which == 0) {
                        mViewModel.onWhiteboardStart();
                        mViewModel.mWhiteboardContentUpdate = false;
                    }
                })
                .setNegativeButton(R.string.title_button_cancel, (dialog, which) -> dialog.dismiss())
                .create();
        if (!shareDialog.isShowing()) {
            shareDialog.show();
        }
    }

    @Override
    public void onBCPanelUserList() {
        NavDestination current = mNavController.getCurrentDestination();
        if (current == null) {
            return;
        }
        if (Constant.SPEAKER_FRAGMENT_LABEL.contentEquals(current.getLabel())) {
            mNavController.navigate(R.id.action_SpeakerFragment_to_UserListFragment);
        } else if (Constant.GALLERY_FRAGMENT_LABEL.contentEquals(current.getLabel())) {
            mNavController.navigate(R.id.action_GalleryFragment_to_UserListFragment);
        }
    }

    @Override
    public void onBCPanelMore() {
        AlertDialog moreDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_call_more)
                .setItems(R.array.more_settings, (dialog, which) -> {
                    if (which == 0) {
                        onClickSettings();
                    }
                })
                .setNegativeButton(R.string.title_button_cancel, (dialog, which) -> dialog.dismiss())
                .create();
        if (!moreDialog.isShowing()) {
            moreDialog.show();
        }
    }

    private void onClickSettings() {
        SettingsActivity.launch(MeetingActivity.this, true);
    }

    /**************  OnBottomControlPanelListener  **************/

    /**************  OnAnnotationControlPanelListener  **************/

    public void onAnnotationStart() {
        AnnotationHelper.getIns().setAnnotationEnable(true);
        hideControlPanel();
        showAnnotationControlPanel();
    }

    public void onAnnotationClose() {
        hideAnnotationControlPanel();
        showControlPanel();
    }

    /**************  OnAnnotationControlPanelListener  **************/

    /**************  OnControlEventListener  **************/
    @Override
    public void onShowHideControlPanel() {
        if (!AnnotationHelper.getIns().annotationEnable()) {
            if (mIsControlPanelShowed) {
                hideControlPanel();
            } else {
                showControlPanel();
            }
        }
    }
    /**************  OnControlEventListener  **************/

    /******************************Annotation Start*********************************************/
    @Override
    public void onVideoAnnotationStart(long userId) {
//        mHandler.postDelayed(() -> {
//        },DELAY_TIME);
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
        if(user !=null && annotation != null){
            onAnnotationStart();
        }
    }

    @Override
    public void onVideoAnnotationStop(long userId) {
        if(mViewModel.mWhiteboardStart) return ;
        onAnnotationClose();
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        PanoAnnotation annotation = AnnotationHelper.getIns().getAnnotation();
        if(user !=null && annotation != null){
            onAnnotationStart();
        }
    }

    @Override
    public void onShareAnnotationStop(long userId) {
        if(mViewModel.mWhiteboardStart) return ;
        onAnnotationClose();
    }

    @Override
    public void onClickAnnotationStop() {
        if(mViewModel.mWhiteboardStart) return ;
        onAnnotationClose();
    }

    /******************************Annotation Start*********************************************/

    public static void launch(Context context, String token, String roomId, long userId, String userName) {
        Intent intent = new Intent();
        intent.putExtra(KEY_PANO_TOKEN, token);
        intent.putExtra(KEY_ROOM_ID, roomId);
        intent.putExtra(KEY_USER_ID, userId);
        intent.putExtra(KEY_USER_NAME, userName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(context, MeetingActivity.class);
        context.startActivity(intent);
    }

    class PanelTask implements Runnable {
        @Override
        public void run() {
            ThreadUtils.runOnUiThread(() -> {
                resetTimer();
                hideControlPanel();
            });
        }
    }
}
