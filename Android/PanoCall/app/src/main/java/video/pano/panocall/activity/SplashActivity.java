package video.pano.panocall.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import video.pano.panocall.R;

public class SplashActivity extends AppCompatActivity {

    private static final int DELAY_TIME = 1200;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        jump2Main();
    }


    private void jump2Main(){
        mHandler.postDelayed(()->{
            MainActivity.launch(SplashActivity.this);
            finish();
        },DELAY_TIME);
    }
}
