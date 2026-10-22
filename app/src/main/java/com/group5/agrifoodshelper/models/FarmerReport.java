package com.group5.agrifoodshelper.models;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "farmer_reports")
public class FarmerReport {

    @PrimaryKey
    @NonNull
    private String reportId;

    @NonNull
    private String title;

    @NonNull
    private String productName;

    private double estimatedQuantity;

    @NonNull
    private String locationData;

    private boolean isSynced;

    private long createdAt;
    @ColumnInfo(name = "local_image_uri")
    private String localImageUri;

    public FarmerReport() {
        this.reportId = "";
        this.title = "";
        this.productName = "";
        this.estimatedQuantity = 0;
        this.locationData = "{}";
        this.isSynced = false;
        this.createdAt = System.currentTimeMillis();
    }

    public FarmerReport(String reportId, String title,
                        String productName, double estimatedQuantity,
                        String locationData, boolean isSynced,
                        long createdAt,String localImageUri) {
        this.reportId = reportId;
        this.title = title;
        this.productName = productName;
        this.estimatedQuantity = estimatedQuantity;
        this.locationData = locationData;
        this.isSynced = isSynced;
        this.createdAt = createdAt;
        this.localImageUri = localImageUri;
    }

    @NonNull
    public String getReportId() {
        return reportId;
    }

    public void setReportId(@NonNull String reportId) {
        this.reportId = reportId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getProductName() {
        return productName;
    }

    public void setProductName(@NonNull String productName) {
        this.productName = productName;
    }

    public double getEstimatedQuantity() {
        return estimatedQuantity;
    }

    public void setEstimatedQuantity(double estimatedQuantity) {
        this.estimatedQuantity = estimatedQuantity;
    }

    @NonNull
    public String getLocationData() {
        return locationData;
    }

    public void setLocationData(@NonNull String locationData) {
        this.locationData = locationData;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getLocalImageUri() {
        return localImageUri;
    }

    public void setLocalImageUri(String localImageUri) {
        this.localImageUri = localImageUri;
    }
}

