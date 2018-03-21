package com.shrinivas.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

//Class to display Splash screen
public class SplashActivity extends AppCompatActivity {

    //reference - https://stackoverflow.com/questions/5486789/how-do-i-make-a-splash-screen

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 7500;
    TextView welcomeTextView;
    ImageView welImageView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        welImageView = findViewById(R.id.welcomeView);
        welcomeTextView = findViewById(R.id.welcomeTextId);

        welcomeTextView.setText(R.string.app_name);
        Glide.with(this)
                .load(R.mipmap.welcome_image)
                .into(welImageView);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent intent = new Intent(getBaseContext(), ShowWeatherActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}

