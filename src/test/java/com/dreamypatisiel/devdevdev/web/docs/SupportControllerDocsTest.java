package com.dreamypatisiel.devdevdev.web.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
public class SupportControllerDocsTest {

    @Autowired
    ObjectMapper om;
    @Autowired
    MockMvc mockMvc;
}
