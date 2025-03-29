package com.example.zyntra;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileUpdateActivity extends AppCompatActivity {

    EditText edtFirstName, edtLastName,edtBio,edtEmail;
    TextView txtBirthdate, txtGender;
    ImageView edtBirthDate, edtGender, imgEdtUserDp;
    ImageView userDp;
    Button btnUpdate;
    Uri imageUri;
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseUser currentUser;

    String selectedGender = "";
    String selectedBirthdate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "du3kpasqd");
            config.put("api_key", "978589238943688");
            config.put("api_secret", "UBV5lxDjn_56OlOQM1CfU8-D2uU");
            MediaManager.init(this, config);
        edtBio = findViewById(R.id.edtBio);
        edtEmail = findViewById(R.id.edtEmailLogin);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        txtBirthdate = findViewById(R.id.txtbirthdate);
        txtGender = findViewById(R.id.txtGender);
        edtBirthDate = findViewById(R.id.edtBirthDate);
        edtGender = findViewById(R.id.edtGender);
        imgEdtUserDp = findViewById(R.id.imgEdtUserDp);
        userDp = findViewById(R.id.user_dp);
        btnUpdate = findViewById(R.id.btnCreateAccount);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Updating Profile...");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        currentUser = mAuth.getCurrentUser();

        imgEdtUserDp.setOnClickListener(v -> chooseImage());
        edtBirthDate.setOnClickListener(v -> openDatePicker());
        edtGender.setOnClickListener(v -> selectGender());

        btnUpdate.setOnClickListener(v -> updateProfile());
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                userDp.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedBirthdate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            txtBirthdate.setText(selectedBirthdate);
        }, year, month, day);
        dialog.show();
    }

    private void selectGender() {
        String[] genderOptions = {"Male", "Female", "Other"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Choose Gender")
                .setItems(genderOptions, (dialog, which) -> {
                    selectedGender = genderOptions[which];
                    txtGender.setText(selectedGender);
                }).show();
    }
    private void updateProfile() {
        String firstName = edtFirstName.getText().toString().trim();
        String lastName = edtLastName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String Bio = edtBio.getText().toString();
        if (firstName.isEmpty() || lastName.isEmpty() || selectedBirthdate.isEmpty() || selectedGender.isEmpty() || Bio.isEmpty()) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail="";
        if (currentUser != null) {
             userEmail = currentUser.getEmail();
            // You can now use userEmail as needed
        } else {
            // User not signed in
            startActivity(new Intent(ProfileUpdateActivity.this, SignUpActivity.class));
            finish();
        }

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("firstName", firstName);
        profileData.put("lastName", lastName);
        profileData.put("birthdate", selectedBirthdate);
        profileData.put("gender", selectedGender);
        profileData.put("isProfileCompleted", true);
        profileData.put("bio", Bio);
        profileData.put("email", userEmail);
        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                    .option("folder", "zyntra_profiles")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = resultData.get("secure_url").toString();
                            profileData.put("profileImageUrl", imageUrl);
                            updateData(profileData);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileUpdateActivity.this, "Cloudinary Upload Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    }).dispatch();
        } else {
            updateData(profileData); // No image selected
        }
    }

    private void updateData(Map<String, Object> profileData){
        db.collection("users").document(currentUser.getUid())
                .set(profileData)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ProfileUpdateActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
}
