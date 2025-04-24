package com.example.zyntra;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private final List<Chat> chatList;
    private final String currentUserId;

    public ChatAdapter(Context context, List<Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        holder.txtUserName.setText(chat.getOtherUserName());
        holder.txtLastMessage.setText(chat.getLastMessage());

        // Set timestamp
        if (chat.getLastMessageTimestamp() != null) {
            holder.txtTimestamp.setText(formatTimestamp(chat.getLastMessageTimestamp()));
        } else {
            holder.txtTimestamp.setText("");
        }

        // Load profile image
        if (chat.getOtherUserProfileImage() != null && !chat.getOtherUserProfileImage().isEmpty()) {
            Glide.with(context)
                    .load(chat.getOtherUserProfileImage())
                    .placeholder(R.drawable.icon_profile)
                    .into(holder.imgUserProfilePic);
        } else {
            holder.imgUserProfilePic.setImageResource(R.drawable.icon_profile);
        }

        // Show unread indicator if message is from other user and not seen
        if (!chat.getLastMessageSenderId().equals(currentUserId) && !chat.isSeen()) {
            holder.viewUnread.setVisibility(View.VISIBLE);
        } else {
            holder.viewUnread.setVisibility(View.GONE);
        }

        // Set click listener to open chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            
            String otherUserId;
            if (chat.getUser1Id().equals(currentUserId)) {
                otherUserId = chat.getUser2Id();
            } else {
                otherUserId = chat.getUser1Id();
            }
            intent.putExtra("otherUserId", otherUserId);
            
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgUserProfilePic;
        TextView txtUserName, txtLastMessage, txtTimestamp;
        View viewUnread;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserProfilePic = itemView.findViewById(R.id.imgUserProfilePic);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            viewUnread = itemView.findViewById(R.id.viewUnread);
        }
    }

    private String formatTimestamp(Date timestamp) {
        if (timestamp == null) return "";
        
        Date now = new Date();
        long diffInMillis = now.getTime() - timestamp.getTime();
        long diffInSeconds = diffInMillis / 1000;
        long diffInMinutes = diffInSeconds / 60;
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;

        if (diffInMinutes < 1) {
            return "Just now";
        } else if (diffInHours < 1) {
            return diffInMinutes + " min ago";
        } else if (diffInDays < 1) {
            return diffInHours + " hr ago";
        } else if (diffInDays < 7) {
            return diffInDays + " day" + (diffInDays > 1 ? "s" : "") + " ago";
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return dateFormat.format(timestamp);
        }
    }
} 