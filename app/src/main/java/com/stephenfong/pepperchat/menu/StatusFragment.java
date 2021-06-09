
package com.stephenfong.pepperchat.menu;

import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.databinding.FragmentStatusBinding;
import com.stephenfong.pepperchat.view.activities.profile.ProfileActivity;
import com.stephenfong.pepperchat.view.activities.status.DisplayStatusActivity;

public class StatusFragment extends Fragment {

    private FragmentStatusBinding binding;

    public StatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_status, container, false);

        getProfile();
        binding.llStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getContext(), DisplayStatusActivity.class));
            }
        });
        return binding.getRoot();
    }

    private void getProfile() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String imageProfile = documentSnapshot.getString("profileImage");

                if (imageProfile.equals("")) {
                    binding.imageProfile.setImageResource(R.drawable.person_placeholder); // set default image
                } else {
                    Glide.with(getContext()).load(imageProfile).into(binding.imageProfile);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NotNull Exception e) {

            }
        });
    }
}