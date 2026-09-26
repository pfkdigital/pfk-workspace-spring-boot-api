package com.example.pfkworkspace.common.aws.service;

import com.example.pfkworkspace.common.aws.StorageException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

  private final S3Presigner s3Presigner;
  private final S3Client s3Client;

  @Value("${pfk.aws.s3.bucket}")
  private String bucket;

  @Value("${pfk.aws.s3.quarantine-bucket}")
  private String quarantineBucket;

  private static String hexToBase64(String hex) {
    return Base64.getEncoder().encodeToString(HexFormat.of().parseHex(hex));
  }

  private static String rfc5987Encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  /**
   * Presigns a PUT to the quarantine bucket. Content-Type, Content-Length and the SHA-256 checksum
   * are all part of the signature, so S3 rejects an upload whose type, size or bytes differ from
   * what was declared (and validated) when the URL was issued. The client must send the same values
   * as headers: {@code Content-Type}, {@code Content-Length} and {@code x-amz-checksum-sha256}
   * (base64 of the raw digest).
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

    try {
      return s3Presigner.presignPutObject(presignRequest).url().toExternalForm();
    } catch (SdkException e) {
      log.error("Failed to presign upload for key {} in bucket {}", key, quarantineBucket, e);
      throw new StorageException("Unable to generate upload URL", e);
    }
  }

  public String generateDownloadUrl(
      String key, String contentType, String filename, Duration expiry) {
    GetObjectRequest objectRequest =
        GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .responseContentType(contentType)
            .responseContentDisposition("attachment; filename*=UTF-8''" + rfc5987Encode(filename))
            .build();
    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(expiry)
            .getObjectRequest(objectRequest)
            .build();

    try {
      return s3Presigner.presignGetObject(presignRequest).url().toExternalForm();
    } catch (SdkException e) {
      log.error("Failed to presign download for key {} in bucket {}", key, bucket, e);
      throw new StorageException("Unable to generate download URL", e);
    }
  }

  public DeleteObjectResponse deleteObject(String key) {
    return deleteFromBucket(bucket, key);
  }

  public DeleteObjectResponse deleteQuarantinedObject(String key) {
    return deleteFromBucket(quarantineBucket, key);
  }

  public boolean existsInQuarantine(String key) {
    return objectExists(quarantineBucket, key);
  }

  public boolean existsInBucket(String key) {
    return objectExists(bucket, key);
  }

  private DeleteObjectResponse deleteFromBucket(String targetBucket, String key) {
    DeleteObjectRequest deleteObjectRequest =
        DeleteObjectRequest.builder().bucket(targetBucket).key(key).build();
    try {
      return s3Client.deleteObject(deleteObjectRequest);
    } catch (SdkException e) {
      log.error("Failed to delete key {} from bucket {}", key, targetBucket, e);
      throw new StorageException("Unable to delete stored file", e);
    }
  }

  private Boolean objectExists(String bucket, String key) {
    HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(bucket).key(key).build();
    try {
      s3Client.headObject(headObjectRequest);
      return true;
    }
    catch(NoSuchKeyException e) {
      return false;
    }
    catch (SdkException e) {
      log.error("Failed to check existence of key {} in bucket {}", key, bucket, e);
      throw new StorageException("Unable to check existence of stored file", e);
    }
  }
}
