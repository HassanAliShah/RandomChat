package com.app.randomchat.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.app.randomchat.R;
import com.app.randomchat.Utils.Utils;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView animationView = findViewById(R.id.lottie_animation);
        animationView.addValueCallback(
                new KeyPath("Dialog 1", "**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        );

        animationView.addValueCallback(
                new KeyPath("Dialog 2", "**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(getColor(R.color.orange), PorterDuff.Mode.SRC_ATOP)
        );
        animationView.addValueCallback(
                new KeyPath("Text 1", "**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(getColor(R.color.quantum_grey400), PorterDuff.Mode.SRC_ATOP)
        );
        animationView.addValueCallback(
                new KeyPath("Text 2", "**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(getColor(R.color.quantum_grey400), PorterDuff.Mode.SRC_ATOP)
        );

        Utils.printHashKey(this);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 5500);
    }
}