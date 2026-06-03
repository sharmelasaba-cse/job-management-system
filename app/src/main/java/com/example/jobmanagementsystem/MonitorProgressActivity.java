package com.example.jobmanagementsystem;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonitorProgressActivity extends AppCompatActivity {

    private RecyclerView rvJobs;
    private JobAdapter adapter;
    private List<Job> jobList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_progress);

        db = FirebaseFirestore.getInstance();
        rvJobs = findViewById(R.id.rvJobs);
        rvJobs.setLayoutManager(new LinearLayoutManager(this));

        jobList = new ArrayList<>();
        adapter = new JobAdapter(jobList);
        adapter.setMonitorMode(true); // Enable progress bar and percentage display
        rvJobs.setAdapter(adapter);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        fetchRunningJobs();
    }

    private void fetchRunningJobs() {
        // Monitor Running jobs (In Progress or Assigned)
        db.collection("jobs")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        jobList.clear();
                        Date today = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                        for (QueryDocumentSnapshot doc : value) {
                            Job job = doc.toObject(Job.class);
                            
                            // Check for delayed jobs alert
                            if (!"Completed".equalsIgnoreCase(job.getStatus()) && job.getDeadline() != null) {
                                try {
                                    Date deadlineDate = sdf.parse(job.getDeadline());
                                    if (deadlineDate != null && deadlineDate.before(today)) {
                                        // You could add a flag or visual indicator for delayed jobs
                                        // For now, we'll just show them in the list
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            
                            jobList.add(job);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
