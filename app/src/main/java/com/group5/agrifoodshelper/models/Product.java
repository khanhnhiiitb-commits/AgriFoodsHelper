package com.group5.agrifoodshelper.models;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Product implements Serializable {
    private String productId;
    private String productName;
    private String category;
    private double unitPrice;
    private String unit;
    private double totalQuantity;
    private double remainingQuantity;
    private String imageUrl;
    private String origin;

    public Product() {
    }


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



    /**
     * Định dạng đơn giá sang tiền tệ Việt Nam (Ví dụ: 15,000 VNĐ/kg)
     */
    public String getFormattedPrice() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(unitPrice) + " đ/" + unit;
    }

    /**
     * Tính sản lượng đã giải cứu được
     */
    public double getSoldQuantity() {
        return Math.max(0, totalQuantity - remainingQuantity);
    }

    // --- Getters & Setters tuân thủ đóng gói OOP ---

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(double remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", unitPrice=" + unitPrice +
                ", unit='" + unit + '\'' +
                ", remainingQuantity=" + remainingQuantity +
                '}';
    }
}