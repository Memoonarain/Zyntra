package com.example.zyntra;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditPostActivity extends AppCompatActivity {

    private EditText txtPostText;
    private ImageView postImageContent;
    private Button btnPost;

    private FirebaseFirestore firestore;
    private String postId;
    private String originalImageUrl; // Just for showing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        txtPostText = findViewById(R.id.txtPostText);
        postImageContent = findViewById(R.id.postImageContent);
        btnPost = findViewById(R.id.btnPost);

        firestore = FirebaseFirestore.getInstance();

        postId = getIntent().getStringExtra("postId");

        Log.e("Eroor Checking", "Post Id: "+postId);
//        if (postId == null) {
//            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }

        loadPostDetails();

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePostTextOnly();
            }
        });
    }

    private void loadPostDetails() {
        firestore.collection("Posts").document(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String postText = snapshot.getString("postText");
                            originalImageUrl = snapshot.getString("postImageUrl");

                            txtPostText.setText(postText);

                            if (!TextUtils.isEmpty(originalImageUrl)) {
                                postImageContent.setVisibility(View.VISIBLE);
                                Glide.with(EditPostActivity.this)
                                        .load(originalImageUrl)
                                        .placeholder(R.drawable.icon_image_loading)
                                        .into(postImageContent);
                            } else {
                                postImageContent.setVisibility(View.GONE);
                            }

                        } else {
                            Toast.makeText(EditPostActivity.this, "Post not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditPostActivity.this, "Failed to load post", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updatePostTextOnly() {
        String updatedText = txtPostText.getText().toString().trim();

        firestore.collection("Posts").document(postId)
                .update("postText", updatedText)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(EditPostActivity.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // go back to previous screen
                })
                .addOnFailureListener(e -> Toast.makeText(EditPostActivity.this, "Failed to update post", Toast.LENGTH_SHORT).show());
    }
}
