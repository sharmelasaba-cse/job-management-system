package com.example.jobmanagementsystem;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Job implements Serializable {
    private String jobId;
    private String title;
    private String description;
    private String instructions; // Added field
    private String status; // Pending, Assigned, In Progress, Completed
    private String assignedTo; // Employee UID
    private String assignedToName;
    private String deadline;
    private String imageUrl;
    private String proofImageUrl;
    private String category;
    private String priority;
    private String location;
    private int progress;
    private Timestamp createdAt;

    public Job() {} // Required for Firebase

    public Job(String jobId, String title, String description, String instructions, String status, String assignedTo, String deadline, String imageUrl, String category, String priority, String location) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.instructions = instructions;
        this.status = status;
        this.assignedTo = assignedTo;
        this.deadline = deadline;
        this.imageUrl = imageUrl;
        this.category = category;
        this.priority = priority;
        this.location = location;
        this.progress = 0;
        this.createdAt = Timestamp.now();
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getProofImageUrl() { return proofImageUrl; }
    public void setProofImageUrl(String proofImageUrl) { this.proofImageUrl = proofImageUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
