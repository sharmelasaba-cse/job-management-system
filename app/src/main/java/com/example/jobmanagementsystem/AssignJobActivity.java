package com.example.jobmanagementsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignJobActivity extends AppCompatActivity {

    private RecyclerView rvAssignJobs;
    private AutoCompleteTextView actvWorkers;
    private JobAdapter adapter;
    private List<Job> jobList;
    private List<WorkerInfo> workerList;
    private List<String> workerDisplayStrings;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private WorkerInfo selectedWorker = null;

    private static class WorkerInfo {
        String name;
        String email;
        String uid; // Added UID for secure mapping
        WorkerInfo(String name, String email, String uid) {
            this.name = name;
            this.email = email;
            this.uid = uid;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_job);

        db = FirebaseFirestore.getInstance();
        
        actvWorkers = findViewById(R.id.actvWorkers);
        rvAssignJobs = findViewById(R.id.rvAssignJobs);
        progressBar = findViewById(R.id.assignProgressBar);
        
        rvAssignJobs.setLayoutManager(new LinearLayoutManager(this));

        jobList = new ArrayList<>();
        workerList = new ArrayList<>();
        workerDisplayStrings = new ArrayList<>();
        
        adapter = new JobAdapter(jobList);
        adapter.setOnJobClickListener(this::confirmAssignment);
        rvAssignJobs.setAdapter(adapter);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        actvWorkers.setOnItemClickListener((parent, view, position, id) -> {
            // Finding the selected worker by display string to handle filtered lists
            String selectedString = (String) parent.getItemAtPosition(position);
            for (WorkerInfo worker : workerList) {
                if ((worker.name + " (" + worker.email + ")").equals(selectedString)) {
                    selectedWorker = worker;
                    break;
                }
            }
        });

        startListeningForWorkers();
        startListeningForUnassignedJobs();
    }

    private void startListeningForWorkers() {
        db.collection("users").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                workerList.clear();
                workerDisplayStrings.clear();

                for (QueryDocumentSnapshot doc : value) {
                    String uid = doc.getId(); // Use document ID as the UID
                    String email = doc.getString("email");
                    String name = doc.getString("fullName");
                    if (name == null) name = doc.getString("name");
                    String role = doc.getString("role");
                    
                    if (email != null && !"Admin".equalsIgnoreCase(role)) {
                        String displayName = (name != null && !name.isEmpty()) ? name : email.split("@")[0];
                        workerList.add(new WorkerInfo(displayName, email, uid));
                        workerDisplayStrings.add(displayName + " (" + email + ")");
                    }
                }
                
                ArrayAdapter<String> workerAdapter = new ArrayAdapter<>(this, 
                        android.R.layout.simple_dropdown_item_1line, workerDisplayStrings);
                actvWorkers.setAdapter(workerAdapter);
            }
        });
    }

    private void startListeningForUnassignedJobs() {
        db.collection("jobs").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                jobList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Job job = doc.toObject(Job.class);
                    // Jobs are unassigned if assignedTo is null or "Not Assigned"
                    if (job.getAssignedTo() == null || "Not Assigned".equalsIgnoreCase(job.getAssignedTo())) {
                        jobList.add(job);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void confirmAssignment(Job job) {
        if (selectedWorker == null) {
            Toast.makeText(this, "Please select an employee from the dropdown list", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Confirm Assignment")
                .setMessage("Assign '" + job.getTitle() + "' to " + selectedWorker.name + "?")
                .setPositiveButton("Assign", (d, w) -> updateJobAssignment(job.getJobId(), selectedWorker.uid, selectedWorker.name))
                .setNegativeButton("Cancel", null).show();
    }

    private void updateJobAssignment(String jobId, String workerUid, String name) {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> updates = new HashMap<>();
        updates.put("assignedTo", workerUid); // SECURE: Using UID instead of Email
        updates.put("assignedToName", name);
        updates.put("status", "Assigned");

        db.collection("jobs").document(jobId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Job assigned successfully!", Toast.LENGTH_SHORT).show();
                    actvWorkers.setText("", false);
                    selectedWorker = null;
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Assignment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
