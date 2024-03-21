package com.dreamypatisiel.devdevdev.aws.s3;

import static org.junit.jupiter.api.Assertions.*;

import com.dreamypatisiel.devdevdev.aws.s3.properties.AwsS3Properties;
import com.dreamypatisiel.devdevdev.aws.s3.properties.S3;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class AwsS3UploaderTest {

    @Autowired
    AwsS3Uploader awsS3Uploader;
    @Autowired
    AwsS3Properties awsS3Properties;

    @Test
    @DisplayName("AWS S3에 이미지를 업로드 한다.")
    void upload() {
        // given
        MockMultipartFile testImage = createMockMultipartFile("testImage", "testImage.png");

        // when
        S3 s3 = awsS3Properties.getS3();
        S3ImageObject s3ImageObject = awsS3Uploader.upload(testImage, s3.bucket(), s3.pickpickpickPath());

        // then
        assertAll(
                () -> assertThat(s3ImageObject.getImageUrl()).isNotEmpty(),
                () -> assertThat(s3ImageObject.getKey()).isNotEmpty()
        );

        // 이미지 삭제
        awsS3Uploader.deletePickOptionImage(s3.bucket(), s3ImageObject.getKey());
    }

    private static MockMultipartFile createMockMultipartFile(String name, String originalFilename) {
        return new MockMultipartFile(
                name,
                originalFilename,
                MediaType.IMAGE_PNG_VALUE,
                name.getBytes()
        );
    }
}