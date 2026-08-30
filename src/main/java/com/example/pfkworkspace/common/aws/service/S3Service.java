package com.example.pfkworkspace.common.aws.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.example.pfkworkspace.common.aws.client.S3Client;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    public String generateUploadUrl(String key, String contentType, Duration expiry) {
        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(s3Client.getBucket(), key)
                        .withMethod(HttpMethod.PUT)
                        .withContentType(contentType)
                        .withExpiration(toExpirationDate(expiry));

        return s3Client.getClient().generatePresignedUrl(request).toExternalForm();
    }

    public String generateDownloadUrl(String key, Duration expiry) {
        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(s3Client.getBucket(), key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(toExpirationDate(expiry));

        return s3Client.getClient().generatePresignedUrl(request).toExternalForm();
    }

    private Date toExpirationDate(Duration expiry) {
        return Date.from(Instant.now().plus(expiry));
    }
}
