package com.example.jobmanagementsystem;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class CreateJobActivity extends AppCompatActivity {

    private TextInputEditText etJobTitle, etJobDesc, etAssignTo;
    private MaterialButton btnSubmitJob;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_job);

        db = FirebaseFirestore.getInstance();

        etJobTitle = findViewById(R.id.etJobTitle);
        etJobDesc = findViewById(R.id.etJobDesc);
        etAssignTo = findViewById(R.id.etAssignTo);
        btnSubmitJob = findViewById(R.id.btnSubmitJob);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        btnSubmitJob.setOnClickListener(v -> {
            String title = etJobTitle.getText().toString().trim();
            String desc = etJobDesc.getText().toString().trim();
            String assignTo = etAssignTo.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty() || assignTo.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String jobId = UUID.randomUUID().toString();
            Job job = new Job(jobId, title, desc, "Pending", assignTo);

            db.collection("jobs").document(jobId)
                    .set(job)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Job Created and Assigned!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}