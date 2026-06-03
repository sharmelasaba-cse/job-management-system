package com.example.jobmanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvAssignedCount, tvPendingCount, tvCompletedCount;
    private JobAdapter jobAdapter;
    private List<Job> jobList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration userListener, jobsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        tvWelcome = findViewById(R.id.tvWelcomeEmployee);
        tvAssignedCount = findViewById(R.id.tvAssignedCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);

        // Initialize RecyclerView for Tasks
        RecyclerView rvAssignedTasks = findViewById(R.id.rvAssignedTasks);
        if (rvAssignedTasks != null) {
            rvAssignedTasks.setLayoutManager(new LinearLayoutManager(this));
        }
        
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList);
        jobAdapter.setMonitorMode(false); 
        jobAdapter.setEmployeeView(true);

        if (rvAssignedTasks != null) {
            rvAssignedTasks.setAdapter(jobAdapter);
        }

        // Job click to view details
        jobAdapter.setOnJobClickListener(job -> {
            Intent intent = new Intent(this, JobDetailsActivity.class);
            intent.putExtra("jobId", job.getJobId());
            intent.putExtra("title", job.getTitle());
            intent.putExtra("status", job.getStatus());
            intent.putExtra("description", job.getDescription());
            intent.putExtra("instructions", job.getInstructions());
            intent.putExtra("deadline", job.getDeadline());
            intent.putExtra("assignedTo", job.getAssignedTo());
            intent.putExtra("assignedToName", job.getAssignedToName());
            intent.putExtra("location", job.getLocation());
            intent.putExtra("priority", job.getPriority());
            intent.putExtra("category", job.getCategory());
            intent.putExtra("progress", job.getProgress());
            intent.putExtra("imageUrl", job.getImageUrl());
            intent.putExtra("isAdmin", false);
            startActivity(intent);
        });

        // Profile button logic
        ImageButton btnProfile = findViewById(R.id.btnEmployeeProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> startActivity(new Intent(EmployeeDashboardActivity.this, ProfileActivity.class)));
        }

        // Logout logic
        findViewById(R.id.btnLogoutEmployee).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(EmployeeDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        checkUserAndFetchData();
    }

    private void checkUserAndFetchData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        userListener = db.collection("users").document(uid).addSnapshotListener((doc, error) -> {
            if (error != null || doc == null || !doc.exists()) return;
            
            String fullName = doc.getString("fullName");
            String email = doc.getString("email");
            String username = doc.getString("username");
            if (fullName == null) fullName = doc.getString("name");

            tvWelcome.setText("Welcome, " + (fullName != null ? fullName : "User") + "!");
            
            // Build list of identifiers to fetch tasks assigned to this user
            List<String> identifiers = new ArrayList<>();
            identifiers.add(uid);
            if (email != null) identifiers.add(email);
            if (username != null) identifiers.add(username);
            if (fullName != null) identifiers.add(fullName);

            fetchTasksForEmployee(identifiers);
        });
    }

    private void fetchTasksForEmployee(List<String> identifiers) {
        if (jobsListener != null) {
            jobsListener.remove();
        }

        // Query jobs where assignedTo matches any of the user's identifiers
        jobsListener = db.collection("jobs")
                .whereIn("assignedTo", identifiers)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    
                    jobList.clear();
                    int assigned = 0;
                    int pendingCount = 0;
                    int completedCount = 0;

                    for (QueryDocumentSnapshot doc : value) {
                        Job job = doc.toObject(Job.class);
                        jobList.add(job);
                        
                        String status = job.getStatus() != null ? job.getStatus() : "";

                        if ("Completed".equalsIgnoreCase(status)) {
                            completedCount++;
                        } else {
                            assigned++;
                            if ("Pending".equalsIgnoreCase(status) || "Assigned".equalsIgnoreCase(status)) {
                                pendingCount++;
                            }
                        }
                    }
                    
                    if (tvAssignedCount != null) tvAssignedCount.setText(String.valueOf(assigned));
                    if (tvPendingCount != null) tvPendingCount.setText(String.valueOf(pendingCount));
                    if (tvCompletedCount != null) tvCompletedCount.setText(String.valueOf(completedCount));
                    
                    jobAdapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) userListener.remove();
        if (jobsListener != null) jobsListener.remove();
    }
}
