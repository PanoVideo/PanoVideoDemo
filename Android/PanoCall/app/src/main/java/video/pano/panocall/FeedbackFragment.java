package video.pano.panocall;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcEngine;


public class FeedbackFragment extends Fragment {
    private static final int kMaxDescriptionLength = 300;
    private static final int kMaxContactLength = 100;

    private int mFeedbackType = -1;

    public FeedbackFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFeedbackType = -1;

        view.findViewById(R.id.radio_feedback_type_audio).setOnClickListener(v->{
            mFeedbackType = 1;
        });
        view.findViewById(R.id.radio_feedback_type_video).setOnClickListener(v->{
            mFeedbackType = 2;
        });
        view.findViewById(R.id.radio_feedback_type_whiteboard).setOnClickListener(v->{
            mFeedbackType = 3;
        });
        view.findViewById(R.id.radio_feedback_type_general).setOnClickListener(v->{
            mFeedbackType = 0;
        });

        EditText editDesc = view.findViewById(R.id.edit_feedback_desc);
        EditText editContact = view.findViewById(R.id.edit_feedback_contact);
        Switch switchUploadLog = view.findViewById(R.id.switch_feedback_upload_log);
        editDesc.setFilters(new InputFilter[] { new InputFilter.LengthFilter(kMaxDescriptionLength) });
        editContact.setFilters(new InputFilter[] { new InputFilter.LengthFilter(kMaxDescriptionLength) });
        switchUploadLog.setChecked(true);

        view.findViewById(R.id.btn_feedback).setOnClickListener(v -> {
            String desc = editDesc.getText().toString();
            if (desc.isEmpty()) {
                Toast.makeText(getActivity(), R.string.msg_feedback_desc_empty, Toast.LENGTH_LONG).show();
                return;
            }
            String contact = editContact.getText().toString();
            boolean enableUploatLog = switchUploadLog.isChecked();
            doFeedback(mFeedbackType, desc, contact, enableUploatLog);
            getActivity().onBackPressed();
        });

        editDesc.requestFocus();
    }

    private void doFeedback(int type, String desc, String contact, boolean enableUploadLog) {
        if (desc.length() > kMaxDescriptionLength) {
            desc = desc.substring(0, kMaxDescriptionLength);
        }
        if (contact.length() > kMaxContactLength) {
            contact = contact.substring(0, kMaxContactLength);
        }
        PanoApplication app = (PanoApplication) getActivity().getApplication();
        if (app.mFeedbackRoomId == null || app.mFeedbackRoomId.isEmpty()) {
            return;
        }
        RtcEngine.FeedbackInfo info = new RtcEngine.FeedbackInfo();
        switch (type) {
            case 1:
                info.type = Constants.FeedbackType.Audio;
                break;
            case 2:
                info.type = Constants.FeedbackType.Video;
                break;
            case 3:
                info.type = Constants.FeedbackType.Whiteboard;
                break;
            case 4:
                info.type = Constants.FeedbackType.Screen;
                break;
            default:
                info.type = Constants.FeedbackType.General;
                break;
        }
        info.productName = "PanoVideoDemo";
        info.description = desc;
        info.contact = contact;
        info.extraInfo = app.getAppUuid();
        info.uploadLogs = enableUploadLog;
        app.getPanoEngine().sendFeedback(info);
        Toast.makeText(getActivity(), R.string.msg_feedback_report_done, Toast.LENGTH_LONG).show();
    }
}
