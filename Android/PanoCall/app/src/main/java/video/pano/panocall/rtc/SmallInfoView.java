package video.pano.panocall.rtc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import video.pano.panocall.R;

public class SmallInfoView extends AbsInfoViewComponent {

    public SmallInfoView(@NonNull Context context) {
        this(context, null);
    }

    public SmallInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SmallInfoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        setZOrderMediaOverlay(true);
    }

    private void initView(@NonNull Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.widget_small_info_view, this, true);
        mRtcView = rootView.findViewById(R.id.small_view);
        mUserNameText = rootView.findViewById(R.id.tv_small_view_user);
        mAudioImg = rootView.findViewById(R.id.img_small_view_audio);
        mDefaultHeadImg = rootView.findViewById(R.id.img_small_view_default_head);
        mSignalImg = rootView.findViewById(R.id.img_small_view_signal);
    }


}
