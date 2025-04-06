package com.dreamypatisiel.devdevdev.web.dto.request.publish;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PublishTechArticle implements Serializable, RedisPublishRequest {
    @NotNull(message = "기술 블로그 아이디는 필수 입니다.")
    private Long id;

    public PublishTechArticle(Long id) {
        this.id = id;
    }
}
