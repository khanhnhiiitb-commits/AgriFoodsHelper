package com.group5.agrifoodshelper.models;

public class Campaign {
    private String campaignId;
    private String title;
    private String detail;
    private double targetQuantity;
    private String address;
    private String imageUrl;
    private long createdAt;
    private String authorId;
    // Getter và Setter cho authorId
    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    private boolean isPinned; // Cờ đánh dấu ghim lên đầu trang
    // 1. Thêm biến này vào danh sách khai báo ở đầu class
    private double currentQuantity = 0;

    public double getCurrentQuantity() {
        return currentQuantity;
    }
    private boolean isActive = true;

    public void setCurrentQuantity(double currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    // Constructor rỗng bắt buộc cho Firebase
    public Campaign() {}

    public Campaign(String campaignId, String title, String detail, double targetQuantity,
                    String address, String imageUrl, long createdAt, boolean isPinned) {
        this.campaignId = campaignId;
        this.title = title;
        this.detail = detail;
        this.targetQuantity = targetQuantity;
        this.address = address;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.isPinned = isPinned;
        this.isActive = true;
    }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

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
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

}