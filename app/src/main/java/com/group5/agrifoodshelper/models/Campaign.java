package com.group5.agrifoodshelper.models;

import androidx.room.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model Campaign đại diện cho chiến dịch kêu gọi giải cứu nông sản
 * Hỗ trợ lưu trữ trên Cloud Firestore và lưu bộ đệm SQLite.
 */
@Entity(tableName = "cached_campaigns")
public class Campaign implements Serializable {
    private String campaignId;
    private String authorId;
    private String authorName;
    private String contactPhone;
    private String title;
    private String story;
    private String address;
    private String priority;
    private String status;
    private boolean pinned;
    private Product product;
    private List<String> imageUrls;
    private int likeCount;
    private int commentCount;
    private long createdAt;
    // Thêm vào danh sách thuộc tính
    private double targetQuantity;
    private double currentQuantity;


    public double getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(double currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public double getTargetQuantity() {
        return targetQuantity;
    }

    public void setTargetQuantity(double targetQuantity) {
        this.targetQuantity = targetQuantity;
    }

    // 1. Constructor rỗng (BẮT BUỘC cho Firebase Firestore deserialization)
    public Campaign() {
        this.imageUrls = new ArrayList<>();
        this.pinned = false;
        this.status = "PENDING";
        this.likeCount = 0;
        this.commentCount = 0;
        this.createdAt = System.currentTimeMillis();
    }

    // 2. Constructor đầy đủ tham số
    public Campaign(String campaignId, String authorId, String authorName, String contactPhone,
                    String title, String story, String address, String priority, String status,
                    boolean pinned, Product product, List<String> imageUrls,
                    int likeCount, int commentCount, long createdAt) {
        this.campaignId = campaignId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.contactPhone = contactPhone;
        this.title = title;
        this.story = story;
        this.address = address;
        this.priority = priority;
        this.status = status;
        this.pinned = pinned;
        this.product = product;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }

    // --- Phương thức nghiệp vụ (Business logic methods) ---

    /**
     * Tính phần trăm tiến độ giải cứu cho thanh ProgressBar (FR36)
     * Trả về số nguyên từ 0 đến 100
     */
    public int getProgressPercentage() {
        if (targetQuantity <= 0) return 0;
        int percent = (int) Math.round((currentQuantity / targetQuantity) * 100);
        return Math.min(100, Math.max(0, percent));
    }

    /**
     * Trả về chuỗi mô tả tiến độ thực tế (ví dụ: "Đã bán: 1.150 kg / 2.000 kg (57.5%)")
     */
    public String getProgressSummary() {
        if (product == null) return "0/0 kg";
        double sold = product.getSoldQuantity();
        double total = product.getTotalQuantity();
        double percent = (total > 0) ? (sold / total) * 100 : 0;
        return String.format("Đã bán: %,.0f %s / %,.0f %s (%.1f%%)",
                sold, product.getUnit(), total, product.getUnit(), percent);
    }

    /**
     * Kiểm tra chiến dịch có thuộc diện giải cứu khẩn cấp hay không
     */
    public boolean isUrgent() {
        return "URGENT_24H".equalsIgnoreCase(priority);
    }

    // --- Getters & Setters tuân thủ đóng gói OOP ---

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}