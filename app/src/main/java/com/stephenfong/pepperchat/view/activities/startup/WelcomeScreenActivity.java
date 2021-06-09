package com.stephenfong.pepperchat.view.activities.startup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.view.activities.auth.PhoneLoginActivity;

public class WelcomeScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        Button btnAgree = findViewById(R.id.btn_agree);
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeScreenActivity.this, PhoneLoginActivity.class));
                // we don't finish the activity because we want the user to be able to come back,
                // but once the user is logged in, we will clear all the activities on the stack.
            }
        });
    }
}