package com.example.pfkworkspace.common.aws.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;

    @Value("${pfk.aws.s3.bucket}")
    private String bucket;

    @Value("${pfk.aws.s3.quarantine-bucket}")
    private String quarantineBucket;

    public String generateUploadUrl(String key, String contentType, Duration expiry) {
        PutObjectRequest objectRequest =
                PutObjectRequest.builder().bucket(quarantineBucket).key(key).contentType(contentType).build();
        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(expiry)
                        .putObjectRequest(objectRequest)
                        .build();

        return s3Presigner.presignPutObject(presignRequest).url().toExternalForm();
    }

    public String generateDownloadUrl(String key, Duration expiry) {
        GetObjectRequest objectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(expiry)
                        .getObjectRequest(objectRequest)
                        .build();

        return s3Presigner.presignGetObject(presignRequest).url().toExternalForm();
    }
}
