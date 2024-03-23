package com.dreamypatisiel.devdevdev.global.utils;

import com.dreamypatisiel.devdevdev.exception.ImageFileException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public class FileUtils {

    public static final String SLASH = "/";
    public static final String DASH = "-";
    public static final String INVALID_MEDIA_TYPE_MESSAGE = "%s는 지원하지 않은 파일 형식 입니다. 현재 [%s] 파일 형식만 지원합니다.";
    public static final String DELIMITER_COMMA = ", ";

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

    public static void validateMediaType(MultipartFile targetMultipartFile, String[] allowedMediaTypes) {
        String contentType = targetMultipartFile.getContentType();

        boolean isAllowMediaType = Arrays.stream(allowedMediaTypes)
                .anyMatch(mediaType -> mediaType.equals(targetMultipartFile.getContentType()));

        if(!isAllowMediaType) {
            String supportedMediaType = String.join(DELIMITER_COMMA, allowedMediaTypes);
            String errorMessage = String.format(INVALID_MEDIA_TYPE_MESSAGE, contentType, supportedMediaType);

            throw new ImageFileException(errorMessage);
        }
    }
}
