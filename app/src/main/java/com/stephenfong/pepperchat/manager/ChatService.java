package com.stephenfong.pepperchat.manager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stephenfong.pepperchat.interfaces.OnReadChatCallBack;
import com.stephenfong.pepperchat.model.chat.Chat;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChatService {
    private Context context;
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private String receiverID;

    public ChatService(Context context) {
        this.context = context;
    }

    public ChatService(Context context, String receiverID) {
        this.context = context;
        this.receiverID = receiverID;
    }

    public void readChatData(OnReadChatCallBack onReadChatCallBack) {
        reference.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Chat> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    try {
                        if (chat != null && chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(receiverID)
                                || chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(receiverID)) {
                            list.add(chat);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                onReadChatCallBack.onReadSuccess(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onReadChatCallBack.onReadFailed();
            }
        });
    }

    public void sendTextMsg(String text) {
        Chat chat = new Chat(
                getCurrentDate(),
                text,
                "",
                "TEXT",
                firebaseUser.getUid(),
                receiverID
        );

        reference.child("Chats").push().setValue(chat).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("Send", "onSuccess: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.d("Send", "onFailure: " + e.getMessage());
            }
        });

        // Add to ChatList
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid()).child(receiverID);
        chatRef1.child("chatid").setValue(receiverID);

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(firebaseUser.getUid());
        chatRef2.child("chatid").setValue(firebaseUser.getUid());
    }

    public void sendImage(String imageUrl) {
        Chat chat = new Chat(
                getCurrentDate(),
                "",
                imageUrl,
                "IMAGE",
                firebaseUser.getUid(),
                receiverID
        );

        reference.child("Chats").push().setValue(chat).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("Send", "onSuccess: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.d("Send", "onFailure: " + e.getMessage());
            }
        });

        // Add to ChatList
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid()).child(receiverID);
        chatRef1.child("chatid").setValue(receiverID);

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(firebaseUser.getUid()
        );
        chatRef2.child("chatid").setValue(firebaseUser.getUid());
    }

    public String getCurrentDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String today = formatter.format(date);

        Calendar currentDateTime = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        String currentTime = df.format(currentDateTime.getTime());

        return today + ", " + currentTime;
    }

    public void sendVoice(String audioPath) {
        Uri uriAudio = Uri.fromFile(new File(audioPath));
        final StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("Chats/Voice/" + System.currentTimeMillis());
        audioRef.putFile(uriAudio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot audioSnapshot) {
                Task<Uri> urlTask = audioSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();
                String voiceUrl = String.valueOf(downloadUrl);

                Chat chat = new Chat(
                        getCurrentDate(),
                        "",
                         voiceUrl,
                        "VOICE",
                        firebaseUser.getUid(),
                        receiverID
                );

                reference.child("Chats").push().setValue(chat).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Send", "onSuccess: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Send", "onFailure: " + e.getMessage());
                    }
                });

                // Add to ChatList
                DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid()).child(receiverID);
                chatRef1.child("chatid").setValue(receiverID);

                DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(firebaseUser.getUid());
                chatRef2.child("chatid").setValue(firebaseUser.getUid());
            }
        });
    }
}
