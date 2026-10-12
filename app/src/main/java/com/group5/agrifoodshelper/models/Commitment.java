package com.group5.agrifoodshelper.models;

public class Commitment {
    private String commitmentId;
    private String campaignId;
    private String supporterId;
    private double committedQuantity;
    private long timestamp;

    public Commitment() {}

    public Commitment(String commitmentId, String campaignId, String supporterId, double committedQuantity, long timestamp) {
        this.commitmentId = commitmentId;
        this.campaignId = campaignId;
        this.supporterId = supporterId;
        this.committedQuantity = committedQuantity;
        this.timestamp = timestamp;
    }

    // --- Getters và Setters ---
    public String getCommitmentId() { return commitmentId; }
    public void setCommitmentId(String commitmentId) { this.commitmentId = commitmentId; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getSupporterId() { return supporterId; }
    public void setSupporterId(String supporterId) { this.supporterId = supporterId; }

    public double getCommittedQuantity() { return committedQuantity; }
    public void setCommittedQuantity(double committedQuantity) { this.committedQuantity = committedQuantity; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}