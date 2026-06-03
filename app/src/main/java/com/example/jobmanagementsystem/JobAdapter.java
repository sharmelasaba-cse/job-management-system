package com.example.jobmanagementsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList;
    private OnJobClickListener clickListener;
    private OnDeleteClickListener deleteListener;
    private OnCompleteClickListener completeListener;
    private OnStartClickListener startListener;
    private OnUpdateProgressListener updateProgressListener;
    private boolean isMonitorMode = false;
    private boolean isEmployeeView = false;

    public interface OnJobClickListener {
        void onJobClick(Job job);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Job job);
    }

    public interface OnCompleteClickListener {
        void onCompleteClick(Job job);
    }

    public interface OnStartClickListener {
        void onStartClick(Job job);
    }

    public interface OnUpdateProgressListener {
        void onUpdateClick(Job job);
    }

    public JobAdapter(List<Job> jobList) {
        this.jobList = jobList;
    }

    public void setOnJobClickListener(OnJobClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public void setOnCompleteClickListener(OnCompleteClickListener listener) {
        this.completeListener = listener;
    }

    public void setOnStartClickListener(OnStartClickListener listener) {
        this.startListener = listener;
    }

    public void setOnUpdateProgressListener(OnUpdateProgressListener listener) {
        this.updateProgressListener = listener;
    }

    public void setMonitorMode(boolean monitorMode) {
        isMonitorMode = monitorMode;
    }

    public void setEmployeeView(boolean employeeView) {
        isEmployeeView = employeeView;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.tvTitle.setText(job.getTitle() != null ? job.getTitle() : "No Title");
        holder.tvDesc.setText(job.getDescription() != null ? job.getDescription() : "");
        holder.tvStatus.setText(job.getStatus() != null ? job.getStatus() : "Pending");
        
        String deadline = job.getDeadline() != null ? job.getDeadline() : "N/A";
        holder.tvDeadline.setText("Deadline: " + deadline);
        
        String workerName = job.getAssignedToName();
        String workerEmail = job.getAssignedTo();
        String displayWorker = (workerName != null && !workerName.isEmpty()) ? workerName : 
                              (workerEmail != null && !workerEmail.equalsIgnoreCase("Not Assigned") ? workerEmail : "Not Assigned");
        
        holder.tvAssignedTo.setText("Worker: " + displayWorker);
        
        if (holder.tvLocation != null) {
            String location = job.getLocation() != null ? job.getLocation() : "N/A";
            holder.tvLocation.setText("Location: " + location);
            holder.tvLocation.setVisibility(View.VISIBLE);
        }

        // Handle Progress
        holder.pbProgress.setProgress(job.getProgress());
        holder.tvProgressText.setText("Progress: " + job.getProgress() + "%");
        
        if (isMonitorMode || isEmployeeView) {
            holder.pbProgress.setVisibility(View.VISIBLE);
            holder.tvProgressText.setVisibility(View.VISIBLE);
        } else {
            holder.pbProgress.setVisibility(View.GONE);
            holder.tvProgressText.setVisibility(View.GONE);
        }

        if (job.getPriority() != null) {
            holder.tvPriority.setText(job.getPriority());
            holder.tvPriority.setVisibility(View.VISIBLE);
            if ("High".equalsIgnoreCase(job.getPriority())) {
                holder.tvPriority.setBackgroundResource(R.drawable.bg_status_pending); 
            } else {
                holder.tvPriority.setBackgroundResource(R.drawable.bg_status_progress);
            }
        } else {
            holder.tvPriority.setVisibility(View.GONE);
        }

        String status = job.getStatus() != null ? job.getStatus() : "Pending";
        if ("Completed".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_progress);
        }

        // Employee Actions
        if (isEmployeeView && holder.llEmployeeActions != null) {
            holder.llEmployeeActions.setVisibility(View.VISIBLE);
            
            if ("Completed".equalsIgnoreCase(status)) {
                holder.btnStartJob.setVisibility(View.GONE);
                holder.btnUpdateProgress.setVisibility(View.GONE);
                holder.btnCompleteJob.setVisibility(View.GONE);
            } else if ("In Progress".equalsIgnoreCase(status)) {
                holder.btnStartJob.setVisibility(View.GONE);
                holder.btnUpdateProgress.setVisibility(View.VISIBLE);
                holder.btnCompleteJob.setVisibility(View.VISIBLE);
            } else {
                // Assigned or Pending
                holder.btnStartJob.setVisibility(View.VISIBLE);
                holder.btnUpdateProgress.setVisibility(View.GONE);
                holder.btnCompleteJob.setVisibility(View.GONE);
            }
        } else if (holder.llEmployeeActions != null) {
            holder.llEmployeeActions.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onJobClick(job);
            }
        });

        if (holder.btnDeleteJob != null) {
            holder.btnDeleteJob.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(job);
                }
            });
            holder.btnDeleteJob.setVisibility(deleteListener != null ? View.VISIBLE : View.GONE);
        }

        holder.btnStartJob.setOnClickListener(v -> {
            if (startListener != null) startListener.onStartClick(job);
        });

        holder.btnUpdateProgress.setOnClickListener(v -> {
            if (updateProgressListener != null) updateProgressListener.onUpdateClick(job);
        });

        holder.btnCompleteJob.setOnClickListener(v -> {
            if (completeListener != null) {
                completeListener.onCompleteClick(job);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvStatus, tvAssignedTo, tvDeadline, tvPriority, tvLocation, tvProgressText;
        ProgressBar pbProgress;
        View llEmployeeActions;
        ImageButton btnDeleteJob;
        MaterialButton btnCompleteJob, btnStartJob, btnUpdateProgress;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvJobTitle);
            tvDesc = itemView.findViewById(R.id.tvJobDesc);
            tvStatus = itemView.findViewById(R.id.tvJobStatus);
            tvAssignedTo = itemView.findViewById(R.id.tvAssignedTo);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            pbProgress = itemView.findViewById(R.id.pbJobProgress);
            llEmployeeActions = itemView.findViewById(R.id.llEmployeeActions);
            btnDeleteJob = itemView.findViewById(R.id.btnDeleteJob);
            btnCompleteJob = itemView.findViewById(R.id.btnCompleteJobItem);
            btnStartJob = itemView.findViewById(R.id.btnStartJobItem);
            btnUpdateProgress = itemView.findViewById(R.id.btnUpdateProgressItem);
        }
    }
}
