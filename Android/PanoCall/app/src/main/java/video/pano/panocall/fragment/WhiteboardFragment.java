package video.pano.panocall.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.PanoCoursePageView;
import com.pano.rtc.api.RtcWbView;
import com.pano.rtc.api.RtcWhiteboard;

import org.json.JSONObject;

import java.util.List;

import video.pano.panocall.R;
import video.pano.panocall.adapter.WBItemAdapter;
import video.pano.panocall.info.FillColor;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.model.UserViewInfo;
import video.pano.panocall.utils.AnnotationHelper;

import static video.pano.panocall.info.Constant.MSG_KEY;


public class WhiteboardFragment extends CallFragment {

    private static final int FROM_TEXT = 0;
    private static final int FROM_PENCIL = 1;
    private static final int FROM_GRAPHICS = 2;
    private static final int FROM_TOP = 3;
    private static final int FROM_OTHER = 4;

    private RtcWhiteboard mRtcWhiteboard;

    private TextView mTvPageName;
    private TextView mTvPageSpeaker;
    private TextView mTextPageNum;
    private TextView mTextPageScale;
    private TextView mTvPencilInt;
    private TextView mTvTextInt;
    private SeekBar mPencilSeek;
    private SeekBar mTextSeek;
    private RadioGroup mPencilColorGroup;
    private RadioGroup mTextColorGroup;
    private RadioGroup mGraphicsColorGroup;
    private RadioGroup mGraphicsToolsGroup;

    private WBItemAdapter mWbItemAdapter;

    private View mBottomPopupView;
    private View mTopPopupView;
    private View mPencilPopupView;
    private View mGraphicsPopupView;
    private View mTextPopupView;
    private View mAdminView;
    private View mAttendeeView;

    private Constants.WBToolType mToolType = Constants.WBToolType.None;
    private Constants.WBRoleType mRoleType = Constants.WBRoleType.Admin;
    private Constants.WBFillType mFillType = Constants.WBFillType.None;

    private FillColor mTextFillColor = FillColor.Black;
    private FillColor mPencilFillColor = FillColor.Black;
    private FillColor mGraphicsFillColor = FillColor.Black;

    private int mLineWidth = 10;
    private int mFontSize = 36;

    private int mTextColorId = R.id.rb_text_color_black;
    private int mPencilColorId = R.id.rb_pencil_color_black;
    private int mGraphicsColorId = R.id.rb_graphics_color_black;
    private int mGraphicsToolsId = R.id.rb_graphics_line;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_whiteboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRtcWhiteboard = mViewModel.rtcEngine().getWhiteboard();

        initUserInfo();
        initTopToolbarPopup(view);
        initBottomGraphicsPopup(view);
        initBottomPencilPopup(view);
        initBottomTextPopup(view);

        // top tools bar
        initTopToolsBar(view);

        //bottom tools bar
        initBottomToolsBar(view);

        initCoursePageView(view);

    }

    private void initUserInfo() {
        new Handler().postDelayed(()->{
            if(mViewModel.getUserManager().getHostUserId() == 0L){
                mRoleType = Constants.WBRoleType.Admin;
                mRtcWhiteboard.setRoleType(Constants.WBRoleType.Admin);
            }else{
                mRoleType = Constants.WBRoleType.Attendee;
            }
        },1000);
    }

    private void initCoursePageView(View view){
        PanoCoursePageView panoCoursePageView = view.findViewById(R.id.pano_course_page_view);
        RtcWbView webView = panoCoursePageView.getAttachRtcWbView();
        webView.setTransparent(true);
        mRtcWhiteboard.open(webView);
    }

    private void initTopToolsBar(View rootView) {
        mTextPageNum = rootView.findViewById(R.id.tv_page_num);
        mTextPageScale = rootView.findViewById(R.id.tv_page_percent);

        setPageNum(mRtcWhiteboard.getCurrentPageNumber(), mRtcWhiteboard.getTotalNumberOfPages());
        setPageScale(mRtcWhiteboard.getCurrentScaleFactor());

        rootView.findViewById(R.id.tv_close_wb).setOnClickListener(view1 -> {
            leaveWhiteboard();
        });
        rootView.findViewById(R.id.cl_page_tools).setOnClickListener(view1 -> {
            showOrHideOtherPopupView(FROM_TOP);
            showOrHideTopPopupView();
        });
        rootView.findViewById(R.id.img_page_undo).setOnClickListener(view -> {
            showOrHideOtherPopupView(FROM_OTHER);
            mRtcWhiteboard.undo();
        });
        rootView.findViewById(R.id.img_page_redo).setOnClickListener(view -> {
            showOrHideOtherPopupView(FROM_OTHER);
            mRtcWhiteboard.redo();
        });
    }

    private void initTopToolbarPopup(View rootView) {
        mTopPopupView = rootView.findViewById(R.id.cl_top_toolbar_popup_view);
        mTopPopupView.setOnClickListener(v ->  showOrHideOtherPopupView(FROM_OTHER));

        mAdminView = rootView.findViewById(R.id.ll_wb_admin_view);
        mAttendeeView = rootView.findViewById(R.id.ll_wb_attendee_view);

        mTvPageName = rootView.findViewById(R.id.tv_page_name);
        mTvPageSpeaker = rootView.findViewById(R.id.tv_page_speaker);

        RecyclerView rvWbList = rootView.findViewById(R.id.rv_wb_list);
        rvWbList.setLayoutManager(new LinearLayoutManager(getContext()));
        mWbItemAdapter = new WBItemAdapter(getContext());
        rvWbList.setAdapter(mWbItemAdapter);

        mWbItemAdapter.setOnItemClickListener(wbDocId -> checkWbDoc(wbDocId,true));

        rootView.findViewById(R.id.tv_page_apply_show).setOnClickListener(view1 -> {
            mRtcWhiteboard.setRoleType(Constants.WBRoleType.Admin);
            mAdminView.setVisibility(View.VISIBLE);
            mAttendeeView.setVisibility(View.GONE);
        });
        rootView.findViewById(R.id.tv_page_prev).setOnClickListener(view1 -> mRtcWhiteboard.prevPage());
        rootView.findViewById(R.id.tv_page_next).setOnClickListener(view1 -> mRtcWhiteboard.nextPage());
        rootView.findViewById(R.id.tv_page_add).setOnClickListener(view1 -> mRtcWhiteboard.addPage(true));
        rootView.findViewById(R.id.tv_page_remove).setOnClickListener(view1 -> {
            int cpn = mRtcWhiteboard.getCurrentPageNumber();
            mRtcWhiteboard.removePage(cpn);
        });
    }

    private void initBottomToolsBar(View rootView) {
        rootView.findViewById(R.id.rb_toolbar_graphics).setOnClickListener(v -> {
            showOrHideOtherPopupView(FROM_GRAPHICS);
            showOrHideGraphicsPopupView();
        });

        rootView.findViewById(R.id.rb_toolbar_pencil).setOnClickListener(v -> {
            showOrHideOtherPopupView(FROM_PENCIL);
            showOrHidePencilPopupView();
        });

        rootView.findViewById(R.id.rb_toolbar_text).setOnClickListener(v -> {
            showOrHideOtherPopupView(FROM_TEXT);
            showOrHideTextPopupView();
        });

        rootView.findViewById(R.id.rb_toolbar_delete).setOnClickListener(v -> {
            showOrHideOtherPopupView(FROM_OTHER);
            selectToolType(Constants.WBToolType.Eraser);
        });

        rootView.findViewById(R.id.rb_toolbar_select).setOnClickListener(v -> {
            showOrHideOtherPopupView(FROM_OTHER);
            selectToolType(Constants.WBToolType.Select);
        });

        mBottomPopupView = rootView.findViewById(R.id.cl_bottom_toolbar_popup_view);
        mBottomPopupView.setOnClickListener(v ->
                showOrHideOtherPopupView(FROM_OTHER)
        );
    }

    private void initBottomPencilPopup(View rootView) {
        mPencilPopupView = rootView.findViewById(R.id.cl_wb_pencil_popup_view);
        mTvPencilInt = rootView.findViewById(R.id.tv_pencil_stroke_intensity);
        mPencilColorGroup = rootView.findViewById(R.id.radio_pencil_color_group);
        mPencilSeek = rootView.findViewById(R.id.seekBar_pencil_stroke_intensity);

        mPencilColorGroup.setOnCheckedChangeListener((group, checkedId) -> {

            selectToolType(mToolType);

            if (checkedId == R.id.rb_pencil_color_black) {
                setColor(FillColor.Black, R.id.rb_pencil_color_black, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_red) {
                setColor(FillColor.Red, R.id.rb_pencil_color_red, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_orange) {
                setColor(FillColor.Orange, R.id.rb_pencil_color_orange, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_yellow) {
                setColor(FillColor.Yellow, R.id.rb_pencil_color_yellow, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_green) {
                setColor(FillColor.Green, R.id.rb_pencil_color_green, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_turquoise) {
                setColor(FillColor.Turquoise, R.id.rb_pencil_color_turquoise, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_azure) {
                setColor(FillColor.Azure, R.id.rb_pencil_color_azure, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_blue) {
                setColor(FillColor.Blue, R.id.rb_pencil_color_blue, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_purple) {
                setColor(FillColor.Purple, R.id.rb_pencil_color_purple, FROM_PENCIL);
            }
        });

        mPencilSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mLineWidth = progress + 1;
                mTvPencilInt.setText(String.valueOf(mLineWidth));
                mRtcWhiteboard.setLineWidth(mLineWidth);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initBottomTextPopup(View rootView) {
        mTextPopupView = rootView.findViewById(R.id.cl_wb_text_popup_view);
        mTvTextInt = rootView.findViewById(R.id.tv_text_stroke_intensity);
        mTextColorGroup = rootView.findViewById(R.id.radio_text_color_group);
        mTextSeek = rootView.findViewById(R.id.seekBar_text_stroke_intensity);

        mTextColorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectToolType(mToolType);

            if (checkedId == R.id.rb_text_color_black) {
                setColor(FillColor.Black, R.id.rb_text_color_black, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_red) {
                setColor(FillColor.Red, R.id.rb_text_color_red, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_orange) {
                setColor(FillColor.Orange, R.id.rb_text_color_orange, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_yellow) {
                setColor(FillColor.Yellow, R.id.rb_text_color_yellow, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_green) {
                setColor(FillColor.Green, R.id.rb_text_color_green, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_turquoise) {
                setColor(FillColor.Turquoise, R.id.rb_text_color_turquoise, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_azure) {
                setColor(FillColor.Azure, R.id.rb_text_color_azure, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_blue) {
                setColor(FillColor.Blue, R.id.rb_text_color_blue, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_purple) {
                setColor(FillColor.Purple, R.id.rb_text_color_purple, FROM_TEXT);
            }
        });

        mTextSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFontSize = progress + 10;
                mTvTextInt.setText(String.valueOf(mFontSize));
                mRtcWhiteboard.setFontSize(mFontSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initBottomGraphicsPopup(View rootView) {
        mGraphicsPopupView = rootView.findViewById(R.id.cl_wb_graphics_popup_view);

        mGraphicsToolsGroup = rootView.findViewById(R.id.radio_graphics_tools_group);
        mGraphicsToolsGroup.setOnCheckedChangeListener((group,checkedId) -> {

            mGraphicsToolsId = checkedId ;

            if(checkedId == R.id.rb_graphics_line){
                selectToolType(Constants.WBToolType.Line);
            }else if(checkedId == R.id.rb_hollow_circle){
                selectToolType(Constants.WBToolType.Ellipse);
            }else if(checkedId == R.id.rb_hollow_square){
                selectToolType(Constants.WBToolType.Rect);
            }else if(checkedId == R.id.rb_solid_circle){
                selectToolType(Constants.WBToolType.Ellipse, Constants.WBFillType.Color);
            }else if(checkedId == R.id.rb_solid_square){
                selectToolType(Constants.WBToolType.Rect, Constants.WBFillType.Color);
            }
            setColor(mGraphicsFillColor,mGraphicsColorId,FROM_GRAPHICS);
        });

        mGraphicsColorGroup = rootView.findViewById(R.id.radio_graphics_color_group);
        mGraphicsColorGroup.setOnCheckedChangeListener((group, checkedId) -> {

            selectToolType(mToolType, mFillType);

            if (checkedId == R.id.rb_graphics_color_black) {
                setColor(FillColor.Black, R.id.rb_graphics_color_black, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_red) {
                setColor(FillColor.Red, R.id.rb_graphics_color_red, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_orange) {
                setColor(FillColor.Orange, R.id.rb_graphics_color_orange, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_yellow) {
                setColor(FillColor.Yellow, R.id.rb_graphics_color_yellow, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_green) {
                setColor(FillColor.Green, R.id.rb_graphics_color_green, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_turquoise) {
                setColor(FillColor.Turquoise, R.id.rb_graphics_color_turquoise, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_azure) {
                setColor(FillColor.Azure, R.id.rb_graphics_color_azure, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_blue) {
                setColor(FillColor.Blue, R.id.rb_graphics_color_blue, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_purple) {
                setColor(FillColor.Purple, R.id.rb_graphics_color_purple, FROM_GRAPHICS);
            }
        });
    }

    private void showOrHideTopPopupView() {
        int v = mTopPopupView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        mTopPopupView.setVisibility(v);

        if (v == View.VISIBLE) {

            String speakerName = "";
            if (mRoleType == Constants.WBRoleType.Admin) {
                UserInfo localUser = mViewModel.getUserManager().getLocalUser();
                speakerName = localUser.getUserName();
                mAdminView.setVisibility(View.VISIBLE);
                mAttendeeView.setVisibility(View.GONE);
            } else {
                UserInfo adminUser = mViewModel.getUserManager()
                        .getWhiteboardUser(mViewModel.getUserManager().getHostUserId());
                speakerName = adminUser.getUserName();
                mAttendeeView.setVisibility(View.VISIBLE);
                mAdminView.setVisibility(View.GONE);
            }

            SpannableStringBuilder presenterBuilder = packageText(getString(R.string.title_wb_current_presenter),speakerName);
            mTvPageSpeaker.setText(presenterBuilder);

            refreshWbList();
        }
    }

    private void showOrHidePencilPopupView() {
        int v = mPencilPopupView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        mPencilPopupView.setVisibility(v);

        if (v == View.VISIBLE) {
            mTvPencilInt.setText(String.valueOf(mLineWidth));
            mPencilSeek.setProgress(mLineWidth);
            mPencilColorGroup.check(mPencilColorId);

            mRtcWhiteboard.setLineWidth(mLineWidth);
            selectToolType(Constants.WBToolType.Path);
        }
    }

    private void showOrHideTextPopupView() {
        int v = mTextPopupView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        mTextPopupView.setVisibility(v);

        if (v == View.VISIBLE) {
            mTvTextInt.setText(String.valueOf(mFontSize));
            mTextSeek.setProgress(mFontSize);
            mTextColorGroup.check(mTextColorId);

            mRtcWhiteboard.setFontSize(mFontSize);
            selectToolType(Constants.WBToolType.Text);
        }
    }

    private void showOrHideGraphicsPopupView() {
        int v = mGraphicsPopupView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        mGraphicsPopupView.setVisibility(v);
        if (v == View.VISIBLE) {
            mGraphicsColorGroup.check(mGraphicsColorId);
            mGraphicsToolsGroup.check(mGraphicsToolsId);
            mRtcWhiteboard.setLineWidth(10);
        }
    }

    private void selectToolType(Constants.WBToolType toolType) {
        selectToolType(toolType, Constants.WBFillType.None);
    }

    private void selectToolType(Constants.WBToolType toolType, Constants.WBFillType fillType) {
        mFillType = fillType ;
        mToolType = toolType;
        mRtcWhiteboard.setToolType(toolType);
        mRtcWhiteboard.setFillType(fillType);
    }
    
    private void setColor(FillColor fillColor, int id, int from){
        if(mFillType == Constants.WBFillType.Color){
            mRtcWhiteboard.setFillColor(fillColor.getValue());
        }else{
            mRtcWhiteboard.setForegroundColor(fillColor.getValue());
        }
        switch (from) {
            case FROM_GRAPHICS:
                mGraphicsFillColor = fillColor;
                mGraphicsColorId = id;
                break;
            case FROM_PENCIL:
                mPencilFillColor = fillColor;
                mPencilColorId = id;
                break;
            case FROM_TEXT:
                mTextFillColor = fillColor;
                mTextColorId = id;
                break;
        }
    }

    private void showOrHideOtherPopupView(int from) {
        switch (from) {
            case FROM_GRAPHICS:
                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.VISIBLE);
                if (mTopPopupView != null) mTopPopupView.setVisibility(View.GONE);
                if (mPencilPopupView != null) mPencilPopupView.setVisibility(View.GONE);
                if (mTextPopupView != null) mTextPopupView.setVisibility(View.GONE);
                break;
            case FROM_PENCIL:
                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.VISIBLE);
                if (mTopPopupView != null) mTopPopupView.setVisibility(View.GONE);
                if (mTextPopupView != null) mTextPopupView.setVisibility(View.GONE);
                if (mGraphicsPopupView != null) mGraphicsPopupView.setVisibility(View.GONE);
                break;
            case FROM_TEXT:
                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.VISIBLE);
                if (mTopPopupView != null) mTopPopupView.setVisibility(View.GONE);
                if (mPencilPopupView != null) mPencilPopupView.setVisibility(View.GONE);
                if (mGraphicsPopupView != null) mGraphicsPopupView.setVisibility(View.GONE);
                break;
            case FROM_TOP:
                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.GONE);
                if (mPencilPopupView != null) mPencilPopupView.setVisibility(View.GONE);
                if (mTextPopupView != null) mTextPopupView.setVisibility(View.GONE);
                if (mGraphicsPopupView != null) mGraphicsPopupView.setVisibility(View.GONE);
                break;
            case FROM_OTHER:
            default:
                if (mTopPopupView != null) mTopPopupView.setVisibility(View.GONE);

                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.GONE);
                if (mPencilPopupView != null) mPencilPopupView.setVisibility(View.GONE);
                if (mTextPopupView != null) mTextPopupView.setVisibility(View.GONE);
                if (mGraphicsPopupView != null) mGraphicsPopupView.setVisibility(View.GONE);
                break;
        }
    }

    private void leaveWhiteboard(){
        showOrHideOtherPopupView(FROM_OTHER);
        mRtcWhiteboard.close();
        NavHostFragment.findNavController(WhiteboardFragment.this).navigateUp();
    }

    private SpannableStringBuilder packageText(String start , String end){
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        SpannableString key = new SpannableString(start);
        key.setSpan(new ForegroundColorSpan(Color.parseColor("#999999")),0,key.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString value = new SpannableString(end);
        value.setSpan(new ForegroundColorSpan(Color.parseColor("#333333")),0,value.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        ssb.append(key).append("  ").append(value);

        return ssb ;
    }

    private void refreshWbList(){
        String currentWBId = mRtcWhiteboard.getCurrentWhiteboardId();
        List<String> wbFiles = mRtcWhiteboard.enumerateFiles();

        if(!TextUtils.isEmpty(currentWBId)){
            String docName = getString(R.string.title_wb_default_wb);
            if(!currentWBId.contains("default")){
                int size = wbFiles.size();
                for(int i = 0 ; i < size ; i ++){
                    if(currentWBId.equals(wbFiles.get(i))){
                        int pageNum = i - 1;
                        docName = getString(R.string.title_wb_default_doc,pageNum <=0 ? 1 : i);
                    }
                }
            }
            SpannableStringBuilder pageNameBuilder = packageText(getString(R.string.title_wb_current_name),docName);
            mTvPageName.setText(pageNameBuilder);
        }

        mWbItemAdapter.setData(wbFiles,currentWBId);
    }

    private void checkWbDoc(String checkWbId , boolean needSwitch){
        if(TextUtils.isEmpty(checkWbId)) return ;
        if(checkWbId.equals(mRtcWhiteboard.getCurrentFileId())){
            return ;
        }

        String docName = getString(R.string.title_wb_default_wb);
        List<String> wbFiles = mRtcWhiteboard.enumerateFiles();

        if(!checkWbId.contains("default")){
            int size = wbFiles.size();
            for(int i = 0 ; i < size ; i ++){
                if(checkWbId.equals(wbFiles.get(i))){
                    int pageNum = i - 1;
                    docName = getString(R.string.title_wb_default_doc,pageNum <=0 ? 1 : i);
                }
            }
        }
        SpannableStringBuilder pageNameBuilder = packageText(getString(R.string.title_wb_current_name),docName);
        mTvPageName.setText(pageNameBuilder);

        mWbItemAdapter.setCheckWbId(checkWbId);
        if(needSwitch) mRtcWhiteboard.switchDoc(checkWbId);
    }

    // -------------------------- PANO Whiteboard Handler --------------------------
    @Override
    public void onPageNumberChanged(int curPage, int totalPages) {
        setPageNum(curPage, totalPages);
    }

    @Override
    public void onViewScaleChanged(float scale) {
        setPageScale(scale);
    }

    @Override
    public void onRoleTypeChanged(Constants.WBRoleType newRole) {
        mRoleType = newRole;
        if(newRole == Constants.WBRoleType.Admin){
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(MSG_KEY, mViewModel.getLocalUserId());
                mRtcWhiteboard.broadcastMessage(jsonObject.toString().getBytes());

                mAdminView.setVisibility(View.VISIBLE);
                mAttendeeView.setVisibility(View.GONE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            mAdminView.setVisibility(View.GONE);
            mAttendeeView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUserJoined(long userId, String userName) {
        if(userId == mViewModel.getLocalUserId()){
            return ;
        }
        if(mRoleType == Constants.WBRoleType.Admin){
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(MSG_KEY, mViewModel.getLocalUserId());
                mRtcWhiteboard.sendMessage(userId,jsonObject.toString().getBytes());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onWhiteboardStop() {
        leaveWhiteboard();
    }

    @Override
    public void onDeleteDoc(Constants.QResult result, String fileId) {
        if(result == Constants.QResult.OK){
            refreshWbList();
        }
    }

    @Override
    public void onSwitchDoc(Constants.QResult result, String fileId) {
        if(result == Constants.QResult.OK){
            checkWbDoc(fileId,false);
        }
    }

    @Override
    public void onCreateDoc(Constants.QResult result, String fileId) {
        if(result == Constants.QResult.OK){
            refreshWbList();
        }
    }

    // -------------------------- CallViewModel.CallEventHandler --------------------------

    private void setPageNum(int curPage, int totalPages) {
        mTextPageNum.setText(curPage + "/" + totalPages);
    }

    private void setPageScale(float scale) {
        int scale100 = (int) (scale * 100);
        mTextPageScale.setText(scale100 + "%");
    }

    /***************************Indication Start*******************************************/
    @Override
    public void onUserLeaveIndication(UserInfo user, Constants.UserLeaveReason reason) {
        //Local View refresh
        if (mUserViewArray[0].userId == user.userId) {
            switchUserViewIndex();
            //Remote View refresh
        }
    }

    /***************************Indication End*******************************************/

    private void switchUserViewIndex() {
        clearViewRender(mUserViewArray[0]);
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        subscribeLocalVideo(mUserViewArray[0], localUser);
        updateUserAudioState(0);
    }

    private void subscribeLocalVideo(UserViewInfo userViewInfo, UserInfo userInfo) {
        userViewInfo.setUser(userInfo.userId, userInfo.userName,mViewModel.getUserManager().isMySelf(userInfo.userId));
        userViewInfo.setVisible(true);
        userViewInfo.isFree = false;
        userViewInfo.isScreen = false;
        userViewInfo.isSubscribed = false;
        updateLocalVideoRender(userViewInfo.rtcView, true);
    }

    private void clearViewRender(UserViewInfo userViewInfo) {
        if (userViewInfo == null) {
            return;
        }
        UserInfo localUser = mViewModel.getUserManager().getLocalUser();
        if (userViewInfo.userId == localUser.userId) {
            clearLocalVideoRender();
        } else if (userViewInfo.isScreen) {
            clearRemoteScreenRender(userViewInfo.userId);
        } else {
            clearRemoteVideoRender(userViewInfo.userId);
        }
    }

    // video view
    @Override
    void setupUserViewArray() {
        // 初始化宫格视图数组，目前支持四宫格
        initUserViewArray(1);

        View view = getView();
        if (view != null) {
            // 配置视图1参数, 此视图一般用于显示本地用户视频
            mUserViewArray[0].initView(
                    view.findViewById(R.id.small_view_righttop),
                    true,
                    view.findViewById(R.id.tv_small_view_righttop_user),
                    view.findViewById(R.id.img_small_view_righttop_audio),
                    view.findViewById(R.id.img_small_view_default_head),
                    view.findViewById(R.id.img_small_view_righttop_signal),
                    view.findViewById(R.id.cl_small_view_righttop));

        }

        initUserVideoView(0, false);
    }

}
