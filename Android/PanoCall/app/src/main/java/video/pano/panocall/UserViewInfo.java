package video.pano.panocall;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.IVideoRender;
import com.pano.rtc.api.RtcView;

import video.pano.RendererCommon;

import static com.pano.rtc.api.IVideoRender.ScalingType.SCALE_ASPECT_FIT;

// 用于保存用户视图信息，
class UserViewInfo {
    long userId;
    String userName;
    RtcView view;
    TextView textView; // 打印用户ID信息
    ImageView imgView; // audio 状态图标
    ConstraintLayout clView;
    boolean isFree = true; // 此视图是否空闲
    boolean isScreen = false; // 此视图是否正在显示桌面共享
    boolean isSubscribed = false; // 此视图是否有订阅视频或桌面共享
    Constants.VideoProfileType subProfile; // 此用户的当前订阅能力

    void initView(int index, RtcView v, TextView tv, ImageView img, ConstraintLayout cl, IVideoRender.ScalingType scaleType) {
        this.view = v;
        this.textView = tv;
        this.imgView = img;
        this.clView = cl;
        this.view.setScalingType(scaleType);
        this.view.init(new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {}
            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {}
        });
        if (index != 0) {
            this.view.setZOrderMediaOverlay(true);
        }
    }

    void setVisible(boolean visible) {
        setVideoVisible(visible);
        setUserVisible(visible);
    }

    void setVideoVisible(boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    void setUserVisible(boolean visible) {
        if (clView != null) {
            clView.setVisibility(visible ? View.VISIBLE : View.GONE);
        } else {
            textView.setVisibility(visible ? View.VISIBLE : View.GONE);
            imgView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    void setUser(long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        if (userName != null && !userName.isEmpty()) {
            textView.setText(userName);
        } else {
            textView.setText("" + userId);
        }
    }

    void reset() {
        if (view != null) {
            view.release();
            view = null;
        }
        textView = null;
        imgView = null;
        isFree = true;
        isScreen = false;
    }
}
