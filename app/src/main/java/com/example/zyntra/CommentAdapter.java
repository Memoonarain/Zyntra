package com.example.zyntra;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.txtUsername.setText(comment.getUsername());
        holder.txtComment.setText(comment.getCommentText());

        // Load user image
        Glide.with(context).load(comment.getUserImg()).into(holder.imgUser);

        // Format timestamp
        holder.txtTime.setText(DateFormat.format("dd MMM yyyy", comment.getCommentTime()));

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgUser;
        TextView txtUsername, txtComment, txtTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.imgUserImg);
            txtUsername = itemView.findViewById(R.id.txtUserName);
            txtComment = itemView.findViewById(R.id.txtCommentContent);
            txtTime = itemView.findViewById(R.id.txtCommentTime);
        }
    }
}
