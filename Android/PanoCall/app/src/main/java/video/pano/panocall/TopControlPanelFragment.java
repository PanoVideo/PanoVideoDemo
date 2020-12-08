package video.pano.panocall;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class TopControlPanelFragment extends Fragment {
    private static final String ARG_ROOM_ID = "arg_room_id";

    private String mRoomId;
    private OnTopControlPanelListener mListener;
    private CallViewModel mViewModel;

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

        mViewModel = new ViewModelProvider(getActivity()).get(CallViewModel.class);

        TextView tvRoomId = view.findViewById(R.id.tv_call_room_id);
        tvRoomId.setText(mRoomId);
        ImageView btnAudioSpeaker = view.findViewById(R.id.btn_call_speaker);
        btnAudioSpeaker.setOnClickListener(v -> {
            mViewModel.mIsAudioSpeakerOpened = !mViewModel.mIsAudioSpeakerOpened;
            if (mViewModel.mIsAudioSpeakerOpened) {
                btnAudioSpeaker.setImageResource(R.drawable.btn_call_speaker_normal);
            } else {
                btnAudioSpeaker.setImageResource(R.drawable.btn_call_speaker_closed);
            }
            if (mListener != null) {
                mListener.onTCPanelAudio(mViewModel.mIsAudioSpeakerOpened);
            }
        });
        if (mViewModel.mIsAudioSpeakerOpened) {
            btnAudioSpeaker.setImageResource(R.drawable.btn_call_speaker_normal);
        } else {
            btnAudioSpeaker.setImageResource(R.drawable.btn_call_speaker_closed);
        }
        view.findViewById(R.id.btn_call_switch_camera).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onTCPanelSwitchCamera();
            }
        });
        view.findViewById(R.id.btn_call_exit).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onTCPanelExit();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnTopControlPanelListener) {
            mListener = (OnTopControlPanelListener) context;
        }
    }


    // listener
    public interface OnTopControlPanelListener {
        void onTCPanelAudio(boolean isSpeaker);
        void onTCPanelSwitchCamera();
        void onTCPanelExit();
    }
}
