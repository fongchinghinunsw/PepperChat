package com.stephenfong.pepperchat.menu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.adapter.ChatListAdapter;
import com.stephenfong.pepperchat.databinding.FragmentChatsBinding;
import com.stephenfong.pepperchat.model.ChatList;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragmentLOG";
    private FragmentChatsBinding binding;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private FirebaseFirestore firebaseFirestore;
    private Handler handler = new Handler();

    private List<ChatList> list;

    private ArrayList<String> allUserID;

    private ChatListAdapter chatListAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chats, container, false);

        list = new ArrayList<>();
        allUserID = new ArrayList<>();

        // By default, LinearLayoutManager lays out 1 extra page of items while smooth scrolling,
        // in the direction of the scroll, and no extra space is laid out in all other situations
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // onClickListener for each chat item is initialized in the adapter's onBindViewHolder()
        chatListAdapter = new ChatListAdapter(list, getContext());
        binding.recyclerView.setAdapter(chatListAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            getChatList();
        }


        return binding.getRoot();
    }

    private void getChatList() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        databaseReference.child("ChatList").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                list.clear();
                allUserID.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userID = Objects.requireNonNull(snapshot.child("chatid").getValue()).toString();

                    allUserID.add(userID);
                }
                binding.progressCircular.setVisibility(View.GONE);
                getUserInfo();
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {

            }
        });
    }

    private void getUserInfo() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (String userID : allUserID) {
                    firebaseFirestore.collection("Users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            ChatList chat = new ChatList(
                                    documentSnapshot.getString("userID"),
                                    documentSnapshot.getString("userName"),
                                    "This is description..",
                                    "",
                                    documentSnapshot.getString("profileImage")
                            );
                            list.add(chat);

                            if (chatListAdapter != null) {
                                chatListAdapter.notifyItemInserted(0);
                                chatListAdapter.notifyDataSetChanged();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.error(getActivity(), e.getMessage(), Toasty.LENGTH_SHORT, true).show();
                        }
                    });
                }
            }
        });
    }
}