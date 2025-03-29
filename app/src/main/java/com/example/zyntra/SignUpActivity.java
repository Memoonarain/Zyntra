package com.example.zyntra;

import androidx.core.graphics.Insets;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtFirstName, edtLastName, edtEmail, edtPassword, edtConfirmPassword, edtBio;
    private TextView txtBirthDate, txtGender;
    private ImageView imgUserDp, imgEdtBirthDate, imgEdtGender,imgEdtUserDp;
    private CheckBox boxShowPassword;
    private Button btnCreateAccount, btnLoginView, btnSignupView;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupListeners();
    }

    private void initViews() {
        edtBio = findViewById(R.id.edtBio);
        txtGender = findViewById(R.id.txtGender);
        txtBirthDate = findViewById(R.id.txtbirthdate);
        imgEdtBirthDate = findViewById(R.id.edtBirthDate);
        imgEdtGender= findViewById(R.id.edtGender);
        imgEdtUserDp = findViewById(R.id.imgEdtUserDp);
        imgUserDp = findViewById(R.id.user_dp);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtEmail = findViewById(R.id.edtEmailLogin);
        edtPassword = findViewById(R.id.edtPasswordLogin1);
        edtConfirmPassword = findViewById(R.id.edtPasswordLogin);
        boxShowPassword = findViewById(R.id.boxShowPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnLoginView = findViewById(R.id.btnLoginView);
        btnSignupView = findViewById(R.id.btnSignupView);
        mAuth = FirebaseAuth.getInstance();
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
    }

    private void setupListeners() {
        imgEdtUserDp.setOnClickListener(v -> selectImage());
        imgEdtBirthDate.setOnClickListener(v -> showDatePicker());
        imgEdtGender.setOnClickListener(v -> {

                String[] genders = {"Male", "Female", "Other"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Gender")
                        .setItems(genders, (dialog, which) -> txtGender.setText(genders[which]));
                builder.show();
        });
        boxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int inputType = isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;

            edtPassword.setInputType(inputType);
            edtConfirmPassword.setInputType(inputType);

            // Move cursor to end
            edtPassword.setSelection(edtPassword.getText().length());
            edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
        });

        btnCreateAccount.setOnClickListener(view -> {
            if (validateInputs()) {
                createAccount(edtEmail.getText().toString().trim(),
                    edtPassword.getText().toString().trim());
                // Replace this with Firebase or database logic
                Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        btnLoginView.setOnClickListener(view -> {
            // Navigate to LoginActivity
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        });

        btnSignupView.setOnClickListener(view -> {
            // Already on Signup page
            Toast.makeText(SignUpActivity.this, "Already on Signup page", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInputs() {
        String firstName = edtFirstName.getText().toString().trim();
        String lastName = edtLastName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();
        String Bio = edtBio.getText().toString();
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()|| txtGender.getText().toString().isEmpty()||Bio.isEmpty()|| txtBirthDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Invalid email format");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            return false;
        }

        if (password.length() < 6) {
            edtPassword.setError("Password should be at least 6 characters");
            return false;
        }

        return true;
    }
    private void createAccount(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignUpActivity.this,
                                "Account created for: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        if (user != null) {
                            uploadUserProfile(user.getUid());
                        }

                        // Navigate to main/dashboard activity
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imgUserDp.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, y, m, d) -> txtBirthDate.setText(d + "/" + (m + 1) + "/" + y),
                year, month, day);
        dialog.show();
    }
    private void uploadUserProfile(String userId) {
        String firstName = edtFirstName.getText().toString().trim();
        String lastName = edtLastName.getText().toString().trim();
        String gender = txtGender.getText().toString();
        String birthDate = txtBirthDate.getText().toString();
        String Bio = edtBio.getText().toString().trim();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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

                            saveUserToFirestore(db, userId, firstName, lastName, gender, birthDate, imageUrl,Bio);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Toast.makeText(SignUpActivity.this, "Cloudinary Upload Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    }).dispatch();
        } else {
            // No image selected
            saveUserToFirestore(db, userId, firstName, lastName, gender, birthDate, null,Bio);
        }
    }

    private void saveUserToFirestore(FirebaseFirestore db, String userId, String firstName,
                                     String lastName, String gender, String birthDate, String imageUrl,String Bio) {

        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("gender", gender);
        userData.put("birthDate", birthDate);
        userData.put("profileImageUrl", imageUrl);
        userData.put("email", edtEmail.getText().toString().trim());
        userData.put("bio", Bio);
        userData.put("isProfileCompleted", true);
        db.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    userData.put("profileCompleted", true); // Only added if profile is saved successfully

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private long convertDateToMillis(String dateStr) {
        try {
            String[] parts = dateStr.split("/");
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0]));
            cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1); // Month is 0-based
            cal.set(Calendar.YEAR, Integer.parseInt(parts[2]));
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    private String convertMillisToDate(Long millis) {
        if (millis == null) return "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // month is 0-based
        int year = calendar.get(Calendar.YEAR);
        return day + "/" + month + "/" + year;
    }
}
