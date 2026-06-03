package com.example.jobmanagementsystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.UUID;

public class CreateJobActivity extends AppCompatActivity {

    private TextInputEditText etJobTitle, etJobDesc, etDeadline, etLocation, etInstructions;
    private AutoCompleteTextView actvCategory;
    private MaterialButtonToggleGroup toggleGroupPriority;
    private MaterialButton btnSubmitJob, btnSelectImage;
    private ImageView ivJobPreview;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri imageUri;
    private String selectedPriority = "Medium";
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_job);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        etJobTitle = findViewById(R.id.etJobTitle);
        etJobDesc = findViewById(R.id.etJobDesc);
        etInstructions = findViewById(R.id.etInstructions);
        etDeadline = findViewById(R.id.etDeadline);
        etLocation = findViewById(R.id.etLocation);
        actvCategory = findViewById(R.id.actvCategory);
        toggleGroupPriority = findViewById(R.id.toggleGroupPriority);
        btnSubmitJob = findViewById(R.id.btnSubmitJob);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        ivJobPreview = findViewById(R.id.ivJobPreview);
        progressBar = findViewById(R.id.progressBar);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Setup Category Dropdown
        String[] categories = {"Development", "Design", "Marketing", "Maintenance", "Support", "Field Work", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        actvCategory.setAdapter(adapter);

        // Date Picker for Deadline
        etDeadline.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String date = year + "-" + (month + 1) + "-" + day;
                etDeadline.setText(date);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Image Selection
        btnSelectImage.setOnClickListener(v -> openFileChooser());

        // Priority Selection
        toggleGroupPriority.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnPriorityHigh) selectedPriority = "High";
                else if (checkedId == R.id.btnPriorityMedium) selectedPriority = "Medium";
                else if (checkedId == R.id.btnPriorityLow) selectedPriority = "Low";
            }
        });

        btnSubmitJob.setOnClickListener(v -> validateAndUpload());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivJobPreview.setImageURI(imageUri);
            ivJobPreview.setVisibility(View.VISIBLE);
        }
    }

    private void validateAndUpload() {
        String title = etJobTitle.getText().toString().trim();
        String desc = etJobDesc.getText().toString().trim();
        String instructions = etInstructions != null ? etInstructions.getText().toString().trim() : "";
        String deadline = etDeadline.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || deadline.isEmpty() || category.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitJob.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            uploadImage(title, desc, instructions, deadline, category, location);
        } else {
            saveJobToFirestore(title, desc, instructions, deadline, category, location, null);
        }
    }

    private void uploadImage(String title, String desc, String instructions, String deadline, String category, String location) {
        StorageReference fileRef = storage.getReference("job_images/" + UUID.randomUUID().toString());
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveJobToFirestore(title, desc, instructions, deadline, category, location, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmitJob.setEnabled(true);
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveJobToFirestore(String title, String desc, String instructions, String deadline, String category, String location, @Nullable String imageUrl) {
        String id = UUID.randomUUID().toString();
        // New jobs are Pending and Not Assigned
        Job job = new Job(id, title, desc, instructions, "Pending", "Not Assigned", deadline, imageUrl, category, selectedPriority, location);

        db.collection("jobs").document(id).set(job)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Job Created Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitJob.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
