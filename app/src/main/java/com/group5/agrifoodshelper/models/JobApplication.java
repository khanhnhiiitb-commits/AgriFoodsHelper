package com.group5.agrifoodshelper.models;

public class JobApplication {
    private String applicationId;
    private String jobId;
    private String userId;
    private String role;
    private String status;

    // 3 trường dữ liệu bổ sung bị thiếu gây ra lỗi
    private String phoneNumber;
    private String vehicleInfo;
    private String eta;

    // Constructor rỗng bắt buộc cho Firestore
    public JobApplication() {}

    public JobApplication(String applicationId, String jobId, String userId, String role, String status, String phoneNumber, String vehicleInfo, String eta) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.userId = userId;
        this.role = role;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.vehicleInfo = vehicleInfo;
        this.eta = eta;
    }

    // --- GETTERS & SETTERS ---
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getVehicleInfo() { return vehicleInfo; }
    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }
}