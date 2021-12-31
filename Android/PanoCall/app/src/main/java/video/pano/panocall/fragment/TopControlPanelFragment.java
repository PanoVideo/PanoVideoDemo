package video.pano.panocall.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import video.pano.panocall.R;
import video.pano.panocall.listener.OnTopEventListener;
import video.pano.panocall.viewmodel.MeetingViewModel;


public class TopControlPanelFragment extends Fragment implements OnTopEventListener {

    public static final String TAG = "TopControlPanelFragment";
    private static final String ARG_ROOM_ID = "arg_room_id";

    private static final long INTERVAL_TIME = 1000 ;

    private String mRoomId;
    private MeetingViewModel mViewModel;
    private CountDownTimer mCountDownTimer;

    private TextView mTvTime;
    private ImageView mBtnSwitchCamera;

    public TopControlPanelFragment() {
        // Required empty public constructor
    }

    public static TopControlPanelFragment newInstance(String roomId) {
        TopControlPanelFragment fragment = new TopControlPanelFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRoomId = getArguments().getString(ARG_ROOM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_top_control_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModel();

        TextView tvRoomId = view.findViewById(R.id.tv_call_room_id);
        tvRoomId.setText(mRoomId);
        mTvTime = view.findViewById(R.id.tv_call_room_time);

        ImageView btnAudioSpeaker = view.findViewById(R.id.btn_call_speaker);
        btnAudioSpeaker.setOnClickListener(v -> {
            mViewModel.mIsAudioSpeakerOpened = !mViewModel.mIsAudioSpeakerOpened;
            if (mViewModel.mIsAudioSpeakerOpened) {
                btnAudioSpeaker.setImageResource(R.drawable.svg_icon_speaker);
            } else {
                btnAudioSpeaker.setImageResource(R.drawable.svg_icon_earpiece);
            }
            if(mViewModel != null){
                mViewModel.onTCPanelAudio(mViewModel.mIsAudioSpeakerOpened);
            }
        });
        if (mViewModel.mIsAudioSpeakerOpened) {
            btnAudioSpeaker.setImageResource(R.drawable.svg_icon_speaker);
        } else {
            btnAudioSpeaker.setImageResource(R.drawable.svg_icon_earpiece);
        }
        mBtnSwitchCamera = view.findViewById(R.id.btn_call_switch_camera);
        mBtnSwitchCamera.setVisibility(mViewModel.mAutoStartCamera ? View.VISIBLE : View.INVISIBLE);
        mBtnSwitchCamera.findViewById(R.id.btn_call_switch_camera).setOnClickListener(v -> {
            if(mViewModel != null){
                mViewModel.onTCPanelSwitchCamera();
            }
        });
        view.findViewById(R.id.btn_call_exit).setOnClickListener(v -> {
            if(mViewModel != null){
                mViewModel.onTCPanelExit();
            }
        });
    }

    private void initViewModel() {
        mViewModel = new ViewModelProvider(getActivity()).get(MeetingViewModel.class);
        mViewModel.setOnTopEventListener(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onChannelCountDown(long remain) {
        Log.i(TAG, "onChannelCountDown, remian="+remain);
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        long countDownRemain = remain * 1000;
        mCountDownTimer = new CountDownTimer(countDownRemain, INTERVAL_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTvTime.setText(tickToCountDownText(millisUntilFinished));
            }
            @Override
            public void onFinish() {
                mTvTime.setText(R.string.msg_call_ended);
            }
        }.start();
    }

    @Override
    public void onBCPanelVideo(boolean closed) {
        mBtnSwitchCamera.setVisibility(closed ? View.INVISIBLE : View.VISIBLE);
    }

    private String tickToCountDownText(long tick) {
        long seconds = tick / 1000;

        long hour = seconds / 3600;
        long min = seconds % 3600;
        long sec = min % 60;
        min /= 60;

        return String.format("%02d:%02d:%02d", hour,min, sec);
    }
}
