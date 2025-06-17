package com.example.qrcodescanner;

public class ScanModel {
    private String content;
    private long timestamp;

    public ScanModel() {
        // Required for Firebase
    }

    public ScanModel(String content, long timestamp) {
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
