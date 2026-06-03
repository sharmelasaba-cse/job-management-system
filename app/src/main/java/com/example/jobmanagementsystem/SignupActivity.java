package com.example.jobmanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etFullName, etUsername, etMobile, etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLoginLink;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etSignupUsername);
        etMobile = findViewById(R.id.etSignupMobile);
        etEmail = findViewById(R.id.etSignupEmail);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        progressBar = findViewById(R.id.progressBar);

        btnSignup.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim().toLowerCase();
            String mobile = etMobile.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty() || username.isEmpty() || mobile.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(SignupActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            checkUniquenessAndSignup(email, password, fullName, username, mobile);
        });

        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void checkUniquenessAndSignup(String email, String password, String fullName, String username, String mobile) {
        btnSignup.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Check if username already exists
        db.collection("users").whereEqualTo("username", username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        showError("Username is already taken");
                    } else {
                        // Check if mobile already exists
                        db.collection("users").whereEqualTo("mobile", mobile).get()
                                .addOnCompleteListener(mobileTask -> {
                                    if (mobileTask.isSuccessful() && !mobileTask.getResult().isEmpty()) {
                                        showError("Mobile number is already registered");
                                    } else {
                                        createAccount(email, password, fullName, username, mobile);
                                    }
                                });
                    }
                });
    }

    private void createAccount(String email, String password, String fullName, String username, String mobile) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserToFirestore(userId, fullName, username, mobile, email);
                    } else {
                        showError("Signup Failed: " + task.getException().getMessage());
                    }
                });
    }

    private void saveUserToFirestore(String userId, String fullName, String username, String mobile, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", userId); // Primary Key
        user.put("fullName", fullName);
        user.put("username", username);
        user.put("mobile", mobile);
        user.put("email", email);
        user.put("role", "Employee"); 
        user.put("completedJobs", 0);

        db.collection("users").document(userId) // Use UID as document ID
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignupActivity.this, "Signup Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showError("Error saving profile: " + e.getMessage());
                });
    }

    private void showError(String message) {
        btnSignup.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
