package com.example.jobmanagementsystem;

public class Job {
    private String jobId;
    private String title;
    private String description;
    private String status;
    private String assignedTo;

    public Job() {} // Required for Firebase

    public Job(String jobId, String title, String description, String status, String assignedTo) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.assignedTo = assignedTo;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
}