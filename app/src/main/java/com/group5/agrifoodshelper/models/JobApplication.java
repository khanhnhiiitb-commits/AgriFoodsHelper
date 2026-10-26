package com.group5.agrifoodshelper.models;

import java.io.Serializable;

public class JobApplication implements Serializable {
    private String applicationId;
    private String jobId;
    private String userId;
    private String applicantName;
    private String applicantPhone;
    private String role;
    private String status;
    private long timestamp;

    public JobApplication() {}

    public JobApplication(String applicationId, String jobId, String userId,
                          String applicantName, String applicantPhone,
                          String role, String status, long timestamp) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.userId = userId;
        this.applicantName = applicantName;
        this.applicantPhone = applicantPhone;
        this.role = role;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String applicantPhone) { this.applicantPhone = applicantPhone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
