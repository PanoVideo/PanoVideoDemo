package video.pano.panocall.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.pano.rtc.api.PanoAnnotationManager;

import video.pano.panocall.R;
import video.pano.panocall.adapter.StatisticsAdapter;
import video.pano.panocall.fragment.StatisticsAudioFragment;
import video.pano.panocall.fragment.StatisticsOverallFragment;
import video.pano.panocall.fragment.StatisticsScreenFragment;
import video.pano.panocall.fragment.StatisticsVideoFragment;
import video.pano.panocall.rtc.PanoRtcEngine;

public class StatisticsActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{

    private ViewPager2 mContentPager;
    private PagerChangeCallback mPagerChangeCallback;
    private RadioGroup mTitleGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        initTitleView();
        initViews();
    }

    private void initViews() {
        mTitleGroup = findViewById(R.id.rg_stat);
        mTitleGroup.setOnCheckedChangeListener(this);

        mContentPager = findViewById(R.id.pager_content);

        StatisticsAdapter adapter = new StatisticsAdapter(this);
        adapter.addFragment(StatisticsOverallFragment.newInstance());
        adapter.addFragment(StatisticsAudioFragment.newInstance());
        adapter.addFragment(StatisticsVideoFragment.newInstance());
        adapter.addFragment(StatisticsScreenFragment.newInstance());
        mContentPager.setAdapter(adapter);
        mContentPager.setCurrentItem(0);

        mPagerChangeCallback = new PagerChangeCallback();
        mContentPager.registerOnPageChangeCallback(mPagerChangeCallback);
    }

    private void initTitleView() {
        TextView titleView = findViewById(R.id.tv_title);
        titleView.setText(R.string.title_statistics);

        ImageView leftIcon = findViewById(R.id.iv_left_icon);
        leftIcon.setVisibility(View.VISIBLE);
        leftIcon.setImageResource(R.drawable.svg_icon_back);
        leftIcon.setOnClickListener(v ->
                finish()
        );
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId == R.id.rb_stat_overall){
            mContentPager.setCurrentItem(0);
        }else if(checkedId == R.id.rb_stat_audio){
            mContentPager.setCurrentItem(1);
        }else if(checkedId == R.id.rb_stat_video){
            mContentPager.setCurrentItem(2);
        }else if(checkedId == R.id.rb_stat_screen){
            mContentPager.setCurrentItem(3);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mContentPager != null && mPagerChangeCallback != null){
            mContentPager.unregisterOnPageChangeCallback(mPagerChangeCallback);
            mPagerChangeCallback = null ;
        }
    }


    class PagerChangeCallback extends ViewPager2.OnPageChangeCallback{
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            switch (position){
                case 0 :
                    mTitleGroup.check(R.id.rb_stat_overall);
                    break;
                case 1:
                    mTitleGroup.check(R.id.rb_stat_audio);
                    break;
                case 2 :
                    mTitleGroup.check(R.id.rb_stat_video);
                    break;
                case 3:
                    mTitleGroup.check(R.id.rb_stat_screen);
                    break;
                default:
                    break;
            }
        }
    }

    public static void launch(Context context){
        Intent intent = new Intent(context,StatisticsActivity.class);
        context.startActivity(intent);
    }
}
