package com.example.zyntra;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {
    private ImageView imgCoverPhoto;
    private de.hdodenhof.circleimageview.CircleImageView imgUserDp;
    private TextView txtUserName, txtUserBio, txtUserBOD, txtUserGender, txtUserEmail;
    private Button btnEditProfile, btnShowPosts, btnShowAbout, btnShareUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });   mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind UI components
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnShareUser= findViewById(R.id.btnShareProfile);
        btnShowAbout = findViewById(R.id.btnInformationView);
        btnShowPosts = findViewById(R.id.btnPostsView);
        imgCoverPhoto = findViewById(R.id.imgCoverPhto);
        imgUserDp = findViewById(R.id.imgUserDp);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserBio = findViewById(R.id.txtUserBio);
        txtUserBOD = findViewById(R.id.txtUserBOD);
        txtUserGender = findViewById(R.id.txtUserGender);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(UserProfileActivity.this, ProfileUpdateActivity.class)));
        btnShowPosts.setOnClickListener(v -> {
            btnShowPosts.setBackgroundResource(R.drawable.defualt_button_bg);
            btnShowPosts.setTextColor(getResources().getColor(R.color.white));
            btnShowAbout.setTextColor(getResources().getColor(R.color.black));
            btnShowAbout.setBackgroundColor(getResources().getColor(R.color.transparent));
        });// Load user data
        btnShowAbout.setOnClickListener(v -> {
            btnShowAbout.setBackgroundResource(R.drawable.defualt_button_bg);
            btnShowPosts.setTextColor(getResources().getColor(R.color.black));
            btnShowAbout.setTextColor(getResources().getColor(R.color.white));
            btnShowPosts.setBackgroundColor(getResources().getColor(R.color.transparent));
        });// Load user data
        loadUserProfile();
    }
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                        String bio = documentSnapshot.getString("bio");
                        String birthdate = documentSnapshot.getString("birthdate");
                        String gender = documentSnapshot.getString("gender");
                        String email = documentSnapshot.getString("email");
                        String profilePicUrl = documentSnapshot.getString("profileImageUrl");
                        String coverPhotoUrl = documentSnapshot.getString("coverPhotoUrl");

                        txtUserName.setText(fullName.trim());
                        txtUserBio.setText(bio != null ? bio : "N/A");
                        txtUserBOD.setText(birthdate != null ? birthdate : "N/A");
                        txtUserGender.setText(gender != null ? gender : "N/A");
                        txtUserEmail.setText(email != null ? email : "N/A");

                        // Load images using Glide
                        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            Glide.with(this).load(profilePicUrl).into(imgUserDp);
                        }

                        if (coverPhotoUrl != null && !coverPhotoUrl.isEmpty()) {
                            Glide.with(this).load(coverPhotoUrl).into(imgCoverPhoto);
                        }

                    } else {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}