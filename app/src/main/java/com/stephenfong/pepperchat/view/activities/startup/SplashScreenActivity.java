package com.stephenfong.pepperchat.view.activities.startup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.view.MainActivity;

public class SplashScreenActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            // A Handler allows you to send and process Message and Runnable objects associated with
            // a thread's MessageQueue (a list of actions to be done). Each Handler instance is associated
            // with a single thread and that thread's message queue. When you create a new Handler it
            // is bound to a Looper. It will deliver messages and runnables to that Looper's message queue
            // and execute them on that Looper's thread. (Default constructor associates this handler
            // with the Looper for the current thread)
            //
            // There are two main uses for a Handler: (1) to schedule messages and runnables to be executed
            // at some point in the future; and (2) to enqueue an action to be performed on a different
            // thread than your own.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    finish();
                }
            }, 3000);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreenActivity.this, WelcomeScreenActivity.class));
                    finish();
                }
            }, 3000);
        }

    }
}