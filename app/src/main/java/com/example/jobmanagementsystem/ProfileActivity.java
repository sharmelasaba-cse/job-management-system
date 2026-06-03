package com.example.jobmanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvCompletedJobsCount;
    private TextInputEditText etProfileName, etProfilePhone;
    private MaterialButton btnUpdateProfile, btnLogoutProfile;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        currentUid = user.getUid();

        // Initialize Views
        tvCompletedJobsCount = findViewById(R.id.tvCompletedJobsCount);
        etProfileName = findViewById(R.id.etProfileName);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);
        progressBar = findViewById(R.id.profileProgressBar);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        // Setup Toolbar
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Load user data
        loadUserProfile();

        // Update Profile logic
        btnUpdateProfile.setOnClickListener(v -> validateAndUpdateProfile());

        // Logout logic
        btnLogoutProfile.setOnClickListener(v -> logout());
    }

    private void loadUserProfile() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("fullName");
                        if (name == null) name = documentSnapshot.getString("name");
                        
                        String phone = documentSnapshot.getString("phone");
                        if (phone == null) phone = documentSnapshot.getString("mobile");
                        
                        Long completedJobs = documentSnapshot.getLong("completedJobs");

                        if (etProfileName != null) etProfileName.setText(name != null ? name : "");
                        if (etProfilePhone != null) etProfilePhone.setText(phone != null ? phone : "");
                        
                        if (tvCompletedJobsCount != null) {
                            tvCompletedJobsCount.setText(String.valueOf(completedJobs != null ? completedJobs : 0));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void validateAndUpdateProfile() {
        String newName = etProfileName.getText() != null ? etProfileName.getText().toString().trim() : "";
        String newPhone = etProfilePhone.getText() != null ? etProfilePhone.getText().toString().trim() : "";

        // Validation
        if (newName.isEmpty()) {
            etProfileName.setError("Name cannot be empty");
            etProfileName.requestFocus();
            return;
        }

        if (newPhone.length() != 11) {
            etProfilePhone.setError("Phone number must be 11 digits");
            etProfilePhone.requestFocus();
            return;
        }

        updateProfile(newName, newPhone);
    }

    private void updateProfile(String name, String phone) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnUpdateProfile.setEnabled(false);

        // Save to Firestore
        saveToFirestore(name, phone);
    }

    private void saveToFirestore(String name, String phone) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("fullName", name); 
        updates.put("phone", phone);
        updates.put("mobile", phone);

        db.collection("users").document(currentUid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    btnUpdateProfile.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    btnUpdateProfile.setEnabled(true);
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
