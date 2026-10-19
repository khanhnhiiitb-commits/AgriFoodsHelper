
package com.group5.agrifoodshelper.models;

import java.io.Serializable;

public class Notification implements Serializable {

    private String notificationId;
    private String userId;
    private String title;
    private String message;
    private boolean isRead;
    private long timestamp;

    public Notification() {
    }

    public Notification(String notificationId, String userId,
                        String title, String message,
                        boolean isRead, long timestamp) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    // Getters & Setters

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId='" + notificationId + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", isRead=" + isRead +
                ", timestamp=" + timestamp +
                '}';
    }
}
