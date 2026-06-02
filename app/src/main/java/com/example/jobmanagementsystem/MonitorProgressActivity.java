package com.example.jobmanagementsystem;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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
        rvJobs.setAdapter(adapter);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        fetchJobs();
    }

    private void fetchJobs() {
        db.collection("jobs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                jobList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Job job = document.toObject(Job.class);
                    jobList.add(job);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to load jobs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}