package com.dreamypatisiel.devdevdev.exception;

import org.springframework.web.multipart.MultipartException;

public class ImageFileException extends MultipartException {
    public ImageFileException(String msg) {
        super(msg);
    }
}
