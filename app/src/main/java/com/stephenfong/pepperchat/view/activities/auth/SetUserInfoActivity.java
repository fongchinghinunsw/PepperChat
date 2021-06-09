package com.stephenfong.pepperchat.view.activities.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stephenfong.pepperchat.BuildConfig;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.databinding.ActivitySetUserInfoBinding;
import com.stephenfong.pepperchat.model.user.User;
import com.stephenfong.pepperchat.view.MainActivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class SetUserInfoActivity extends AppCompatActivity {

    private ActivitySetUserInfoBinding binding;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser firebaseUser;

    private ProgressDialog progressDialog;

    // A built-in class for creating a dialog at the bottom.
    private BottomSheetDialog pickIconBottomSheetDialog;

    private final int IMAGE_GALLERY_REQUEST = 1000;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_user_info);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is new or not
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(firebaseUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                // task represents an asynchronous operation. getResult() returns DocumentSnapshot
                if (task.isSuccessful()) {
                    if (task.getResult().getString("userName") != null) {
                        //Log.d("WTF", "Yahoo");
                        binding.edtName.setText(task.getResult().getString("userName"));
                        Toast.makeText(getApplicationContext(), "Found the name", Toast.LENGTH_SHORT).show();

                    }
                    //Log.d("WTF", task.getResult().getData() + "");
                    //Log.d("WTF", task.getException() + "");
                    //Log.d("WTF", task.getResult() + "");
                    Toast.makeText(getApplicationContext(), task.getResult().getData().get("profileImage") + "...", Toast.LENGTH_SHORT).show();
                    Glide.with(SetUserInfoActivity.this).load(task.getResult().getData().get("profileImage")).into(binding.imageProfile);
                } else {
                    Log.d("WTF", task.getResult() + " : not completed");
                }
            }
        });

        progressDialog = new ProgressDialog(this);
        initButtonClick();
    }

    private void initButtonClick() {
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(binding.edtName.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Please input username", Toast.LENGTH_SHORT).show();
                } else {
                    uploadToFirebase();
                }
            }
        });

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pickImage();
                showBottomSheetPickPhoto();
            }
        });
    }

    private void showBottomSheetPickPhoto() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_pick, null);

        ((View) view.findViewById(R.id.ll_gallery)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                pickIconBottomSheetDialog.dismiss();
            }
        });

        ((View) view.findViewById(R.id.ll_camera)).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                checkCameraPermission();
                pickIconBottomSheetDialog.dismiss();
            }
        });

        pickIconBottomSheetDialog = new BottomSheetDialog(this);
        pickIconBottomSheetDialog.setContentView(view);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Objects.requireNonNull(pickIconBottomSheetDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }

        pickIconBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                pickIconBottomSheetDialog = null;
            }
        });
        pickIconBottomSheetDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    3000);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    4000);
        } else {
            openCamera();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        try {
            File file = File.createTempFile("IMG_" + timeStamp, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.putExtra("listPhotoName", imageFileName);
            startActivityForResult(intent, 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        // Allow the user to select a particular kind of data and return it.
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // createChooser() builds a new ACTION_CHOOSER Intent that wraps the given target intent,
        // by that, you can optionally supplying a title.
        startActivityForResult(Intent.createChooser(intent, "select image"), IMAGE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            imageUri = data.getData();
            Glide.with(SetUserInfoActivity.this).load(imageUri).into(binding.imageProfile);

        } else if (requestCode == 5000
                && resultCode == RESULT_OK) {
            //uploadToFirebase();
            Glide.with(SetUserInfoActivity.this).load(imageUri).into(binding.imageProfile);
        }
    }

    private String getFileExtension(Uri uri) {
        // The Content Resolver is the single, global instance in your application that provides access
        // to your (and other applications') content providers. The Content Resolver behaves exactly as
        // its name implies: it accepts requests from clients, and resolves these requests by directing
        // them to the content provider with a distinct authority.
        ContentResolver contentResolver = getContentResolver();
        // Two-way map that maps MIME-types to file extensions and vice versa.
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadToFirebase() {
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if (imageUri != null) {
            StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("profileImages/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    Uri downloadUrl = urlTask.getResult();

                    final String sdownload_url = String.valueOf(downloadUrl);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("profileImage", sdownload_url);
                    hashMap.put("userName", binding.edtName.getText().toString());

                    progressDialog.dismiss();
                    firebaseFirestore.collection("Users").document(firebaseUser.getUid()).update(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "upload successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                }
                            });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "upload Failed", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userName", binding.edtName.getText().toString());

            progressDialog.dismiss();
            firebaseFirestore.collection("Users").document(firebaseUser.getUid()).update(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    });
        }
    }
}