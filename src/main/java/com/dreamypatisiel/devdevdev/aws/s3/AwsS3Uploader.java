package com.dreamypatisiel.devdevdev.aws.s3;

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

    private final AmazonS3 amazonS3Client;

    public List<S3ImageObject> uploadMultipleImage(List<MultipartFile> multipartFiles, String bucket, String dirName) {
        return multipartFiles.stream()
                .map(multipartFile -> uploadImage(multipartFile, bucket, dirName))
                .toList();
    }

    public S3ImageObject uploadSingleImage(MultipartFile multipartFile, String bucket, String dirName) {
        return uploadImage(multipartFile, bucket, dirName);
    }

    private S3ImageObject uploadImage(MultipartFile multipartFile, String bucket, String dirName) {
        try {
            // 파일 형식 검사
            FileUtils.validateMediaType(multipartFile, ALLOW_IMAGE_MEDIA_TYPES);
            return uploadAnyFile(multipartFile, bucket, dirName);
        } catch (IOException e) {
            // 업로드에 실패하면 UPLOAD_FAIL 객체 반환
            log.error("uploadImage={}", e.getMessage(), e);
            return S3ImageObject.fail();
        }
    }

    public S3ImageObject uploadAnyFile(MultipartFile multipartFile, String bucket, String dirName)
            throws IOException {
        // S3 업로드에 필요한 objectMetadata, key, putObjectRequest 생성
        String key = createKey(dirName, multipartFile);
        ObjectMetadata objectMetadata = createObjectMetadataByMultipartFile(multipartFile);
        PutObjectRequest putObjectRequest = createPutObjectRequest(bucket, key, multipartFile, objectMetadata);

        // 파일 업로드
        amazonS3Client.putObject(putObjectRequest);
        String imageUrl = amazonS3Client.getUrl(bucket, key).toString();

        return S3ImageObject.of(imageUrl, key);
    }

    public void deleteMultipleImage(String bucket, List<S3ImageObject> s3ImageObjects) {
        s3ImageObjects.forEach(s3ImageObject -> deleteAnyFile(bucket, s3ImageObject.getKey()));
    }

    public void deleteSingleImage(String bucket, S3ImageObject s3ImageObjects) {
        deleteAnyFile(bucket, s3ImageObjects.getKey());
    }

    private void deleteAnyFile(String bucket, String key) {
        amazonS3Client.deleteObject(bucket, key);
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

    private String createKey(String dirName, MultipartFile multipartFile) {
        String fileName = FileUtils.createRandomFileNameBy(multipartFile.getOriginalFilename());
        return dirName + FileUtils.SLASH + fileName;
    }
}
