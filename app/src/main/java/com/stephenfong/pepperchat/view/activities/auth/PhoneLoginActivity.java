package com.stephenfong.pepperchat.view.activities.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.databinding.ActivityPhoneLoginBinding;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class PhoneLoginActivity extends AppCompatActivity {

    private ActivityPhoneLoginBinding binding;

    private final static String TAG = "PhoneLoginActivity";

    private FirebaseAuth mAuth;
    private String mVerificationId;
    // private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        // Firebase Realtime database is structured as a JSON tree while Cloud Firestore stores data in
        // documents (document is a set of key-value pairs) and collections (collections of documents).
        // You can consider the firestore an upgrade of the realtime database
        // FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            startActivity(new Intent(this, SetUserInfoActivity.class));
        }

        progressDialog = new ProgressDialog(this);
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // the user enters its phone number, we then send the code
                if (binding.btnNext.getText().toString().equals("Next")) {
                    progressDialog.setMessage("Please wait");
                    progressDialog.show();

                    startPhoneNumberVerification(binding.ccp.getSelectedCountryCodeWithPlus(), binding.edtPhone.getText().toString());

                // verify the code
                } else {
                    progressDialog.setMessage("Verifying...");
                    progressDialog.show();
                    verifyPhoneNumberWithCode(mVerificationId, binding.edtCode.getText().toString());
                }

            }
        });

        // this callback function is used in startPhoneNumberVerification()
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
                progressDialog.dismiss();
            }

            @Override
            public void onVerificationFailed(@NotNull FirebaseException e) {
                Toasty.error(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT, true).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                mVerificationId = verificationId;
                // mResendToken = forceResendingToken;
                binding.btnNext.setText("Confirm");
                binding.edtCode.setVisibility(View.VISIBLE);
                progressDialog.dismiss();
            }
        };
    }

    // called when the user has entered the phone number and press next
    private void startPhoneNumberVerification(String countryCodeWithPlus, String phoneNumber) {
        progressDialog.setMessage("Sending code to " + countryCodeWithPlus + " " + phoneNumber);
        progressDialog.show();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(countryCodeWithPlus + phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // called when the user has entered the verification code and press confirm
    private void verifyPhoneNumberWithCode(String mVerificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    // given the credential, try to sign the user in
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            FirebaseUser user = task.getResult().getUser();
                            startActivity(new Intent(PhoneLoginActivity.this, SetUserInfoActivity.class));

                        } else {
                            progressDialog.dismiss();
                            // the code is incorrect
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toasty.error(getApplicationContext(), "Incorrect verification code, please try again.", Toast.LENGTH_SHORT, true).show();
                            }
                        }
                    }
                });
    }
}