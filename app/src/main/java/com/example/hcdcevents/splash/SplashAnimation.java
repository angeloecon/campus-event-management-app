package com.example.hcdcevents.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hcdcevents.R;
import com.example.hcdcevents.feature.auth.SignInActivity;

public class SplashAnimation extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_animation);

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashAnimation.this, SignInActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        };
        handler.postDelayed(runnable, 4500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
