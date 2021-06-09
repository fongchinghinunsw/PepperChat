package com.stephenfong.pepperchat.service;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stephenfong.pepperchat.model.StatusModel;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class FirebaseService {

    private Context context;

    public FirebaseService(Context context) {
        this.context = context;
    }

    public void uploadImageToFirebaseStorage(Uri uri, OnCallBack onCallBack) {
        // profile images are stored in the FireStorage first, then we stored the URL of the image
        // in the firestore.
        StorageReference profileImagesRef = FirebaseStorage.getInstance().getReference().child("ImagesChats/" + System.currentTimeMillis() + "." + getFileExtension(uri));
        profileImagesRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                // Keep looping until the task is completed
                while (!uriTask.isSuccessful());

                Uri downloadUri = uriTask.getResult();
                final String url = String.valueOf(downloadUri);

                onCallBack.onUploadSuccess(downloadUri);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCallBack.onUploadFailed(e);
            }
        });
    }

    private String getFileExtension(Uri uri) {
        // The Content Resolver is the single, global instance in your application that provides access
        // to your (and other applications') content providers. The Content Resolver behaves exactly as
        // its name implies: it accepts requests from clients, and resolves these requests by directing
        // them to the content provider with a distinct authority.
        ContentResolver contentResolver = context.getContentResolver();
        // Two-way map that maps MIME-types to file extensions and vice versa.
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public interface OnCallBack {
        void onUploadSuccess(Uri imageUrl);
        void onUploadFailed(Exception e);
    }

    public void addNewStatus(StatusModel statusModel, final OnAddNewStatusCallBack onAddNewStatusCallBack) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Status Daily").document(statusModel.getId()).set(statusModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                onAddNewStatusCallBack.onSuccess();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {

            }
        });
    }

    public interface OnAddNewStatusCallBack {
        void onSuccess();
        void onFailed();
    }
}
