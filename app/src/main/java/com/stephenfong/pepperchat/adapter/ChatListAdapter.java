package com.stephenfong.pepperchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.model.ChatList;
import com.stephenfong.pepperchat.view.activities.chats.ChatsActivity;
import com.stephenfong.pepperchat.view.activities.dialog.DialogViewUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.Holder> {

    private List<ChatList> list;
    private Context context;

    public ChatListAdapter(List<ChatList> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NotNull
    @Override
    public ChatListAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_chat_list, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(ChatListAdapter.Holder holder, int position) {
        ChatList chatlist = list.get(position);

        holder.tvName.setText(chatlist.getUserName());
        holder.tvDesc.setText(chatlist.getDescription());
        holder.tvDate.setText(chatlist.getDate());

        if (chatlist.getUrlProfile().equals("")) {
            holder.profile.setImageResource(R.drawable.person_placeholder); // set default image
        } else {
            Glide.with(context).load(chatlist.getUrlProfile()).into(holder.profile);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                context.startActivity(new Intent(context, ChatsActivity.class)
                        .putExtra("userID", chatlist.getUserID())
                        .putExtra("userName", chatlist.getUserName())
                        .putExtra("userProfile", chatlist.getUrlProfile())
                );
            }
        });

        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogViewUser(context, chatlist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDesc, tvDate;
        private CircularImageView profile;

        public Holder (View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tv_date);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvName = itemView.findViewById(R.id.tv_name);
            profile = itemView.findViewById(R.id.image_profile);
        }
    }
}
