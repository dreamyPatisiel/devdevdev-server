package com.dreamypatisiel.devdevdev.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.dreamypatisiel.devdevdev.global.utils.FileUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class AwsS3Uploader {

    public static final int FILE_UPLOAD_MAX_TRY_COUNT = 3;
    public static final String DEFAULT_IMAGE_URL = "https://devdevdev-storage.s3.ap-northeast-2.amazonaws.com/devdevdev-default-image.png";
    public static final String EMPTY = "";

    private final AmazonS3 amazonS3Client;

    public List<S3ImageObject> uploads(List<MultipartFile> multipartFiles, String bucket, String dirName)
            throws IOException {
        List<S3ImageObject> s3ImageObjects = new ArrayList<>();

        for(MultipartFile multipartFile : multipartFiles) {
            ObjectMetadata objectMetadata = createObjectMetadataByMultipartFile(multipartFile);
            String key = createKey(dirName, multipartFile);
            PutObjectRequest putObjectRequest = createPutObjectRequest(bucket, key, multipartFile, objectMetadata);

            amazonS3Client.putObject(putObjectRequest);

            String imageUrl = amazonS3Client.getUrl(bucket, key).toString();
            s3ImageObjects.add(S3ImageObject.of(imageUrl, key));
        }

        return s3ImageObjects;
    }

    public S3ImageObject upload(MultipartFile multipartFile, String bucket, String dirName) {
        int fileUploadMaxTryCount = 0;

        while (fileUploadMaxTryCount < FILE_UPLOAD_MAX_TRY_COUNT) {
            try {
                ObjectMetadata objectMetadata = createObjectMetadataByMultipartFile(multipartFile);

                String key = createKey(dirName, multipartFile);
                PutObjectRequest putObjectRequest = createPutObjectRequest(bucket, key, multipartFile, objectMetadata);

                // 파일 업로드
                amazonS3Client.putObject(putObjectRequest);
                String imageUrl = amazonS3Client.getUrl(bucket, key).toString();

                return S3ImageObject.of(imageUrl, key);
            } catch (IOException e) {
                fileUploadMaxTryCount++;
            }
        }

        return S3ImageObject.of(DEFAULT_IMAGE_URL, EMPTY);
    }

    public void deletePickOptionImage(String bucket, String key) {
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
        String filName = FileUtils.createRandomFileNameBy(multipartFile.getOriginalFilename());
        return dirName + FileUtils.SLASH + filName;
    }
}
