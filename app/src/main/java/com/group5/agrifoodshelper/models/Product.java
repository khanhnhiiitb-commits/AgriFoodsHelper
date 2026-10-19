package com.group5.agrifoodshelper.models;

import java.io.Serializable;

public class Product implements Serializable {
    private String productId;
    private String productName;
    private String category;
    private double unitPrice;
    private String unit;              // kg, tấn, thùng
    private double totalQuantity;     // Tổng sản lượng cần giải cứu
    private double remainingQuantity; // Sản lượng còn lại
    private String imageUrl;
    private String origin;

    // Constructor rỗng bắt buộc cho Firebase
    public Product() {}

    public Product(String productId, String productName, String category, double unitPrice,
                   String unit, double totalQuantity, double remainingQuantity,
                   String imageUrl, String origin) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = remainingQuantity;
        this.imageUrl = imageUrl;
        this.origin = origin;
    }

    // Phương thức nghiệp vụ tính số lượng đã giải cứu được
    public double getSoldQuantity() {
        return Math.max(0, totalQuantity - remainingQuantity);
    }

    // --- Getters và Setters ---
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(double totalQuantity) { this.totalQuantity = totalQuantity; }

    public double getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(double remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
}