package com.dreamypatisiel.devdevdev.aws.s3.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud.aws")
@RequiredArgsConstructor
@Getter @Setter(AccessLevel.PRIVATE)
public class AwsS3Properties {
    private final Credentials credentials;
    private final String region;
    private final S3 s3;
}
