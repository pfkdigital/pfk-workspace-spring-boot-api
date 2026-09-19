package com.example.pfkworkspace.modules.task.application;

import com.example.pfkworkspace.modules.task.application.messaging.ScanResultMessage;

public interface AttachmentService {
    void applyScanResult(ScanResultMessage scanResult);
}
