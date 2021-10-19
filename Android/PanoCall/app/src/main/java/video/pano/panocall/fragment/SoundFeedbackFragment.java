package video.pano.panocall.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.info.UserManager;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.rtc.PanoRtcEngine;
import video.pano.panocall.utils.Utils;

import static video.pano.panocall.info.Constant.KEY_SOUND_FEEDBACK_COMMAND;
import static video.pano.panocall.info.Constant.KEY_SOUND_FEEDBACK_DESCRIPTION;
import static video.pano.panocall.info.Constant.KEY_SOUND_FEEDBACK_TYPE;
import static video.pano.panocall.info.Constant.VALUE_SOUND_FEEDBACK_COMMAND_STARTDUMP;
import static video.pano.panocall.info.Constant.VALUE_SOUND_FEEDBACK_TYPE_COMMAND;

public class SoundFeedbackFragment extends BaseSettingFragment {

    private static final int kMaxDescriptionLength = 300;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sound_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView countText = view.findViewById(R.id.tv_count);
        countText.setText(getString(R.string.title_text_count_watch,kMaxDescriptionLength));

        EditText editDesc = view.findViewById(R.id.edit_feedback_desc);
        editDesc.requestFocus();

        editDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = 0;
                if (s != null) {
                    length = s.toString().length();
                }
                int remainingCount = kMaxDescriptionLength - length ;
                countText.setText(getString(R.string.title_text_count_watch,remainingCount));
            }
        });

        view.findViewById(R.id.btn_feedback).setOnClickListener(v -> {
            String desc = editDesc.getText().toString();
            if (desc.isEmpty()) {
                Toast.makeText(getActivity(), R.string.msg_feedback_desc_empty, Toast.LENGTH_LONG).show();
                return;
            }
            UserInfo localUser = UserManager.getIns().getLocalUser();
            if(localUser != null && !TextUtils.isEmpty(localUser.userName)){
                desc = "'"+localUser.userName+"' 一键上报："+desc;
            }
            doFeedback(desc);
            if(getActivity() != null){
                getActivity().finish();
            }
        });
    }

    private void doFeedback(String desc) {
        if (desc.length() > kMaxDescriptionLength) {
            desc = desc.substring(0, kMaxDescriptionLength);
        }
        PanoApplication app = (PanoApplication) Utils.getApp();
        if (app.mFeedbackRoomId == null || app.mFeedbackRoomId.isEmpty()) {
            return;
        }
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(KEY_SOUND_FEEDBACK_COMMAND,VALUE_SOUND_FEEDBACK_COMMAND_STARTDUMP);
            jsonObject.put(KEY_SOUND_FEEDBACK_DESCRIPTION,desc);
            jsonObject.put(KEY_SOUND_FEEDBACK_TYPE,VALUE_SOUND_FEEDBACK_TYPE_COMMAND);
            PanoRtcEngine.getIns().getPanoMessageService().broadcastMessage(jsonObject.toString().getBytes(),true);
            Toast.makeText(getActivity(), R.string.msg_feedback_report_done, Toast.LENGTH_LONG).show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
