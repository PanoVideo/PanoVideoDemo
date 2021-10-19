package video.pano.panocall.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

import video.pano.panocall.fragment.StatisticsAudioFragment;
import video.pano.panocall.fragment.StatisticsOverallFragment;
import video.pano.panocall.fragment.StatisticsScreenFragment;
import video.pano.panocall.fragment.StatisticsVideoFragment;

public class StatisticsAdapter extends FragmentStateAdapter {

    private List<Fragment> mFragments = new ArrayList<>();

    public StatisticsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void addFragment(Fragment fragment) {
        mFragments.add(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }
}
