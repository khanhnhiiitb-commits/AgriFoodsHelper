package com.group5.agrifoodshelper.models;

import com.google.firebase.firestore.DocumentId;

public class SupportJob {
    @DocumentId
    private String jobId;
    private String title;
    private String farmName;
    private String address;
    private String date;

    public SupportJob() {} // Bắt buộc phải có constructor rỗng cho Firebase

    public String getJobId() { return jobId; }
    public String getTitle() { return title; }
    public String getFarmName() { return farmName; }
    public String getAddress() { return address; }
    public String getDate() { return date; }
}