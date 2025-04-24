package com.example.zyntra;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private FirebaseFirestore db;
    private String currentUserId;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.db = FirebaseFirestore.getInstance();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.txtUserName.setText(user.getFullName());
        holder.txtUserBio.setText(user.getBio() != null ? user.getBio() : "No bio available");

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.icon_profile)
                    .into(holder.imgUserProfilePic);
        } else {
            holder.imgUserProfilePic.setImageResource(R.drawable.icon_profile);
        }

        // Check if current user is following this user
        checkIfFollowing(user.getUserId(), holder);

        // Set follow button click listener
        holder.btnFollow.setOnClickListener(v -> {
            if (user.isFollowed()) {
                unfollowUser(user, holder);
            } else {
                followUser(user, holder);
            }
        });

        // Set view profile button click listener
        holder.btnViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("UserId", user.getUserId());
            context.startActivity(intent);
        });
    }

    private void checkIfFollowing(String userId, UserViewHolder holder) {
        db.collection("users").document(currentUserId)
                .collection("following")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = userList.get(holder.getAdapterPosition());
                    if (documentSnapshot.exists()) {
                        user.setFollowed(true);
                        holder.btnFollow.setText("Unfollow");
                    } else {
                        user.setFollowed(false);
                        holder.btnFollow.setText("Follow");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error checking follow status", Toast.LENGTH_SHORT).show();
                });
    }

    private void followUser(User user, UserViewHolder holder) {
        // Add to current user's following collection
        Map<String, Object> followingData = new HashMap<>();
        followingData.put("userId", user.getUserId());
        followingData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("users").document(currentUserId)
                .collection("following")
                .document(user.getUserId())
                .set(followingData)
                .addOnSuccessListener(aVoid -> {
                    // Add to target user's followers collection
                    Map<String, Object> followerData = new HashMap<>();
                    followerData.put("userId", currentUserId);
                    followerData.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("users").document(user.getUserId())
                            .collection("followers")
                            .document(currentUserId)
                            .set(followerData)
                            .addOnSuccessListener(aVoid1 -> {
                                user.setFollowed(true);
                                holder.btnFollow.setText("Unfollow");
                                Toast.makeText(context, "Following " + user.getFullName(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Error following user", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error following user", Toast.LENGTH_SHORT).show();
                });
    }

    private void unfollowUser(User user, UserViewHolder holder) {
        // Remove from current user's following collection
        db.collection("users").document(currentUserId)
                .collection("following")
                .document(user.getUserId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from target user's followers collection
                    db.collection("users").document(user.getUserId())
                            .collection("followers")
                            .document(currentUserId)
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                user.setFollowed(false);
                                holder.btnFollow.setText("Follow");
                                Toast.makeText(context, "Unfollowed " + user.getFullName(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Error unfollowing user", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error unfollowing user", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgUserProfilePic;
        TextView txtUserName, txtUserBio;
        Button btnFollow, btnViewProfile;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserProfilePic = itemView.findViewById(R.id.imgUserProfilePic);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserBio = itemView.findViewById(R.id.txtUserBio);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
        }
    }
} 