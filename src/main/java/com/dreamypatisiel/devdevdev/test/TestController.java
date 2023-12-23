package com.dreamypatisiel.devdevdev.test;

import java.util.List;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/devdevdev/api/v1")
public class TestController {

    public static final String OK = "ok";

    @GetMapping("/members")
    public BasicResponse<Member> getMembers() {
        Member imHa = new Member("이임하", "디자이너");
        Member minYoung = new Member("김민영", "프론트 개발");
        Member minJu = new Member("문민주", "프론트 개발");
        Member soYoung = new Member("유소영", "백엔드 개발");
        Member seaung = new Member("장세웅", "백엔드 개발");

        return new BasicResponse<>("SUCCESS", List.of(imHa, minJu, minYoung, soYoung, seaung));
    }

    @Data
    static class Member {
        private String name;
        private String role;

        public Member(String name, String role) {
            this.name = name;
            this.role = role;
        }
    }

    @Data
    static class BasicResponse<T> {
        private String resultType;
        private List<T> data;

        public BasicResponse(String resultType, List<T> data) {
            this.resultType = resultType;
            this.data = data;
        }
    }
}
