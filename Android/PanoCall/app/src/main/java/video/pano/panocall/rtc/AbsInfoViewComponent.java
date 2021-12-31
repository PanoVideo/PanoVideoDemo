package video.pano.panocall.rtc;

import static video.pano.panocall.info.Constant.KEY_ENABLE_USER_STATISTICS;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcView;
import com.pano.rtc.api.RtcWbView;

import video.pano.panocall.R;
import video.pano.panocall.info.UserManager;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.utils.SPUtils;
import video.pano.panocall.utils.Utils;

public abstract class AbsInfoViewComponent extends ConstraintLayout {

    private static final int MY_NAME_MAX_COUNT = 3;
    private static final int USER_NAME_MAX_COUNT = 6;

    protected Context mContext;
    protected UserInfo mUserInfo;
    protected RtcView mRtcView;
    protected Constants.VideoProfileType mProfileType = Constants.VideoProfileType.Low;
    // 打印用户ID信息
    protected TextView mUserNameText;
    // audio 状态图标
    // 信号图标
    protected ImageView mSignalImg;
    protected ImageView mAudioImg;
    protected ImageView mActiveImg;
    protected ImageView mDefaultHeadImg;
    protected FrameLayout mWhiteboardContainer;
    protected ViewGroup mParentView;

    protected View mUserStatisticsView;
    protected TextView decodeTypeText;
    protected TextView resolutionTypeText;
    protected TextView bitRateTextView;

    public AbsInfoViewComponent(@NonNull Context context) {
        this(context, null);
    }

    public AbsInfoViewComponent(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public AbsInfoViewComponent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setData(UserInfo userInfo) {
        mUserInfo = userInfo;
        if (mUserInfo == null) return;
        String userName = mUserInfo.userName;
        if (!TextUtils.isEmpty(userName)) {
            if (UserManager.getIns().isMySelf(mUserInfo.userId)) {
                if (userName.length() > MY_NAME_MAX_COUNT) {
                    userName = userName.substring(0, MY_NAME_MAX_COUNT) + "…(Me)";
                } else {
                    userName = userName + "…(Me)";
                }
            } else {
                if (userName.length() > USER_NAME_MAX_COUNT) {
                    userName = userName.substring(0, USER_NAME_MAX_COUNT) + "…";
                } else {
                    userName = userName + "…";
                }
            }
            mUserNameText.setText(userName);
        } else {
            mUserNameText.setText(String.valueOf(mUserInfo.userId));
        }
    }

    public void setParentView(ViewGroup parentView) {
        mParentView = parentView;
    }

    public ViewGroup getParentView(){
        return mParentView ;
    }

    public void setSignalRes(int signalRes) {
        if (mSignalImg != null) {
            mSignalImg.setImageResource(signalRes);
        }
    }

    public void setActiveViewVisible(boolean visible) {
        if (mActiveImg != null) {
            mActiveImg.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setDefaultHeadVisible(boolean visible) {
        if (mDefaultHeadImg != null) {
            mDefaultHeadImg.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setRtcViewVisible(boolean visible) {
        if (mRtcView != null) {
            mRtcView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setParentViewVisible(boolean visible) {
        if (mParentView != null) {
            mParentView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void setAudioResource(int audioResource) {
        if (mAudioImg != null) {
            mAudioImg.setVisibility(View.VISIBLE);
            mAudioImg.setImageResource(audioResource);
        }
    }

    public void setProfileType(Constants.VideoProfileType profileType) {
        this.mProfileType = profileType;
    }

    public void setZOrderMediaOverlay(boolean overlay){
        mRtcView.setZOrderMediaOverlay(overlay);
    }

    public Constants.VideoProfileType getProfileType() {
        return mProfileType;
    }

    public void addRtcWbView(RtcWbView rtcWbView){
        if (mWhiteboardContainer != null && rtcWbView != null){
            mWhiteboardContainer.removeAllViews();
            mWhiteboardContainer.addView(rtcWbView);
        }
    }

    public RtcView getRtcView() {
        return mRtcView;
    }

    public void initUserStatistics(){
        if(mUserStatisticsView != null){
            mUserStatisticsView.setVisibility(SPUtils.getBoolean(KEY_ENABLE_USER_STATISTICS,false)
                    ? View.VISIBLE : View.GONE);
        }

        String resolutionText = Utils.getApp().getString(R.string.title_video_resolution_type," - ");
        String bitRateText = Utils.getApp().getString(R.string.title_video_bit_rate," - ");
        String decodeText = Utils.getApp().getString(R.string.title_video_encode_type," - ");

        if(decodeTypeText != null){
            decodeTypeText.setText(decodeText);
        }
        if(resolutionTypeText != null){
            resolutionTypeText.setText(resolutionText);
        }
        if(bitRateTextView != null){
            bitRateTextView.setText(bitRateText);
        }
    }

    public void setUserVideoStatisticsVisible(boolean visible){
        if(mUserStatisticsView != null){
            mUserStatisticsView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setUserVideoStatistics(String decodeType , String resolutionType , String bitRate){
        if(decodeTypeText != null){
            decodeTypeText.setText(decodeType);
        }
        if(resolutionTypeText != null){
            resolutionTypeText.setText(resolutionType);
        }
        if(bitRateTextView != null){
            bitRateTextView.setText(bitRate);
        }
    }

    public void releaseData(){
        mUserInfo = null ;
    }

    public void abandon() {
        mRtcView = null;
        mUserNameText = null;
        mAudioImg = null;
        mDefaultHeadImg = null;
        mActiveImg = null;
        mSignalImg = null;
        mUserInfo = null ;
    }

}
