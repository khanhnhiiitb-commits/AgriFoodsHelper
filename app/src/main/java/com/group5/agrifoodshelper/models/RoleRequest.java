package com.group5.agrifoodshelper.models;

import com.google.firebase.firestore.DocumentId;
import java.util.Map;

public class RoleRequest {
    @DocumentId
    private String requestId;
    private String userId;
    private String requestedRole;
    private String status;
    private Map<String, String> info;

    public RoleRequest() {}

    public String getRequestId() { return requestId; }
    public String getUserId() { return userId; }
    public String getRequestedRole() { return requestedRole; }
    public String getStatus() { return status; }
    public Map<String, String> getInfo() { return info; }
}