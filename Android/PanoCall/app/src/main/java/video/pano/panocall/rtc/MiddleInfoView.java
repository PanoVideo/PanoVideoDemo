package video.pano.panocall.rtc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import video.pano.panocall.R;

public class MiddleInfoView extends AbsInfoViewComponent {

    public MiddleInfoView(@NonNull Context context) {
        this(context, null);
    }

    public MiddleInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public MiddleInfoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        setZOrderMediaOverlay(true);
    }

    private void initView(@NonNull Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.widget_middle_info_view, this, true);
        mRtcView = rootView.findViewById(R.id.medium_view);
        mUserNameText = rootView.findViewById(R.id.tv_medium_view_user);
        mAudioImg = rootView.findViewById(R.id.img_medium_view_audio);
        mDefaultHeadImg = rootView.findViewById(R.id.img_medium_view_default_head);
        mSignalImg = rootView.findViewById(R.id.img_medium_view_signal);
        mActiveImg = rootView.findViewById(R.id.img_medium_view_active_bg);
    }


}
