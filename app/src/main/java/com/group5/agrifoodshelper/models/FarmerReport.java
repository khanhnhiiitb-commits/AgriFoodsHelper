package com.group5.agrifoodshelper.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;


@Entity(tableName = "farmer_reports")

public class FarmerReport {

    @PrimaryKey
    @NonNull
    private String reportId;

    @NonNull
    private String title;

    @ColumnInfo(name = "price")
    private double price;
    @NonNull
    private String productName;

    private double estimatedQuantity;

    @NonNull
    private String locationData;

    private boolean isSynced;

    private long createdAt;

    @ColumnInfo(name = "local_image_uri")
    private String localImageUri;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "detail")
    private String detail;

    @ColumnInfo(name = "rescue_time")
    private String rescueTime;
    @ColumnInfo(name = "user_id")
    private String userId;
    public FarmerReport() {
        this.reportId = "";
        this.title = "";
        this.productName = "";
        this.estimatedQuantity = 0;
        this.locationData = "{}";
        this.isSynced = false;
        this.createdAt = System.currentTimeMillis();
        this.status = "PENDING";
        this.detail = "";
        this.rescueTime = "";
    }

    public FarmerReport(@NonNull String reportId, @NonNull String title,
                        @NonNull String productName, double estimatedQuantity,
                        @NonNull String locationData, boolean isSynced,
                        long createdAt, String localImageUri, String status,
                        String detail, String rescueTime,String userId) {
        this.reportId = reportId;
        this.title = title;
        this.productName = productName;
        this.estimatedQuantity = estimatedQuantity;
        this.price = 0.0;
        this.locationData = locationData;
        this.isSynced = isSynced;
        this.createdAt = createdAt;
        this.localImageUri = localImageUri;
        this.status = status;
        this.detail = detail;
        this.rescueTime = rescueTime;
        this.userId = userId;
    }

    @NonNull public String getReportId() { return reportId; }
    public void setReportId(@NonNull String reportId) { this.reportId = reportId; }

    @NonNull public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }

    @NonNull public String getProductName() { return productName; }
    public void setProductName(@NonNull String productName) { this.productName = productName; }

    public double getEstimatedQuantity() { return estimatedQuantity; }
    public void setEstimatedQuantity(double estimatedQuantity) { this.estimatedQuantity = estimatedQuantity; }

    @NonNull public String getLocationData() { return locationData; }
    public void setLocationData(@NonNull String locationData) { this.locationData = locationData; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { this.isSynced = synced; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getLocalImageUri() { return localImageUri; }
    public void setLocalImageUri(String localImageUri) { this.localImageUri = localImageUri; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    public String getRescueTime() { return rescueTime; }
    public void setRescueTime(String rescueTime) { this.rescueTime = rescueTime; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}