package com.group5.agrifoodshelper.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cached_campaigns")
public class Campaign {

    @PrimaryKey
    @NonNull
    private String campaignId = ""; // Khởi tạo chuỗi rỗng để tránh lỗi Null của Room

    private String title;
    private String detail;
    private double targetQuantity;
    private String address;
    private String imageUrl;
    private long createdAt;
    private boolean isPinned;
    private double currentQuantity = 0.0;
    private boolean isActive = true;
    private String status = "OPEN";
    private String ownerId;
    private String createdBy;

    // Firebase và Room đều bắt buộc cần constructor rỗng.
    public Campaign() {
    }

    public Campaign(
            @NonNull String campaignId,
            String title,
            String detail,
            double targetQuantity,
            String address,
            String imageUrl,
            long createdAt,
            boolean isPinned) {

        this.campaignId = campaignId;
        this.title = title;
        this.detail = detail;
        this.targetQuantity = targetQuantity;
        this.address = address;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.isPinned = isPinned;
        this.currentQuantity = 0.0;
        this.isActive = true;
        this.status = "OPEN";
    }

    @NonNull
    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(@NonNull String campaignId) {
        this.campaignId = campaignId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public double getTargetQuantity() {
        return targetQuantity;
    }

    public void setTargetQuantity(double targetQuantity) {
        this.targetQuantity = targetQuantity;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        this.isPinned = pinned;
    }

    public double getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(double currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}