package video.pano.panocall.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import video.pano.panocall.R;
import video.pano.panocall.listener.OnBottomEventListener;
import video.pano.panocall.viewmodel.MeetingViewModel;

public class BottomControlPanelFragment extends Fragment implements OnBottomEventListener {

    private MeetingViewModel mViewModel;
    private TextView mBtnAudio;
    private TextView mBtnVideo;
    private TextView mBtnCallShare;

    public BottomControlPanelFragment() {
        // Required empty public constructor
    }

    public static BottomControlPanelFragment newInstance() {
        BottomControlPanelFragment fragment = new BottomControlPanelFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_control_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModel();
        mBtnAudio = view.findViewById(R.id.btn_call_audio);
        mBtnAudio.setOnClickListener(v -> {
            mViewModel.mIsAudioMuted = !mViewModel.mIsAudioMuted;
            updateAudioButtonState();
            if(mViewModel != null){
                mViewModel.onBCPanelAudio(mViewModel.mIsAudioMuted);
            }
        });
        updateAudioButtonState();

        mBtnVideo = view.findViewById(R.id.btn_call_video);
        mBtnVideo.setOnClickListener(v -> {
            mViewModel.mAutoStartCamera = !mViewModel.mAutoStartCamera;
            updateVideoButtonState();
            if (mViewModel != null) {
                mViewModel.onBCPanelVideo(mViewModel.mAutoStartCamera);
            }
        });
        updateVideoButtonState();

        mBtnCallShare = view.findViewById(R.id.btn_call_share);
        mBtnCallShare.setOnClickListener(v -> {
            if (mViewModel != null) {
                mViewModel.onBCPanelShare();
            }
            updateCallShareButtonState();
        });
        updateCallShareButtonState();

        view.findViewById(R.id.btn_call_more).setOnClickListener(v -> {
            if (mViewModel != null) {
                mViewModel.onBCPanelMore();
            }
        });

        view.findViewById(R.id.btn_call_user_list).setOnClickListener(v -> {
            if (mViewModel != null) {
                mViewModel.onBCPanelUserList();
            }
        });
    }

    private void initViewModel() {
        mViewModel = new ViewModelProvider(getActivity()).get(MeetingViewModel.class);
        mViewModel.setOnBottomEventListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void updateAudioButtonState() {
        if (mViewModel.mIsAudioMuted) {
            mBtnAudio.setText(R.string.title_call_audio_muted);
            mBtnAudio.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_audio_mute, 0, 0);
        } else {
            mBtnAudio.setText(R.string.title_call_audio_normal);
            mBtnAudio.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_audio_normal, 0, 0);
        }
    }

    private void updateVideoButtonState() {
        if (mViewModel.mAutoStartCamera) {
            mBtnVideo.setText(R.string.title_call_video_normal);
            mBtnVideo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_video_normal, 0, 0);
        } else {
            mBtnVideo.setText(R.string.title_call_video_closed);
            mBtnVideo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_video_closed, 0, 0);
        }
    }

    @Override
    public void updateCallShareButtonState() {
        if (mViewModel.mWhiteboardContentUpdate) {
            mBtnCallShare.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_share_hint, 0, 0);
        } else {
            mBtnCallShare.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_share, 0, 0);
        }
    }

    @Override
    public void updateCallButtonState(boolean isPSTN){
        if (isPSTN){
            if (mViewModel.mIsAudioMuted){
                mBtnAudio.setText(R.string.title_call_audio_muted);
                mBtnAudio.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_audio_pstn_mute, 0, 0);
            }else{
                mBtnAudio.setText(R.string.title_call_audio_normal);
                mBtnAudio.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_audio_pstn_normal, 0, 0);
            }
        }else{
            if (mViewModel.mIsAudioMuted) {
                mBtnAudio.setText(R.string.title_call_audio_muted);
                mBtnAudio.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_audio_mute, 0, 0);
            } else {
                mBtnAudio.setText(R.string.title_call_audio_normal);
                mBtnAudio.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_audio_normal, 0, 0);
            }
        }
    }
}
