package com.example.pfkworkspace.modules.task.application.messaging;

public record ScanResultMessage(String storageKey, String bucket, ScanVerdict verdict, String reason) {}
