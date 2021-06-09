package com.stephenfong.pepperchat.view.activities.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stephenfong.pepperchat.BuildConfig;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.common.Common;
import com.stephenfong.pepperchat.databinding.ActivityProfileBinding;
import com.stephenfong.pepperchat.view.activities.display.ViewImageActivity;
import com.stephenfong.pepperchat.view.activities.startup.SplashScreenActivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;

    // A built-in class for creating a dialog at the bottom.
    private BottomSheetDialog pickIconBottomSheetDialog, editNameBottomSheetDialog;
    private ProgressDialog progressDialog;

    private final int IMAGE_GALLERY_REQUEST = 1000;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        // similar to setActionBar, but also support older Android versions
        // this method applies your custom toolbar instead of default toolbar into that specific
        // screen/activity
        setSupportActionBar(binding.toolbar);
        // show the Backward button on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);

        if (firebaseUser != null) {
            getInfo();
        }

        initActionClick();

    }

    private void getInfo() {
        firebaseFirestore.collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String userName = documentSnapshot.getString("userName");
                String userPhone = documentSnapshot.getString("userPhone");
                String imageProfile = documentSnapshot.getString("profileImage");

                binding.tvUsername.setText(userName);
                binding.tvPhone.setText(userPhone);
                if (imageProfile.equals("")) {
                    Toasty.info(getApplicationContext(), "getInfo()", Toasty.LENGTH_SHORT).show();
                    binding.imageProfile.setImageResource(R.drawable.person_placeholder); // set default image
                } else {
                    Glide.with(ProfileActivity.this).load(imageProfile).into(binding.imageProfile);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NotNull Exception e) {

            }
        });
    }

    private void initActionClick() {
        binding.fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetPickPhoto();
            }
        });

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // tells the system that the current view has changed and it should be redrawn as
                // soon as possible. As this method can only be called from your UIThread
                binding.imageProfile.invalidate();
                Drawable dr = binding.imageProfile.getDrawable();
                // Everything that is drawn in android is a Bitmap. We can create a Bitmap instance ,
                // either by using the Bitmap class which has methods that allow us to manipulate pixels
                // in the 2d coordinate system
                Common.IMAGE_BITMAP = ((BitmapDrawable) dr.getCurrent()).getBitmap();
                // Create an ActivityOptions to transition between Activities using cross-Activity scene animations.
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileActivity.this, binding.imageProfile, "image");
                Intent intent = new Intent(ProfileActivity.this, ViewImageActivity.class);
                startActivity(intent, activityOptionsCompat.toBundle());
            }
        });

        binding.llEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetEditName();
            }
        });

        binding.btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogOutDialog();
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

    private void showBottomSheetEditName() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_name, null);

        ((View) view.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNameBottomSheetDialog.dismiss();
            }
        });

        EditText edtUserName = view.findViewById(R.id.edt_username);
        ((View) view.findViewById(R.id.btn_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edtUserName.getText().toString())) {
                    Toasty.warning(getApplicationContext(), "Name can't be empty", Toast.LENGTH_SHORT, true).show();
                } else {
                    updateName(edtUserName.getText().toString());
                    editNameBottomSheetDialog.dismiss();
                }
            }
        });

        editNameBottomSheetDialog = new BottomSheetDialog(this);
        editNameBottomSheetDialog.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Objects.requireNonNull(editNameBottomSheetDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        editNameBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                editNameBottomSheetDialog = null;
            }
        });
        editNameBottomSheetDialog.show();
    }

    private void showLogOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setMessage("Do you want to sign out?")
                .setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, SplashScreenActivity.class));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

            uploadProfileImageToFirebase();
        } else if (requestCode == 5000
                && resultCode == RESULT_OK) {
            uploadToFirebase();
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
        if (imageUri!=null){
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("profileImages/" + System.currentTimeMillis()+"."+ getFileExtension(imageUri));
            riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    final String sdownload_url = String.valueOf(downloadUrl);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("profileImage", sdownload_url);

                    progressDialog.dismiss();
                    firebaseFirestore.collection("Users").document(firebaseUser.getUid()).update(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(),"upload successfully",Toast.LENGTH_SHORT).show();

                                    getInfo();
                                }
                            });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"upload Failed",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void uploadProfileImageToFirebase() {
        if (imageUri != null) {
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            // profile images are stored in the FireStorage first, then we stored the URL of the image
            // in the firestore.
            StorageReference profileImagesRef = FirebaseStorage.getInstance().getReference().child("profileImages/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            profileImagesRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                    // Keep looping until the task is completed
                    while (!uriTask.isSuccessful());

                    Uri downloadUri = uriTask.getResult();
                    final String url = String.valueOf(downloadUri);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("profileImage", url);

                    progressDialog.dismiss();
                    firebaseFirestore.collection("Users").document(firebaseUser.getUid()).update(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toasty.success(getApplicationContext(), "Upload Successful", Toast.LENGTH_SHORT, true).show();
                            // update the image immediately
                            getInfo();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(getApplicationContext(), "Upload Failed", Toast.LENGTH_SHORT, true).show();
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void updateName(String newName) {
        firebaseFirestore.collection("Users").document(firebaseUser.getUid()).update("userName", newName).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toasty.success(getApplicationContext(), "Update Successful", Toast.LENGTH_SHORT, true).show();
                getInfo();
            }
        });
    }
}