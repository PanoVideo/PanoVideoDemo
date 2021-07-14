package video.pano.panocall.model;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcView;

import video.pano.RendererCommon;

// 用于保存用户视图信息，
public class UserViewInfo {
    public long userId;
    public String userName;
    public RtcView rtcView;
    public TextView textView; // 打印用户ID信息
    public ImageView imgView; // audio 状态图标
    public ImageView headIcon;
    public ImageView signalIcon ;
    public ImageView activeView ;
    public ConstraintLayout clView;
    public boolean isFree = true; // 此视图是否空闲
    public boolean isScreen = false; // 此视图是否正在显示桌面共享
    public boolean isSubscribed = false; // 此视图是否有订阅视频或桌面共享
    public Constants.VideoProfileType subProfile; // 此用户的当前订阅能力

    public void initView(RtcView v, TextView tv, ImageView img, ImageView headImg, ImageView signalImg) {
        initView(v, false,tv, img, headImg, signalImg,null);
    }

    public void initView(RtcView v, boolean isMediaOverla, TextView tv, ImageView img, ImageView headImg, ImageView signalImg, ConstraintLayout cl) {
        this.textView = tv;
        this.imgView = img;
        this.clView = cl;
        this.headIcon = headImg;
        this.signalIcon = signalImg ;
        this.rtcView = v;
        this.rtcView.init(new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {
            }

            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {
            }
        });
        this.rtcView.setZOrderMediaOverlay(isMediaOverla);
    }

    public void setVisible(boolean visible) {
        setVideoVisible(visible);
        setUserVisible(visible);
    }

    public void setVideoVisible(boolean visible) {
        rtcView.setVisibility(visible ? View.VISIBLE : View.GONE);
        setDefaultHeadVisible(!visible);
    }

    public void setUserVisible(boolean visible) {
        if (clView != null) {
            clView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
        textView.setVisibility(visible ? View.VISIBLE : View.GONE);
        imgView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setDefaultHeadVisible(boolean visible){
        if (headIcon != null) {
            headIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setUser(long userId, String userName,boolean isMySelf) {
        this.userId = userId;
        this.userName = userName;
        if (!TextUtils.isEmpty(userName)) {
            textView.setText(isMySelf ? userName+"(Me)" : userName);
        } else {
            textView.setText(String.valueOf(userId));
        }
    }

    public void setSignalRes(int signalRes){
        if(signalIcon != null){
            signalIcon.setImageResource(signalRes);
        }
    }

    public void reset() {
        if (rtcView != null) {
            rtcView.release();
            rtcView = null;
        }
        textView = null;
        imgView = null;
        isFree = true;
        isScreen = false;
        headIcon = null ;
        activeView = null ;
        signalIcon = null ;
    }
}
