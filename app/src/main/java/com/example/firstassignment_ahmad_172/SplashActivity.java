package com.example.firstassignment_ahmad_172;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class SplashActivity extends Activity {

    private ImageView imgLogo;
    private static final int ANIMATION_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imgLogo = findViewById(R.id.imgLogo);

        animateLogo();

        // Start MainActivity after animation
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, ANIMATION_DURATION + 1000); // Slight delay for better effect
    }

    private void animateLogo() {
        ObjectAnimator fadeZoomIn = ObjectAnimator.ofPropertyValuesHolder(
                imgLogo,
                PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1.2f, 1f), // Scale effect
                PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1.2f, 1f),
                PropertyValuesHolder.ofFloat("alpha", 0f, 1f) // Fade-in effect
        );
        fadeZoomIn.setDuration(ANIMATION_DURATION);
        fadeZoomIn.start();
    }
}
