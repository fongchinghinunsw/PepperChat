package com.stephenfong.pepperchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.model.user.User;
import com.stephenfong.pepperchat.view.activities.chats.ChatsActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class
ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;

    public ContactsAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NotNull
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_contact_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ViewHolder holder, int position) {
        User user = userList.get(position);

        holder.username.setText(user.getUserName());
        holder.desc.setText(user.getUserPhone());

        if (user.getProfileImage().equals("")) {
            holder.profileImage.setImageResource(R.drawable.person_placeholder); // set default image
        } else {
            Glide.with(context).load(user.getProfileImage()).into(holder.profileImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ChatsActivity.class)
                        .putExtra("userID", user.getUserID())
                        .putExtra("userName", user.getUserName())
                        .putExtra("userProfile", user.getProfileImage())
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView profileImage;
        private TextView username, desc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.tv_username);
            desc = itemView.findViewById(R.id.tv_desc);
        }
    }
}
