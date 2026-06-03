package com.example.jobmanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotalJobs, tvCompletedJobs, tvPendingJobs;
    private RecyclerView rvAdminJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Stats TextViews
        tvTotalJobs = findViewById(R.id.tvTotalJobsCount);
        tvCompletedJobs = findViewById(R.id.tvCompletedJobsCount);
        tvPendingJobs = findViewById(R.id.tvPendingJobsCount);
        
        // Job List Setup
        rvAdminJobs = findViewById(R.id.rvAdminJobs);
        rvAdminJobs.setLayoutManager(new LinearLayoutManager(this));
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList);
        rvAdminJobs.setAdapter(jobAdapter);

        // Action Buttons
        MaterialButton btnCreateJob = findViewById(R.id.btnAdminCreateJob);
        MaterialButton btnAssignJob = findViewById(R.id.btnAssignJob);
        MaterialButton btnMonitorProgress = findViewById(R.id.btnMonitorProgress);
        MaterialButton btnViewReports = findViewById(R.id.btnViewReports);
        Button btnLogout = findViewById(R.id.btnLogoutAdmin);

        btnCreateJob.setOnClickListener(v -> startActivity(new Intent(this, CreateJobActivity.class)));
        btnAssignJob.setOnClickListener(v -> startActivity(new Intent(this, AssignJobActivity.class)));
        btnMonitorProgress.setOnClickListener(v -> startActivity(new Intent(this, MonitorProgressActivity.class)));
        btnViewReports.setOnClickListener(v -> startActivity(new Intent(this, ViewReportsActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        jobAdapter.setOnJobClickListener(job -> {
            Intent intent = new Intent(this, JobDetailsActivity.class);
            intent.putExtra("jobId", job.getJobId());
            intent.putExtra("title", job.getTitle());
            intent.putExtra("status", job.getStatus());
            intent.putExtra("description", job.getDescription());
            intent.putExtra("deadline", job.getDeadline());
            intent.putExtra("assignedTo", job.getAssignedTo());
            intent.putExtra("location", job.getLocation());
            intent.putExtra("priority", job.getPriority());
            intent.putExtra("category", job.getCategory());
            intent.putExtra("progress", job.getProgress());
            intent.putExtra("imageUrl", job.getImageUrl());
            intent.putExtra("isAdmin", true); // Pass Admin flag
            startActivity(intent);
        });

        jobAdapter.setOnDeleteClickListener(this::showDeleteConfirmDialog);

        startListeningForJobs();
    }

    private void showDeleteConfirmDialog(Job job) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Job")
                .setMessage("Are you sure you want to delete this job?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("jobs").document(job.getJobId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Job deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startListeningForJobs() {
        db.collection("jobs").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                jobList.clear();
                int total = 0, completed = 0, pending = 0;
                for (QueryDocumentSnapshot document : value) {
                    Job job = document.toObject(Job.class);
                    jobList.add(job);
                    total++;
                    if ("Completed".equalsIgnoreCase(job.getStatus())) completed++;
                    else pending++;
                }
                tvTotalJobs.setText(String.valueOf(total));
                tvCompletedJobs.setText(String.valueOf(completed));
                tvPendingJobs.setText(String.valueOf(pending));
                jobAdapter.notifyDataSetChanged();
            }
        });
    }
}
