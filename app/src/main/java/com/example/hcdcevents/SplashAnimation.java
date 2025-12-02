package com.example.hcdcevents;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hcdcevents.feature.auth.SignInActivity;
import com.example.hcdcevents.ui.home.HomePageActivity;

public class SplashAnimation extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_animation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        runnable = new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(SplashAnimation.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        };


        handler.postDelayed(runnable, 4000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
