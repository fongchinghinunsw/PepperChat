package com.stephenfong.pepperchat.view.activities.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.databinding.ActivitySettingsBinding;
import com.stephenfong.pepperchat.view.activities.profile.ProfileActivity;

import es.dmoral.toasty.Toasty;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            getInfo();
        }

        initClickAction();

    }

    private void initClickAction() {
        binding.llProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
            }
        });
    }

    private void getInfo() {
        firebaseFirestore.collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String userName = documentSnapshot.get("userName").toString();
                String imageProfile = documentSnapshot.getString("profileImage");
                binding.tvUsername.setText(userName);
                if (imageProfile.equals("")) {
                    binding.imageProfile.setImageResource(R.drawable.person_placeholder); // set default image
                } else {
                    Glide.with(SettingsActivity.this).load(imageProfile).into(binding.imageProfile);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(getApplicationContext(), e.getMessage(), Toasty.LENGTH_SHORT, true).show();
            }
        });
    }
}