package com.dreamypatisiel.devdevdev.aws.s3.properties;

import com.dreamypatisiel.devdevdev.global.utils.FileUtils;

public record S3(String bucket, String root, String pickpickpickPath) {

    public String createPickPickPickDirectory() {
        return root + FileUtils.SLASH + pickpickpickPath;
    }
}
