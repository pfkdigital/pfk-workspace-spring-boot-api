package com.example.pfkworkspace.modules.task.infrastructure.messaging;

import com.example.pfkworkspace.modules.task.application.AttachmentService;
import com.example.pfkworkspace.modules.task.application.messaging.ScanResultMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScanResultListener {
    private final AttachmentService attachmentService;

    @SqsListener("${pfk.aws.sqs.scan-results-queue}")
    public void applyScanResult(ScanResultMessage scanResult) {
        log.info("Received scan result: {}", scanResult);
        attachmentService.applyScanResult(scanResult);
    }
}
