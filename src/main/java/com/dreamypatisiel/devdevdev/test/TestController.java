package com.dreamypatisiel.devdevdev.test;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile(value = {"test", "local", "dev"})
@Slf4j
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
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

    @GetMapping("/authentication")
    public Authentication getAu() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication;
    }

    @GetMapping("/admin")
    public ResponseEntity<String> adminTest() {
        return new ResponseEntity<>("관리자만 들어올 수 있는 페이지", HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<String> userTest() {
        return new ResponseEntity<>("유저만 접근 가능한 페이지", HttpStatus.OK);
    }

    @GetMapping("/public")
    public ResponseEntity<String> publicTest() {
        return new ResponseEntity<>("모두에게 공개된 페이지", HttpStatus.OK);
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
