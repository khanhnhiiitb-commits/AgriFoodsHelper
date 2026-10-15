
package com.group5.agrifoodshelper.models;

import java.io.Serializable;

public class User implements Serializable {

    private String userId;
    private String fullName;
    private String phone;
    private String role;
    private double trustScore;

    public User() {
    }

    public User(String userId, String fullName, String phone,
                String role, double trustScore) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.trustScore = trustScore;
    }

    // Getters & Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(double trustScore) {
        this.trustScore = trustScore;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + role + '\'' +
                ", trustScore=" + trustScore +
                '}';
    }
}
