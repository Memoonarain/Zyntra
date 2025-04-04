package com.example.zyntra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewPost extends AppCompatActivity {
    private EditText txtPostText;
    private ImageView postImageContent;
    private Button btnPost;
    private ImageButton btnAddPhoto;
    private CircleImageView imgUserImg;
    private TextView txtUserName;

    private Uri selectedImageUri = null;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    LoadingDialogue loadingDialogues;
    private final int REQUEST_IMAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        loadingDialogues =new LoadingDialogue(NewPost.this);
        // UI Initialization
        txtPostText = findViewById(R.id.txtPostText);
        postImageContent = findViewById(R.id.postImageContent);
        btnPost = findViewById(R.id.btnPost);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        imgUserImg = findViewById(R.id.imgUserImg);
        txtUserName = findViewById(R.id.txtUserName);
        try {
            MediaManager.get(); // Try to get instance
        } catch (IllegalStateException e) {
            // Not initialized yet, so initialize now
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "du3kpasqd");
            config.put("api_key", "978589238943688");
            config.put("api_secret", "UBV5lxDjn_56OlOQM1CfU8-D2uU");
            MediaManager.init(this, config);
        }


        loadUserProfile();

        btnAddPhoto.setOnClickListener(v -> openImagePicker());

        btnPost.setOnClickListener(v -> {
            String content = txtPostText.getText().toString().trim();
            if (content.isEmpty() && selectedImageUri == null) {
                Toast.makeText(this, "Post cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                btnPost.setEnabled(false);
                loadingDialogues.showLoadingDialog();
                if (selectedImageUri != null) {
                    uploadImageToCloudinary(selectedImageUri, content);
                } else {
                    uploadPostToFirestore(content, null);
                }
            }
        });
    }

    private void loadUserProfile() {
        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("name");
                        String profileUrl = snapshot.getString("profileImageUrl");
                        txtUserName.setText(name);
                        Glide.with(this).load(profileUrl).placeholder(R.drawable.icon_profile).into(imgUserImg);
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            postImageContent.setVisibility(View.VISIBLE);
            postImageContent.setImageURI(selectedImageUri);
        }
    }
    private void uploadImageToCloudinary(Uri imageUri, String postText) {
        try {
            byte[] compressedBytes = compressImage(imageUri);
            if (compressedBytes == null) {
                throw new IOException("Image compression failed.");
            }

            File compressedFile = writeCompressedImageToTempFile(compressedBytes);

            MediaManager.get().upload(compressedFile.getAbsolutePath())
                    .option("folder", "zyntra_posts") // Better folder name for posts
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = resultData.get("secure_url").toString();
                            runOnUiThread(() -> uploadPostToFirestore(postText, imageUrl));
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            loadingDialogues.hideLoadingDialog();
                            Toast.makeText(NewPost.this, "Upload Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            btnPost.setEnabled(true);
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            loadingDialogues.hideLoadingDialog();
                            btnPost.setEnabled(true);
                        }
                    }).dispatch();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Compression or File Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            loadingDialogues.hideLoadingDialog();
            btnPost.setEnabled(true);
        }
    }

    private void uploadPostToFirestore(String postText, @Nullable String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Step 1: Fetch the user's document from 'users' collection
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String userImage = documentSnapshot.getString("profileImageUrl");

                        String fullName = (firstName != null ? firstName : "") +
                                (lastName != null ? " " + lastName : "");
                        if (fullName.trim().isEmpty()) {
                            fullName = "Unknown User";
                        }

                        if (userImage == null) {
                            userImage = ""; // fallback to empty or default avatar URL
                        }

                        // Step 2: Prepare post data
                        Map<String, Object> postMap = new HashMap<>();
                        postMap.put("userId", userId);
                        postMap.put("userName", fullName.trim());
                        postMap.put("userImageUrl", userImage);
                        postMap.put("postText", postText);
                        postMap.put("postImageUrl", imageUrl != null ? imageUrl : "");
                        postMap.put("timestamp", FieldValue.serverTimestamp());

                        // Step 3: Upload post
                        db.collection("Posts").add(postMap)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                                    loadingDialogues.hideLoadingDialog();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to upload post", Toast.LENGTH_SHORT).show();
                                    loadingDialogues.hideLoadingDialog();
                                    btnPost.setEnabled(true);
                                });

                    } else {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                    Log.e("UploadPost", "Error fetching user doc: ", e);
                });
    }
    public byte[] compressImage(Uri imageUri) {
        try {
            // Step 1: Load bitmap from URI
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Step 2: Resize (Optional: to max 1080 width for example)
            int maxWidth = 1080;
            float aspectRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
            int height = (int) (maxWidth * aspectRatio);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, maxWidth, height, true);

            // Step 3: Compress to byte array (JPEG with 70% quality)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70 is good balance
            return baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private File writeCompressedImageToTempFile(byte[] imageData) throws IOException {
        File tempFile = new File(getCacheDir(), "compressed_post_image.jpg");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(imageData);
        fos.flush();
        fos.close();
        return tempFile;
    }


}