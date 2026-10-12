package com.group5.agrifoodshelper.models;

public class Mobilization {
    private String mobilizationId;
    private String campaignId;
    private String type; // "TRUCK" (Xe tải) hoặc "VOLUNTEER" (Người)
    private int requiredCount; // Trọng tải xe hoặc số lượng người
    private String driverId; // ID của tài xế nhận (mặc định để trống)
    private String status; // "WAITING", "CONFIRMED", "COMPLETED"

    public Mobilization() {}

    public Mobilization(String mobilizationId, String campaignId, String type, int requiredCount, String driverId, String status) {
        this.mobilizationId = mobilizationId;
        this.campaignId = campaignId;
        this.type = type;
        this.requiredCount = requiredCount;
        this.driverId = driverId;
        this.status = status;
    }

    public String getMobilizationId() { return mobilizationId; }
    public void setMobilizationId(String mobilizationId) { this.mobilizationId = mobilizationId; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getRequiredCount() { return requiredCount; }
    public void setRequiredCount(int requiredCount) { this.requiredCount = requiredCount; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}