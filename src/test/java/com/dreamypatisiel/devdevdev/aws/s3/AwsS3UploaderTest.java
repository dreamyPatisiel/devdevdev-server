package com.dreamypatisiel.devdevdev.aws.s3;

import static com.dreamypatisiel.devdevdev.aws.s3.S3ImageObject.UPLOAD_FAIL;
import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.dreamypatisiel.devdevdev.aws.s3.properties.AwsS3Properties;
import com.dreamypatisiel.devdevdev.aws.s3.properties.S3;
import com.dreamypatisiel.devdevdev.exception.ImageFileException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class AwsS3UploaderTest {

    @Autowired
    AwsS3Uploader awsS3Uploader;
    @Autowired
    AwsS3Properties awsS3Properties;

    @Test
    @DisplayName("AWS S3에 이미지를 업로드 한다.")
    void uploadSingleImage() {
        // given
        MockMultipartFile testImage = createMockMultipartFile("testImage", "testImage.png");

        S3 s3 = awsS3Properties.getS3();

        // when
        S3ImageObject s3ImageObject = awsS3Uploader.uploadSingleImage(testImage, s3.bucket(), s3.pickpickpickPath());

        // then
        assertAll(
                () -> assertThat(s3ImageObject.getImageUrl()).isNotEmpty(),
                () -> assertThat(s3ImageObject.getKey()).isNotEmpty()
        );

        // 이미지 삭제
        awsS3Uploader.deleteSingleImage(s3.bucket(), s3ImageObject.getKey());
    }

    @Test
    @DisplayName("AWS S3에 여러개의 이미지를 업로드 한다.")
    void uploadImages() {
        // given
        MultipartFile testImage1 = createMockMultipartFile("testImage1", "testImage1.png");
        MultipartFile testImage2 = createMockMultipartFile("testImage2", "testImage2.png");
        List<MultipartFile> testImages = List.of(testImage1, testImage2);

        S3 s3 = awsS3Properties.getS3();

        // when
        List<S3ImageObject> s3ImageObjects = awsS3Uploader.uploadMultipleImage(testImages, s3.bucket(), s3.pickpickpickPath());

        // then
        assertThat(s3ImageObjects).hasSize(2)
                .extracting("imageUrl", "key")
                .isNotNull();

        // 이미지 여러개 삭제
        List<String> keys = s3ImageObjects.stream()
                .map(S3ImageObject::getKey)
                .toList();
        awsS3Uploader.deleteMultipleImage(s3.bucket(), keys);
    }

    @Test
    @DisplayName("지원하지 않는 이미지 형식을 업로드하면 예외가 발생한다.")
    void uploadSingleImageMediaTypeException() {
        // given
        MockMultipartFile testImage = createMockMultipartFile("testImage", "testImage.gif",
                MediaType.IMAGE_GIF_VALUE);
        S3 s3 = awsS3Properties.getS3();

        // when // then
        assertThatThrownBy(() -> awsS3Uploader.uploadSingleImage(testImage, s3.bucket(), s3.pickpickpickPath()))
                .isInstanceOf(ImageFileException.class);
    }

    @Test
    @DisplayName("이미지를 업로드 할 때 IOException이 발생하면 "+UPLOAD_FAIL+"이 들어있는 S3ImageObject를 반환한다.")
    void uploadSingleImageIOException() throws IOException {
        // given
        MultipartFile mockMultipartFile = mock(MultipartFile.class);

        S3 s3 = awsS3Properties.getS3();

        // when
        when(mockMultipartFile.getName()).thenReturn("testImage");
        when(mockMultipartFile.getOriginalFilename()).thenReturn("testImage.png");
        when(mockMultipartFile.getContentType()).thenReturn(MediaType.IMAGE_PNG_VALUE);
        when(mockMultipartFile.getInputStream()).thenThrow(new IOException("Test IOException"));

        S3ImageObject s3ImageObject = awsS3Uploader.uploadSingleImage(mockMultipartFile, s3.bucket(), s3.pickpickpickPath());

        // then
        assertThat(s3ImageObject).isEqualTo(S3ImageObject.fail());
    }

    @Disabled
    @Test
    @DisplayName("이미지를 삭제할 때 SdkClientException 예외가 발생하면('" +"')메시지와 함께 SdkClientException예외를 발생시킨다.")
    void deleteSingleImageSdkClientException() {
        // given
        AmazonS3Client amazonS3Client = mock(AmazonS3Client.class);
        String bucket = "bucket";
        String key = "key";

        // when
        doThrow(SdkClientException.class).when(amazonS3Client).deleteObject(anyString(), anyString());

        // then
        assertThatThrownBy(() -> awsS3Uploader.deleteSingleImage(bucket, key))
                .isInstanceOf(SdkClientException.class);
    }

    private static MockMultipartFile createMockMultipartFile(String name, String originalFilename) {
        return new MockMultipartFile(
                name,
                originalFilename,
                MediaType.IMAGE_PNG_VALUE,
                name.getBytes()
        );
    }

    private static MockMultipartFile createMockMultipartFile(String name, String originalFilename,
                                                             String mediaTypeValue) {
        return new MockMultipartFile(
                name,
                originalFilename,
                mediaTypeValue,
                name.getBytes()
        );
    }
}