package com.example.jobmanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AssignedJobsActivity extends AppCompatActivity {

    private RecyclerView rvAssignedJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigned_jobs);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvAssignedJobs = findViewById(R.id.rvAssignedJobs);
        rvAssignedJobs.setLayoutManager(new LinearLayoutManager(this));
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList);
        
        jobAdapter.setOnJobClickListener(job -> {
            Intent intent = new Intent(AssignedJobsActivity.this, JobDetailsActivity.class);
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
            startActivity(intent);
        });

        jobAdapter.setOnDeleteClickListener(job -> {
            // Check role before allowing delete
            checkRoleAndDelete(job);
        });
        
        rvAssignedJobs.setAdapter(jobAdapter);

        checkUserRoleAndFetch();
    }

    private void checkUserRoleAndFetch() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String role = doc.getString("role");
                if ("Admin".equalsIgnoreCase(role)) {
                    fetchAllAssignedJobs();
                } else {
                    String email = doc.getString("email");
                    String username = doc.getString("username");
                    String fullName = doc.getString("fullName");
                    fetchMyJobs(email, username, fullName);
                }
            } else {
                // Fallback for safety
                fetchMyJobs(mAuth.getCurrentUser().getEmail(), null, null);
            }
        });
    }

    private void fetchAllAssignedJobs() {
        // Admin sees all jobs that have been assigned to someone
        db.collection("jobs")
                .whereNotEqualTo("assignedTo", "Not Assigned")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        jobList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            jobList.add(document.toObject(Job.class));
                        }
                        jobAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void fetchMyJobs(String email, String username, String fullName) {
        List<String> ids = new ArrayList<>();
        if (email != null) ids.add(email);
        if (username != null) ids.add(username);
        if (fullName != null) ids.add(fullName);

        if (ids.isEmpty()) return;

        db.collection("jobs")
                .whereIn("assignedTo", ids)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        jobList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            jobList.add(document.toObject(Job.class));
                        }
                        jobAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void checkRoleAndDelete(Job job) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if ("Admin".equalsIgnoreCase(doc.getString("role"))) {
                db.collection("jobs").document(job.getJobId()).delete()
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Job deleted", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Only Admin can delete jobs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
