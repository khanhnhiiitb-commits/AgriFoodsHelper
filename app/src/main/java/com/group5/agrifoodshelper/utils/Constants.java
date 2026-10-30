package com.group5.agrifoodshelper.utils;

public class Constants {
    // Roles
    public static final String ROLE_FARMER = "FARMER";
    public static final String ROLE_SUPPORTER = "SUPPORTER";
    public static final String ROLE_DRIVER = "DRIVER";
    public static final String ROLE_ADMIN = "ADMIN";

    // Firestore Collections
    public static final String COLL_USERS = "users";
    public static final String COLL_CAMPAIGNS = "Campaigns";
    public static final String COLL_COMMITMENTS = "Commitments";
    public static final String COLL_MOBILIZATIONS = "Mobilizations";

    // Campaign Status
    public static final String STATUS_PENDING = "PENDING";     // Chờ duyệt
    public static final String STATUS_ACTIVE = "ACTIVE";       // Đang giải cứu
    public static final String STATUS_COMPLETED = "COMPLETED"; // Đã hoàn thành
}
