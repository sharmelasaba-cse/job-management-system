package com.example.jobmanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText etIdentifier, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etIdentifier = findViewById(R.id.etIdentifier);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> {
            String identifier = etIdentifier.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your details", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(identifier, password);
        });

        findViewById(R.id.tvSignupLink).setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    private void performLogin(String identifier, String password) {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // If it's a valid email, login directly
        if (Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            signIn(identifier, password);
        } else {
            // Otherwise, look up the email by username in Firestore
            db.collection("users")
                    .whereEqualTo("username", identifier.toLowerCase())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            String email = null;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                email = doc.getString("email");
                            }
                            if (email != null) {
                                signIn(email, password);
                            } else {
                                showError("Email mapping error. Use email to login.");
                            }
                        } else {
                            // If not found by username, try searching by mobile number
                            db.collection("users")
                                    .whereEqualTo("mobile", identifier)
                                    .get()
                                    .addOnCompleteListener(mobileTask -> {
                                        if (mobileTask.isSuccessful() && mobileTask.getResult() != null && !mobileTask.getResult().isEmpty()) {
                                            String email = mobileTask.getResult().getDocuments().get(0).getString("email");
                                            signIn(email, password);
                                        } else {
                                            showError("User not found. Please sign up.");
                                        }
                                    });
                        }
                    });
        }
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, RoleSelectionActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}