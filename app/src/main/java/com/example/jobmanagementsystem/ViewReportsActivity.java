package com.example.jobmanagementsystem;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ViewReportsActivity extends AppCompatActivity {

    private TextView tvReportTotal, tvReportCompleted, tvReportMonthly, tvReportCompletionRate, tvPerformanceReport;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reports);

        db = FirebaseFirestore.getInstance();

        tvReportTotal = findViewById(R.id.tvReportTotal);
        tvReportCompleted = findViewById(R.id.tvReportCompleted);
        tvReportMonthly = findViewById(R.id.tvReportMonthly);
        tvReportCompletionRate = findViewById(R.id.tvReportCompletionRate);
        tvPerformanceReport = findViewById(R.id.tvPerformanceReport);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        fetchReportData();
    }

    private void fetchReportData() {
        db.collection("jobs").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                int total = 0;
                int completed = 0;
                int monthlyCount = 0;
                
                Calendar cal = Calendar.getInstance();
                int currentMonth = cal.get(Calendar.MONTH);
                int currentYear = cal.get(Calendar.YEAR);

                // Map to store: WorkerName -> [TotalAssigned, TotalCompleted]
                Map<String, int[]> workerStats = new HashMap<>();

                for (QueryDocumentSnapshot document : value) {
                    Job job = document.toObject(Job.class);
                    total++;

                    // 1. Completion check
                    if ("Completed".equalsIgnoreCase(job.getStatus())) {
                        completed++;
                    }

                    // 2. Monthly check
                    if (job.getCreatedAt() != null) {
                        cal.setTime(job.getCreatedAt().toDate());
                        if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
                            monthlyCount++;
                        }
                    }

                    // 3. Employee Performance
                    String workerName = job.getAssignedToName();
                    if (workerName != null && !workerName.isEmpty()) {
                        if (!workerStats.containsKey(workerName)) {
                            workerStats.put(workerName, new int[]{0, 0});
                        }
                        int[] stats = workerStats.get(workerName);
                        stats[0]++; // Total assigned
                        if ("Completed".equalsIgnoreCase(job.getStatus())) {
                            stats[1]++; // Total completed
                        }
                    }
                }

                // Update UI
                tvReportTotal.setText("Total Jobs: " + total);
                tvReportCompleted.setText("Completed Jobs: " + completed);
                tvReportMonthly.setText("Jobs Created This Month: " + monthlyCount);

                if (total > 0) {
                    int rate = (completed * 100) / total;
                    tvReportCompletionRate.setText("Overall Completion Rate: " + rate + "%");
                } else {
                    tvReportCompletionRate.setText("Overall Completion Rate: 0%");
                }

                // Generate Performance String
                StringBuilder performanceBuilder = new StringBuilder();
                if (workerStats.isEmpty()) {
                    performanceBuilder.append("No performance data available.");
                } else {
                    for (Map.Entry<String, int[]> entry : workerStats.entrySet()) {
                        String name = entry.getKey();
                        int[] stats = entry.getValue();
                        int individualRate = (stats[0] > 0) ? (stats[1] * 100) / stats[0] : 0;
                        performanceBuilder.append("• ").append(name)
                                .append(": ").append(stats[1]).append("/").append(stats[0])
                                .append(" completed (").append(individualRate).append("%)\n\n");
                    }
                }
                tvPerformanceReport.setText(performanceBuilder.toString());
            }
        });
    }
}
