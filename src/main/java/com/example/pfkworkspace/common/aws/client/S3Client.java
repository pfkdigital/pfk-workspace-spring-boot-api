package com.example.pfkworkspace.common.aws.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3Client {

  @Value("${pfk.aws.region}")
  private String region;

  @Value("${pfk.aws.s3.bucket}")
  private String bucket;

  private AmazonS3 client;

  @PostConstruct
  private void init() {
    this.client = AmazonS3ClientBuilder.standard().withRegion(region).build();
  }

  public AmazonS3 getClient() {
    return client;
  }

  public String getBucket() {
    return bucket;
  }
}
