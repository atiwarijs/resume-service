package com.cvservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Getter
@Setter
public class S3Config {
    private String region;
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private String endpoint;
}
