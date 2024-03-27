package com.dreamypatisiel.devdevdev.aws.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.dreamypatisiel.devdevdev.global.utils.FileUtils;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3Uploader {

    public static final String[] ALLOW_IMAGE_MEDIA_TYPES = new String[] {
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    };
    public static final String INVALID_CAN_NOT_DELETE_IMAGE_MESSAGE = "일시적인 오류로 이미지 삭제를 실패 했습니다.";

    private final AmazonS3 amazonS3Client;

    public List<S3ImageObject> uploadMultipleImage(List<MultipartFile> multipartFiles, String bucket, String dirName) {
        return multipartFiles.stream()
                .map(multipartFile -> uploadImage(multipartFile, bucket, dirName))
                .toList();
    }

    public S3ImageObject uploadSingleImage(MultipartFile multipartFile, String bucket, String path) {
        return uploadImage(multipartFile, bucket, path);
    }

    private S3ImageObject uploadImage(MultipartFile multipartFile, String bucket, String path) {
        FileUtils.validateMediaType(multipartFile, ALLOW_IMAGE_MEDIA_TYPES);
        return uploadAnyFile(multipartFile, bucket, path);
    }

    public S3ImageObject uploadAnyFile(MultipartFile multipartFile, String bucket, String path) {
        try {
            // S3 업로드에 필요한 objectMetadata, key, putObjectRequest 생성
            String key = createKey(multipartFile, path);
            ObjectMetadata objectMetadata = createObjectMetadataByMultipartFile(multipartFile);
            PutObjectRequest putObjectRequest = createPutObjectRequest(bucket, key, multipartFile, objectMetadata);

            // 파일 업로드
            amazonS3Client.putObject(putObjectRequest);
            String imageUrl = amazonS3Client.getUrl(bucket, key).toString();

            return S3ImageObject.of(imageUrl, key);
        } catch (IOException e) {
            log.error("uploadAnyFile={}", e.getMessage(), e);
            return S3ImageObject.fail();
        }
    }

    public void deleteMultipleImage(String bucket, List<String> keys) {
        keys.forEach(key -> deleteAnyFile(bucket, key));
    }

    public void deleteSingleImage(String bucket, String key) {
        deleteAnyFile(bucket, key);
    }

    private void deleteAnyFile(String bucket, String key) {
        try {
            amazonS3Client.deleteObject(bucket, key);
        } catch (SdkClientException e) {
            log.error("deleteAnyFile={}", e.getMessage(), e);
            throw new SdkClientException(INVALID_CAN_NOT_DELETE_IMAGE_MESSAGE);
        }
    }

    private PutObjectRequest createPutObjectRequest(String bucket, String key, MultipartFile multipartFile,
                                                    ObjectMetadata objectMetadata) throws IOException {

        return new PutObjectRequest(bucket, key, multipartFile.getInputStream(), objectMetadata);
    }

    private ObjectMetadata createObjectMetadataByMultipartFile(MultipartFile multipartFile) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        return objectMetadata;
    }

    private String createKey(MultipartFile multipartFile, String dirName) {
        String fileName = FileUtils.createRandomFileNameBy(multipartFile.getOriginalFilename());
        return dirName + FileUtils.SLASH + fileName;
    }
}
