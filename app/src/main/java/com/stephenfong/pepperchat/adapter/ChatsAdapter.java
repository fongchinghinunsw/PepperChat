package com.stephenfong.pepperchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.model.chat.Chat;
import com.stephenfong.pepperchat.tools.AudioService;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private List<Chat> list;
    private Context context;
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private FirebaseUser firebaseUser;
    private ImageButton tmpBtnPlay;
    private AudioService audioService;

    public ChatsAdapter(List<Chat> list, Context context) {
        this.list = list;
        this.context = context;
        this.audioService = new AudioService(context);
    }

    public void setList(List<Chat> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_LEFT) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
        }
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessage;
        private LinearLayout layoutText, layoutImage, layoutVoice;
        private ImageView imageMessage;
        private ImageButton btnPlay;
        private ViewHolder tmpHolder;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textMessage = itemView.findViewById(R.id.tv_text_message);
            layoutImage = itemView.findViewById(R.id.layout_image);
            layoutText = itemView.findViewById(R.id.layout_text);
            layoutVoice = itemView.findViewById(R.id.layout_voice);
            imageMessage = itemView.findViewById(R.id.image_chat);
            btnPlay = itemView.findViewById(R.id.btn_play_chat);
        }

        void bind(Chat chat) {
            // check chat type
            switch (chat.getType()) {
                case "TEXT":
                    layoutText.setVisibility(View.VISIBLE);
                    layoutImage.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.GONE);

                    textMessage.setText(chat.getTextMessage());
                    break;
                case "IMAGE":
                    layoutText.setVisibility(View.GONE);
                    layoutImage.setVisibility(View.VISIBLE);
                    layoutVoice.setVisibility(View.GONE);

                    Glide.with(context).load(chat.getUrl()).into(imageMessage);
                    break;

                case "VOICE":
                    layoutText.setVisibility(View.GONE);
                    layoutImage.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.VISIBLE);

                    layoutVoice.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (tmpBtnPlay != null) {
                                tmpBtnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_play_circle_filled_24));
                            }
                            btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_pause_circle_filled_24));
                            audioService.playAudioFromUrl(chat.getUrl(), new AudioService.OnPlayCallBack() {
                                @Override
                                public void onFinished() {
                                    btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_play_circle_filled_24));
                                }
                            });
                            tmpBtnPlay = btnPlay;

                        }
                    });
                    break;
            }

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (list.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
