package com.dreamypatisiel.devdevdev.global.utils;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public class FileUtils {

    public static final String SLASH = "/";
    public static final String DASH = "-";

    public static String createRandomFileNameBy(String originalFileName) {
        return UUID.randomUUID() + DASH + originalFileName;
    }

    public static List<BlobInfo> createBlobInfos(String bucketName, List<MultipartFile> multipartFiles) {
        return multipartFiles.stream()
                .map(image -> Blob.newBuilder(bucketName,
                        FileUtils.createRandomFileNameBy(image.getOriginalFilename()))
                        .setContentType(image.getContentType())
                        .build()
                )
                .toList();
    }
}
