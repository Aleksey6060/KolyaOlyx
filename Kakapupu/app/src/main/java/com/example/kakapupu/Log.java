package com.example.kakapupu;

public class Log {
    private String action;
    private long timestamp;
    private String adminId;

    public Log() {}

    public Log(String action, long timestamp, String adminId) {
        this.action = action;
        this.timestamp = timestamp;
        this.adminId = adminId;
    }

    public String getAction() { return action; }
    public long getTimestamp() { return timestamp; }
    public String getAdminId() { return adminId; }
}