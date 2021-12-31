package video.pano.panocall.rtc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pano.rtc.api.Constants;

import video.pano.panocall.R;

public class LargeInfoView extends AbsInfoViewComponent {

    public LargeInfoView(@NonNull Context context) {
        this(context, null);
    }

    public LargeInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public LargeInfoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        setProfileType(Constants.VideoProfileType.HD1080P);
        setZOrderMediaOverlay(false);
    }

    private void initView(@NonNull Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.widget_large_info_view, this, true);
        mRtcView = rootView.findViewById(R.id.large_view);
        mUserNameText = rootView.findViewById(R.id.tv_large_view_user);
        mAudioImg = rootView.findViewById(R.id.img_large_view_audio);
        mDefaultHeadImg = rootView.findViewById(R.id.img_large_view_default_head);
        mSignalImg = rootView.findViewById(R.id.img_large_view_signal);
        mWhiteboardContainer = rootView.findViewById(R.id.large_whiteboard_view_container);

        mUserStatisticsView = rootView.findViewById(R.id.cl_call_large_user_statistics);
        decodeTypeText = rootView.findViewById(R.id.tv_decode_type);
        resolutionTypeText = rootView.findViewById(R.id.tv_resolution_type);
        bitRateTextView = rootView.findViewById(R.id.tv_bit_rate);
    }


}
