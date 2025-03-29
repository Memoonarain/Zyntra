package com.example.zyntra;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmailLogin, edtPasswordLogin;
    private Button btnLoginAccount, btnLoginView, btnSignupView;
    private CheckBox boxShowPassword;
    private TextView txtForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Views
        edtEmailLogin = findViewById(R.id.edtEmailLogin);
        edtPasswordLogin = findViewById(R.id.edtPasswordLogin);
        btnLoginAccount = findViewById(R.id.btnLoginAccount);
        btnLoginView = findViewById(R.id.btnLoginView);
        btnSignupView = findViewById(R.id.btnSignupView);
        boxShowPassword = findViewById(R.id.boxShowPassword);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        // Toggle Password Visibility
        boxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edtPasswordLogin.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                edtPasswordLogin.setSelection(edtPasswordLogin.getText().length());
            } else {
                edtPasswordLogin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                edtPasswordLogin.setSelection(edtPasswordLogin.getText().length());
            }
        });

        // Login Button Action
        btnLoginAccount.setOnClickListener(v -> {
            String email = edtEmailLogin.getText().toString().trim();
            String password = edtPasswordLogin.getText().toString().trim();
            FirebaseAuth mAuth;
// ...
// Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                edtPasswordLogin.setError("Password should be at least 6 characters");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmailLogin.setError("Invalid email format");
                return;
            }            // TODO: Replace with real authentication logic (Firebase/Auth API)
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        // Switch to Signup View
        btnSignupView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class); // Replace with your actual signup activity
            startActivity(intent);
        });

        // Forgot Password
        txtForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Password recovery is not implemented yet.", Toast.LENGTH_SHORT).show()
        );
    }
}
