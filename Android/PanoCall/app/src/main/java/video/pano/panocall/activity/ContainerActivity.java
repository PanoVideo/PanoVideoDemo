package video.pano.panocall.activity;

import static video.pano.panocall.info.Constant.FACE_BEAUTY_FRAGMENT;
import static video.pano.panocall.info.Constant.FEED_BACK_FRAGMENT;
import static video.pano.panocall.info.Constant.SOUND_FEED_BACK_FRAGMENT;
import static video.pano.panocall.info.Constant.WEB_PAGE_FRAGMENT;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import video.pano.panocall.R;
import video.pano.panocall.fragment.FaceBeautyFragment;
import video.pano.panocall.fragment.FeedbackFragment;
import video.pano.panocall.fragment.SoundFeedbackFragment;
import video.pano.panocall.fragment.WebPageFragment;

public class ContainerActivity extends AppCompatActivity {

    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_FRAGMENT_NAME = "extra_fragment_name";
    private static final String EXTRA_WEB_URL = "extra_web_url";

    private String mTitle;
    private String mFragmentName;
    private String mWebUrl;

    private Fragment mCurrentFragment ;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        initData();
        initTitleView();
        initViews();
    }

    private void initViews() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_container, mCurrentFragment)
                .addToBackStack(null);
        try {
            ft.show(mCurrentFragment).commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        Intent intent = getIntent();
        if(intent == null){
            return ;
        }
        if(intent.hasExtra(EXTRA_TITLE)){
            mTitle = intent.getStringExtra(EXTRA_TITLE);
        }
        if(intent.hasExtra(EXTRA_FRAGMENT_NAME)){
            mFragmentName = intent.getStringExtra(EXTRA_FRAGMENT_NAME);
        }
        if(intent.hasExtra(EXTRA_WEB_URL)){
            mWebUrl = intent.getStringExtra(EXTRA_WEB_URL);
        }

        switch (mFragmentName){
            case FACE_BEAUTY_FRAGMENT:
                mCurrentFragment = new FaceBeautyFragment();
                break;
            case FEED_BACK_FRAGMENT:
                mCurrentFragment = new FeedbackFragment();
                break;
            case SOUND_FEED_BACK_FRAGMENT:
                mCurrentFragment = new SoundFeedbackFragment();
                break;
            case WEB_PAGE_FRAGMENT:
                mCurrentFragment = new WebPageFragment(mWebUrl);
                break;
            default:
                mCurrentFragment = new Fragment();
                break;
        }
    }

    private void initTitleView() {
        TextView titleView = findViewById(R.id.tv_title);
        ImageView leftIcon = findViewById(R.id.iv_left_icon);

        titleView.setText(mTitle);

        leftIcon.setVisibility(View.VISIBLE);
        leftIcon.setImageResource(R.drawable.svg_icon_back);
        leftIcon.setOnClickListener(view ->
                finish()
        );
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public static void launch(Context context, String fragmentName , String title, String webUrl) {
        Intent intent = new Intent();
        intent.setClass(context, ContainerActivity.class);
        intent.putExtra(EXTRA_FRAGMENT_NAME,fragmentName);
        intent.putExtra(EXTRA_TITLE,title);
        intent.putExtra(EXTRA_WEB_URL,webUrl);
        context.startActivity(intent);
    }

}
