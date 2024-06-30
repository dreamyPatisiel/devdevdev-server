package com.dreamypatisiel.devdevdev.aws.s3.properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

public record Credentials(String accessKey, String secretKey) {
    public AWSCredentials createBasicAWSCredentials() {
        return new BasicAWSCredentials(accessKey, secretKey);
    }
}
