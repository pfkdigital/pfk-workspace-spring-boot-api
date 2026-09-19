package com.example.pfkworkspace.common.aws.service;

import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
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

    /**
     * Presigns a PUT to the quarantine bucket. Content-Type, Content-Length and the SHA-256
     * checksum are all part of the signature, so S3 rejects an upload whose type, size or bytes
     * differ from what was declared (and validated) when the URL was issued. The client must send
     * the same values as headers: {@code Content-Type}, {@code Content-Length} and
     * {@code x-amz-checksum-sha256} (base64 of the raw digest).
     *
     * @param sha256Hex hex-encoded SHA-256 digest of the file contents
     */
    public String generateUploadUrl(
            String key, String contentType, long contentLength, String sha256Hex, Duration expiry) {
        PutObjectRequest objectRequest =
                PutObjectRequest.builder()
                        .bucket(quarantineBucket)
                        .key(key)
                        .contentType(contentType)
                        .contentLength(contentLength)
                        .checksumSHA256(hexToBase64(sha256Hex))
                        .build();
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

    private static String hexToBase64(String hex) {
        return Base64.getEncoder().encodeToString(HexFormat.of().parseHex(hex));
    }
}
