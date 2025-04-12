package com.example.zyntra;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class PostActivity extends AppCompatActivity {
    private String originalImageUrl;
    private FirebaseFirestore firestore;
    private TextView txtPostText, txtuserName;
    private ImageView postImageContent, userImage;
    private EditText edtComment;
    private ImageButton btnSendComment, btnLike;
    private FirebaseAuth auth;
    private RecyclerView rvComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private FirebaseFirestore db;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtuserName = findViewById(R.id.txtUserName);
        userImage = findViewById(R.id.imgUserImg);
        firestore = FirebaseFirestore.getInstance();
        txtPostText = findViewById(R.id.txtPostText);
        postImageContent = findViewById(R.id.postImageContent);
        edtComment = findViewById(R.id.edtComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        btnLike = findViewById(R.id.btnLike);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnSendComment.setOnClickListener(v -> postComment());
        postId = getIntent().getStringExtra("postId");

        rvComments = findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        rvComments.setAdapter(commentAdapter);

        if (postId != null) {
            loadPostDetails();
            loadComments();
        } else {
            Toast.makeText(this, "Post ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.btnShare).setOnClickListener(v -> sharePost());
        findViewById(R.id.btnLike).setOnClickListener(v -> likePost());
    }

    private void likePost() {
        db.collection("Posts").document(postId).collection("likes").document(auth.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        db.collection("Posts").document(postId).collection("likes").document(auth.getUid())
                                .delete()
                                .addOnSuccessListener(unused -> btnLike.setImageResource(R.drawable.icon_like));
                    } else {
                        db.collection("Posts").document(postId).collection("likes").document(auth.getUid())
                                .set(new Like(auth.getUid(), System.currentTimeMillis()))
                                .addOnSuccessListener(unused -> {
                                    btnLike.setImageResource(R.drawable.icon_liked);
                                    db.collection("Posts").document(postId).get()
                                            .addOnSuccessListener(postSnapshot -> {
                                                if (postSnapshot.exists()) {
                                                    String ownerId = postSnapshot.getString("userId");
                                                    if (!ownerId.equals(auth.getUid())) {
                                                        db.collection("users").document(ownerId).get()
                                                                .addOnSuccessListener(userSnapshot -> {
                                                                    String token = userSnapshot.getString("fcmToken");
                                                                    if (token != null && !token.isEmpty()) {
                                                                        NotificationModel notification = new NotificationModel(
                                                                                "Someone liked your post",
                                                                                postId,
                                                                                ownerId,
                                                                                auth.getUid(),
                                                                                "New Like",
                                                                                "like",
                                                                                token
                                                                        );
                                                                        FcmPushNotification.saveAndPushNotification(PostActivity.this, notification);
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                });
                    }
                });
    }

    private void postComment() {
        String commentText = edtComment.getText().toString().trim();
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String userImage = documentSnapshot.getString("profileImageUrl");

                        String fullName = (firstName != null ? firstName : "") +
                                (lastName != null ? " " + lastName : "");
                        if (fullName.trim().isEmpty()) fullName = "Unknown User";
                        if (userImage == null) userImage = "";

                        String commentId = db.collection("Posts").document(postId).collection("comments").document().getId();
                        long commentTime = System.currentTimeMillis();

                        Comment comment = new Comment(commentId, userId, fullName, userImage, commentText, commentTime);

                        String finalFullName = fullName;
                        db.collection("Posts").document(postId).collection("comments").document(commentId)
                                .set(comment)
                                .addOnSuccessListener(aVoid -> {
                                    edtComment.setText("");
                                    Toast.makeText(PostActivity.this, "Comment added", Toast.LENGTH_SHORT).show();

                                    db.collection("Posts").document(postId).get()
                                            .addOnSuccessListener(postSnapshot -> {
                                                if (postSnapshot.exists()) {
                                                    String ownerId = postSnapshot.getString("userId");
                                                    if (!ownerId.equals(auth.getUid())) {
                                                        db.collection("users").document(ownerId).get()
                                                                .addOnSuccessListener(userSnapshot -> {
                                                                    String token = userSnapshot.getString("fcmToken");
                                                                    if (token != null && !token.isEmpty()) {
                                                                        NotificationModel notification = new NotificationModel(
                                                                                finalFullName + " commented: " + commentText,
                                                                                postId,
                                                                                ownerId,
                                                                                auth.getUid(),
                                                                                "New Comment",
                                                                                "comment",
                                                                                token
                                                                        );
                                                                        FcmPushNotification.saveAndPushNotification(PostActivity.this, notification);
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                })
                                .addOnFailureListener(e -> Toast.makeText(PostActivity.this, "Failed to add comment", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                    Log.e("PostComment", "Error fetching user doc: ", e);
                });
    }

    private void sharePost() {
        String shareText = txtPostText.getText().toString() + "\n\nCheck this post on Zyntra!";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void loadPostDetails() {
        db.collection("Posts").document(postId).collection("likes").document(auth.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        btnLike.setImageResource(R.drawable.icon_liked);
                    }
                });

        firestore.collection("Posts").document(postId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String postText = snapshot.getString("postText");
                        originalImageUrl = snapshot.getString("postImageUrl");
                        String userName = snapshot.getString("userName");
                        String userImg = snapshot.getString("userImageUrl");

                        txtuserName.setText(userName);

                        Glide.with(PostActivity.this)
                                .load(userImg)
                                .placeholder(R.drawable.icon_image_loading)
                                .into(userImage);

                        txtPostText.setText(postText);

                        if (!TextUtils.isEmpty(originalImageUrl)) {
                            postImageContent.setVisibility(View.VISIBLE);
                            Glide.with(PostActivity.this)
                                    .load(originalImageUrl)
                                    .placeholder(R.drawable.icon_image_loading)
                                    .into(postImageContent);
                        } else {
                            postImageContent.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(PostActivity.this, "Post not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PostActivity.this, "Failed to load post", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadComments() {
        db.collection("Posts").document(postId).collection("comments")
                .orderBy("commentTime", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error loading comments", error);
                        return;
                    }

                    commentList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        commentList.add(comment);
                    }
                    commentAdapter.notifyDataSetChanged();
                });
    }
}
