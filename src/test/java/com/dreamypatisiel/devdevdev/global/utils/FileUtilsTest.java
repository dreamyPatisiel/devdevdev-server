package com.dreamypatisiel.devdevdev.global.utils;

import static com.dreamypatisiel.devdevdev.global.utils.FileUtils.DELIMITER_COMMA;
import static com.dreamypatisiel.devdevdev.global.utils.FileUtils.INVALID_MEDIA_TYPE_MESSAGE;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dreamypatisiel.devdevdev.exception.ImageFileException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class FileUtilsTest {

    static String fileName = "testFileName";
    static String originalFileName = "testOriginalFileName";

    @ParameterizedTest
    @ValueSource(strings = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    @DisplayName("지원 파일 형식을 검증 합니다.")
    void validateMediaType(String mediaTypeValue) {
        // given
        String[] allowedMediaTypes = new String[]{
                MediaType.IMAGE_PNG_VALUE,
                MediaType.IMAGE_JPEG_VALUE
        };

        MockMultipartFile multipartFile = createMockMultipartFile(fileName, originalFileName, mediaTypeValue);

        // when // then
        assertThatCode(() -> FileUtils.validateMediaType(multipartFile, allowedMediaTypes))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.APPLICATION_CBOR_VALUE})
    @DisplayName("지원 파일 형식을 검증 할 때 허용하지 않은 파일형식이면 예외가 발생한다.")
    void validateMediaTypeException(String invalidMediaType) {
        // given
        String[] allowedMediaTypes = new String[]{
                MediaType.IMAGE_PNG_VALUE,
                MediaType.IMAGE_JPEG_VALUE
        };

        MockMultipartFile multipartFile = createMockMultipartFile(fileName, originalFileName, invalidMediaType);

        String supportedMediaType = String.join(DELIMITER_COMMA, allowedMediaTypes);
        String errorMessage = String.format(INVALID_MEDIA_TYPE_MESSAGE,
                multipartFile.getContentType(), supportedMediaType);

        // when // then
       assertThatThrownBy(() -> FileUtils.validateMediaType(multipartFile, allowedMediaTypes))
               .isInstanceOf(ImageFileException.class)
               .hasMessage(errorMessage);
    }

    private static MockMultipartFile createMockMultipartFile(String name, String originalFilename, String mediaTypeValue) {
        return new MockMultipartFile(
                name,
                originalFilename,
                mediaTypeValue,
                name.getBytes()
        );
    }
}