package com.example.zyntra;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    String fullName = "Unkown User";
    private Context context;
    private List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.txtUserName.setText(post.getUserName());
        holder.txtPostTime.setText(DateFormat.format("dd MMM yyyy", post.getTimestamp().toDate()));
        holder.txtReadmore.setVisibility(View.GONE);
        holder.txtPostText.setMaxLines(3);
        holder.txtPostText.setEllipsize(TextUtils.TruncateAt.END);
        holder.txtPostText.setText(post.getPostText());

        if (post.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            holder.btnOptions.setVisibility(View.VISIBLE);
        }
        else {
            holder.btnOptions.setVisibility(View.GONE);
        }
        isPostLiked(post, holder.btnLike);

        holder.imgUserImg.setOnClickListener(v -> {
                Log.w("Checking data", post.getPostId());
                openUserProfile(post);
        });
        holder.txtUserName.setOnClickListener(v -> openUserProfile(post));
        holder.txtPostText.setOnClickListener(v ->  {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("postId", post.getPostId());
            context.startActivity(intent);
        });
        holder.postImageContent.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("postId", post.getPostId());
            context.startActivity(intent);
        });
        holder.btnLike.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String postId = post.getPostId();
            holder.btnLike.setImageResource(R.drawable.icon_liked);
            db.collection("Posts")
                    .document(postId)
                    .collection("likes")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            // Unlike
                            db.collection("Posts")
                                    .document(postId)
                                    .collection("likes")
                                    .document(userId)
                                    .delete()
                                    .addOnSuccessListener(unused -> holder.btnLike.setImageResource(R.drawable.icon_like));
                        } else {
                            // Like
                            db.collection("Posts")
                                    .document(postId)
                                    .collection("likes")
                                    .document(userId)
                                    .set(new Like(FirebaseAuth.getInstance().getUid(), System.currentTimeMillis()))
                                    .addOnSuccessListener(unused -> { sendLikeNotification(post);
                                        holder.btnLike.setImageResource(R.drawable.icon_liked);
                                    });
                        }
                    });
        });
        holder.btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("postId", post.getPostId());
            context.startActivity(intent);
        });
        holder.btnShare.setOnClickListener(v -> {
            String shareText = post.getPostText();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            context.startActivity(Intent.createChooser(shareIntent, "Share post via"));
        });
        holder.btnOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.btnOptions);
            popupMenu.getMenuInflater().inflate(R.menu.menu_post_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_edit) {
                    Log.e("Eroor Checking", "Post Id: "+post.getPostId());
                    // Launch EditPostActivity
                    Intent intent = new Intent(context, EditPostActivity.class);
                    intent.putExtra("postId", post.getPostId());
                    context.startActivity(intent);
                    return true;
                } else if (id == R.id.action_delete) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Post")
                            .setMessage("Are you sure you want to delete this post?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                FirebaseFirestore.getInstance()
                                        .collection("Posts")
                                        .document(post.getPostId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                            postList.remove(holder.getAdapterPosition());
                                            notifyItemRemoved(holder.getAdapterPosition());
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                            Log.e("PostAdapter", "Delete failed", e);
                                        });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                }

                return false;
            });

            popupMenu.show();
        });
        Glide.with(context).load(post.getUserImageUrl()).placeholder(R.drawable.icon_profile).into(holder.imgUserImg);
        if (post.getPostImageUrl() != null && !post.getPostImageUrl().isEmpty()) {
            holder.postImageContent.setVisibility(View.VISIBLE);
            Glide.with(context).load(post.getPostImageUrl()).placeholder(R.drawable.icon_image_loading).into(holder.postImageContent);
        }
        else {
            holder.postImageContent.setVisibility(View.GONE);
        }
    }

    private void openUserProfile(Post post) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("UserId", post.getUserId());
            context.startActivity(intent);
    }

    private void sendLikeNotification(Post post) {
        // Fetch the FCM token of the post owner
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Posts").document(post.getPostId()).get()
                .addOnSuccessListener(postSnapshot -> {
                    if (postSnapshot.exists()) {
                        String ownerId = postSnapshot.getString("userId");
                        if (!ownerId.equals(FirebaseAuth.getInstance().getUid())) {

                            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get()
                                    .addOnSuccessListener(documentSnapshot -> {

                                        String firstName = documentSnapshot.getString("firstName");
                                        String lastName = documentSnapshot.getString("lastName");
                                        Log.e("error name check", firstName+lastName+ " ");

                                        fullName = (firstName != null ? firstName : "") +
                                                (lastName != null ? " " + lastName : "");
                                    });
                            if (fullName.isEmpty()) fullName = "Unknown User";
                            db.collection("users").document(ownerId).get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        String token = userSnapshot.getString("fcmToken");
                                        if (token != null && !token.isEmpty()) {
                                            NotificationModel notification = new NotificationModel(
                                                    fullName+ " liked your post",
                                                    post.getPostId(),
                                                    ownerId,
                                                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                    "New Like",
                                                    "like",
                                                    token
                                            );
                                            FcmPushNotification.saveAndPushNotification(context, notification);
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
    private void isPostLiked(Post post, ImageButton btnLike) {
        FirebaseFirestore.getInstance()
                .collection("Posts")
                .document(post.getPostId())
                .collection("likes")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        btnLike.setImageResource(R.drawable.icon_liked); // filled heart
                    } else {
                        btnLike.setImageResource(R.drawable.icon_like); // outline heart
                    }
                });
    }
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgUserImg;
        TextView txtUserName, txtPostTime, txtPostText, txtReadmore;
        ImageView postImageContent;
        VideoView postVideoContent;
        ImageButton btnLike, btnComment, btnShare,btnOptions;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReadmore = itemView.findViewById(R.id.txtReadMore);
            imgUserImg = itemView.findViewById(R.id.imgUserImg);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtPostTime = itemView.findViewById(R.id.txtPostTime);
            txtPostText = itemView.findViewById(R.id.txtPostText);
            postImageContent = itemView.findViewById(R.id.postImageContent);
            postVideoContent = itemView.findViewById(R.id.postVideoContent);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnOptions = itemView.findViewById(R.id.btnPostOptions);
        }
    }
}
