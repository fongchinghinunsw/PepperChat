package com.stephenfong.pepperchat.view.activities.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.databinding.ActivityUserProfileBinding;

import es.dmoral.toasty.Toasty;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityUserProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String receiverID = intent.getStringExtra("userID"); // the user that's using this app
        String userProfile = intent.getStringExtra("profileImage");

        if (receiverID != null) {
            Toast.makeText(this, "entered", Toast.LENGTH_SHORT).show();
            binding.toolbar.setTitle(userName);

            if (userProfile != null) {
                if (userProfile.equals("")) {
                    binding.imageProfile.setImageResource(R.drawable.person_placeholder); // set default image
                } else {
                    Glide.with(this).load(userProfile).into(binding.imageProfile);
                }
            }

        }

        initToolbar();
    }

    private void initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toasty.info(getApplicationContext(), item.getTitle(), Toasty.LENGTH_SHORT, true).show();
        }
        return super.onOptionsItemSelected(item);
    }
}