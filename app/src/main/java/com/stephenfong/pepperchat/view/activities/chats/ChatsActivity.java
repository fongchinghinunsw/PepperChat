package com.stephenfong.pepperchat.view.activities.chats;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordListener;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.adapter.ChatsAdapter;
import com.stephenfong.pepperchat.databinding.ActivityChatsBinding;
import com.stephenfong.pepperchat.manager.ChatService;
import com.stephenfong.pepperchat.interfaces.OnReadChatCallBack;
import com.stephenfong.pepperchat.model.chat.Chat;
import com.stephenfong.pepperchat.service.FirebaseService;
import com.stephenfong.pepperchat.view.activities.dialog.DialogReviewSendImage;
import com.stephenfong.pepperchat.view.activities.profile.UserProfileActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class ChatsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 2000;
    private ActivityChatsBinding binding;
    private String receiverID;
    private ChatsAdapter adapter;
    private List<Chat> list = new ArrayList<>();
    private String userName, userProfile;
    private boolean isActionShown = false;
    private ChatService chatService;

    private final int IMAGE_GALLERY_REQUEST = 1000;
    private Uri imageUri;

    // Audio
    private MediaRecorder mediaRecorder;
    private String audio_path;
    private String sTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chats);

        initialize();
        initBtnClick();
        readChats();
    }

    private void initialize() {

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        receiverID = intent.getStringExtra("userID"); // the user that's using this app
        userProfile = intent.getStringExtra("userProfile");

        chatService = new ChatService(this, receiverID);

        if (receiverID != null) {
            binding.tvUsername.setText(userName);

            if (userProfile.equals("")) {
                binding.imageProfile.setImageResource(R.drawable.person_placeholder); // set default image
            } else {
                Glide.with(this).load(userProfile).into(binding.imageProfile);
            }
        }

        binding.edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(binding.edtMessage.getText().toString())) {
                    binding.btnSend.setVisibility(View.INVISIBLE);
                    binding.recordButton.setVisibility(View.VISIBLE);
                } else {
                    binding.btnSend.setVisibility(View.VISIBLE);
                    binding.recordButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // orientation – Layout orientation. Should be HORIZONTAL or VERTICAL.
        // reverseLayout – When set to true, layouts from end to start
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        // items is displayed start from the bottom
        layoutManager.setStackFromEnd(true);

        binding.recyclerView.setLayoutManager(layoutManager);

        adapter = new ChatsAdapter(list, this);
        binding.recyclerView.setAdapter(adapter);

        // initialize record button
        binding.recordButton.setRecordView(binding.recordView);
        binding.recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                Toast.makeText(getApplicationContext(), "onStart", Toast.LENGTH_SHORT).show();
                // Start recording
                if (!checkPermissionFromDevice()) {
                    binding.btnEmoji.setVisibility(View.INVISIBLE);
                    binding.btnFile.setVisibility(View.INVISIBLE);
                    binding.btnCamera.setVisibility(View.INVISIBLE);
                    binding.edtMessage.setVisibility(View.INVISIBLE);

                    Toast.makeText(getApplicationContext(), "Start recording", Toast.LENGTH_SHORT).show();
                    startRecord();
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(100);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Requesting permission", Toast.LENGTH_SHORT).show();
                    requestPermission();
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(ChatsActivity.this, "onCancel", Toast.LENGTH_SHORT).show();
                try {
                    mediaRecorder.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(long recordTime) {
                binding.btnEmoji.setVisibility(View.VISIBLE);
                binding.btnFile.setVisibility(View.VISIBLE);
                binding.btnCamera.setVisibility(View.VISIBLE);
                binding.edtMessage.setVisibility(View.VISIBLE);

                // Stop Recording
                try {
                    sTime = getHumanTimeText(recordTime);
                    Toast.makeText(ChatsActivity.this, mediaRecorder + "...", Toast.LENGTH_SHORT).show();
                    stopRecord();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLessThanSecond() {
                binding.btnEmoji.setVisibility(View.VISIBLE);
                binding.btnFile.setVisibility(View.VISIBLE);
                binding.btnCamera.setVisibility(View.VISIBLE);
                binding.edtMessage.setVisibility(View.VISIBLE);

            }
        });
        binding.recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                binding.btnEmoji.setVisibility(View.VISIBLE);
                binding.btnFile.setVisibility(View.VISIBLE);
                binding.btnCamera.setVisibility(View.VISIBLE);
                binding.edtMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d",
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    private void readChats() {
        chatService.readChatData(new OnReadChatCallBack() {
            @Override
            public void onReadSuccess(List<Chat> list) {
                adapter.setList(list);
            }

            @Override
            public void onReadFailed() {
                Toasty.error(getApplicationContext(), "readChats() failed", Toasty.LENGTH_SHORT, true).show();
            }
        });
    }


    private void initBtnClick() {
        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(binding.edtMessage.getText().toString())) {
                    chatService.sendTextMsg(binding.edtMessage.getText().toString());

                    binding.edtMessage.setText("");
                }
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatsActivity.this, UserProfileActivity.class)
                        .putExtra("userID", receiverID)
                        .putExtra("profileImage", userProfile)
                        .putExtra("userName", userName));
            }
        });

        binding.btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isActionShown) {
                    binding.layoutAction.setVisibility(View.GONE);
                    isActionShown = false;
                } else {
                    binding.layoutAction.setVisibility(View.VISIBLE);
                    isActionShown = true;
                }
            }
        });

        binding.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    // this is called when the gallery button is clicked
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        // Allow the user to select a particular kind of data and return it.
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // createChooser() builds a new ACTION_CHOOSER Intent that wraps the given target intent,
        // by that, you can optionally supplying a title.
        startActivityForResult(Intent.createChooser(intent, "select image"), IMAGE_GALLERY_REQUEST);
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_DENIED || record_audio_result == PackageManager.PERMISSION_DENIED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_CODE_PERMISSION);
    }

    private void startRecord() {
        setUpMediaRecorder();
        Toast.makeText(this, mediaRecorder + "...", Toast.LENGTH_SHORT).show();

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            Toasty.error(this, "Recording Error, Please restart your app: " + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
        }
    }

    private void stopRecord() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;

                chatService.sendVoice(audio_path);
            } else {
                Toasty.error(this, "null", Toasty.LENGTH_SHORT, true).show();
            }
        } catch (Exception e) {
            Toasty.error(this, "Stop Recording Error: " + e.getMessage(), Toasty.LENGTH_SHORT, true).show();
        }
    }

    private void setUpMediaRecorder() {
        String path_save = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + UUID.randomUUID().toString() + "audio_record.m4a";
        audio_path = path_save;

        mediaRecorder = new MediaRecorder();
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(path_save);
        } catch (Exception e) {
            Toasty.error(this, "media recorder error", Toasty.LENGTH_SHORT, true).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            imageUri = data.getData();

            // uploadProfileImageToFirebase();

            try {
                // Everything that is drawn in android is a Bitmap. We can create a Bitmap instance ,
                // either by using the Bitmap class which has methods that allow us to manipulate pixels
                // in the 2d coordinate system
                // MediaStore defines the contract between the media provider and applications, you can
                // use it to get media data from the device
                // Content Resolver is the single, global instance in your application that provides
                // access to your (and other applications') content providers
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                reviewImage(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void reviewImage(Bitmap bitmap) {
        new DialogReviewSendImage(ChatsActivity.this, bitmap).show(new DialogReviewSendImage.OnCallBack() {
            @Override
            public void onButtonSendClick() {
                // upload image to firebase storage to get url image
                if (imageUri != null) {
                    ProgressDialog progressDialog = new ProgressDialog(ChatsActivity.this);
                    progressDialog.show();

                    // hide action botton
                    binding.layoutAction.setVisibility(View.GONE);
                    isActionShown = false;

                    new FirebaseService(ChatsActivity.this).uploadImageToFirebaseStorage(imageUri, new FirebaseService.OnCallBack() {
                        @Override
                        public void onUploadSuccess(Uri imageUrl) {
                            // send chat image
                            chatService.sendImage(imageUrl.toString());
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onUploadFailed(Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

            }
        });
    }
}