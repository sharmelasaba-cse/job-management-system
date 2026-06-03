package com.example.jobmanagementsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class JobDetailsActivity extends AppCompatActivity {

    private TextView tvTitle, tvStatus, tvDesc, tvDeadline, tvPriority, tvLocation, tvProgressPercent, tvInstructions;
    private ImageView ivJobDetail;
    private MaterialButton btnUpdateProgress, btnCompleteJob;
    private SeekBar seekBarProgress;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        jobId = getIntent().getStringExtra("jobId");

        // Initialize UI Components
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvInstructions = findViewById(R.id.tvDetailInstructions);
        tvDeadline = findViewById(R.id.tvDetailDeadline);
        tvPriority = findViewById(R.id.tvDetailPriority);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvProgressPercent = findViewById(R.id.tvDetailProgressPercent);
        ivJobDetail = findViewById(R.id.ivDetailImage);
        
        seekBarProgress = findViewById(R.id.seekBarProgress);
        btnUpdateProgress = findViewById(R.id.btnUpdateProgress);
        btnCompleteJob = findViewById(R.id.btnCompleteJob);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Load data from Intent
        displayData();

        // UI Interactions
        setupListeners();
    }

    private void displayData() {
        String title = getIntent().getStringExtra("title");
        String status = getIntent().getStringExtra("status");
        String description = getIntent().getStringExtra("description");
        String instructions = getIntent().getStringExtra("instructions");
        String deadline = getIntent().getStringExtra("deadline");
        String priority = getIntent().getStringExtra("priority");
        String location = getIntent().getStringExtra("location");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        int progress = getIntent().getIntExtra("progress", 0);

        tvTitle.setText(title != null ? title : "No Title");
        tvStatus.setText(status != null ? status : "Pending");
        tvDesc.setText(description != null ? description : "No description.");
        
        if (tvInstructions != null) {
            tvInstructions.setText(instructions != null && !instructions.isEmpty() ? instructions : "No special instructions.");
        }

        tvDeadline.setText("Deadline: " + (deadline != null ? deadline : "N/A"));
        tvPriority.setText("Priority: " + (priority != null ? priority : "Medium"));
        tvLocation.setText("Location: " + (location != null ? location : "N/A"));
        
        updateProgressUI(progress);
        updateUIState(status);

        if (imageUrl != null && !imageUrl.isEmpty() && ivJobDetail != null) {
            ivJobDetail.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).into(ivJobDetail);
        }
    }

    private void setupListeners() {
        if (seekBarProgress != null) {
            seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (tvProgressPercent != null) {
                        tvProgressPercent.setText("Progress: " + progress + "%");
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (btnUpdateProgress != null) {
            btnUpdateProgress.setOnClickListener(v -> {
                int progress = seekBarProgress.getProgress();
                updateJobProgressInFirestore(progress);
            });
        }

        if (btnCompleteJob != null) {
            btnCompleteJob.setOnClickListener(v -> completeJob());
        }
    }

    private void updateJobProgressInFirestore(int progress) {
        if (jobId == null) {
            Toast.makeText(this, "Job ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("progress", progress);
        updates.put("status", "In Progress");

        db.collection("jobs").document(jobId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Progress updated", Toast.LENGTH_SHORT).show();
                    tvStatus.setText("In Progress");
                    updateProgressUI(progress);
                    updateUIState("In Progress");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update progress: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void completeJob() {
        if (jobId == null) {
            Toast.makeText(this, "Job ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        WriteBatch batch = db.batch();

        // 1. Update job status and progress
        Map<String, Object> jobUpdates = new HashMap<>();
        jobUpdates.put("status", "Completed");
        jobUpdates.put("progress", 100);
        batch.update(db.collection("jobs").document(jobId), jobUpdates);

        // 2. Increment completedJobs counter for the user
        batch.update(db.collection("users").document(uid), "completedJobs", FieldValue.increment(1));

        // Commit batch
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Job Completed Successfully!", Toast.LENGTH_SHORT).show();
            tvStatus.setText("Completed");
            updateProgressUI(100);
            updateUIState("Completed");
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to complete job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUIState(String status) {
        boolean isCompleted = "Completed".equalsIgnoreCase(status);
        if (seekBarProgress != null) seekBarProgress.setEnabled(!isCompleted);
        if (btnUpdateProgress != null) btnUpdateProgress.setEnabled(!isCompleted);
        if (btnCompleteJob != null) {
            btnCompleteJob.setVisibility(isCompleted ? View.GONE : View.VISIBLE);
        }
        
        if (isCompleted) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
        } else if ("In Progress".equalsIgnoreCase(status)) {
            tvStatus.setBackgroundResource(R.drawable.bg_status_progress);
        } else {
            tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
        }
    }

    private void updateProgressUI(int progress) {
        if (tvProgressPercent != null) {
            tvProgressPercent.setText("Progress: " + progress + "%");
        }
        if (seekBarProgress != null) {
            seekBarProgress.setProgress(progress);
        }
    }
}
