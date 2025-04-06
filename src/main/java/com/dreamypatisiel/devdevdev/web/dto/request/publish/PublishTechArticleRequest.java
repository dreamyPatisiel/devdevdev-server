package com.dreamypatisiel.devdevdev.web.dto.request.publish;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PublishTechArticleRequest implements Serializable, RedisPublishRequest {

    @NotNull(message = "회사 아이디는 필수 입니다.")
    private Long companyId;

    @NotEmpty(message = "기술 블로그는 필수 입니다.")
    private List<PublishTechArticle> techArticles;

    public PublishTechArticleRequest(Long companyId, List<PublishTechArticle> techArticles) {
        this.companyId = companyId;
        this.techArticles = techArticles;
    }
}
