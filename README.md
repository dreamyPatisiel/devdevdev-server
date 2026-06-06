# DEVDEVDEV(서비스 종료)

[![devdevdev](https://github.com/user-attachments/assets/138bff7e-92b3-4c0a-8900-99ba96a797e0)](https://www.devdevdev.co.kr)

> ### 힘들고 막힐 때는 댑댑댑!
> 꿈빛 파티시엘은 **“힘들고 막힐 때는 댑댑댑”** 이라는 슬로건을 기반으로 **개발자들의 고민을 공유**하고 **빅테크 기업들의 다양한 문제 해결 사례**를 통해 **빠른 시간내에 해결**할 수 있도록 도와주는 것을 목표로 삼고 있어요.

🔗 https://www.devdevdev.co.kr

<br />

<div align="center">

  [![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2FdreamyPatisiel%2Fdevdevdev-server&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)

</div>

### 목차
- [개발기간 및 개발인원](#개발기간-및-개발인원)
- [서버 개발환경](#%EF%B8%8F-서버-개발환경)
- [기술스택](#-기술스택)
- [주요 기능](#주요-기능)
- [아키텍처](#아키텍처)
- [DB ERD](#db-erd)
- [디렉토리 구조](#디렉토리-구조)


<br />

## 🕰 개발기간 및 개발인원
- 개발기간: 2023.12 ~ 2024.07(1차배포)
- 개발인원: FE(2), BE(2), Design(1)


<div align="center">


<img src="https://avatars.githubusercontent.com/u/84004367?v=4" width="150" height="150"/>|<img src="https://avatars.githubusercontent.com/u/83548784?v=4" width="150" height="150"/>|<img src="https://avatars.githubusercontent.com/u/42672362?v=4" width="150" height="150"/>|<img src="https://avatars.githubusercontent.com/u/117627859?v=4" width="150" height="150"/>|<img src="https://github.com/user-attachments/assets/b5e96d9e-0fb4-4fc5-bad8-13e483d12933" width="150" height="150"/>|
|:-:|:-:|:-:|:-:|:-:|
|[@minyoung22222](https://github.com/minyoung22222)|[@mandelina](https://github.com/mandelina)|[@ssosee](https://github.com/ssosee)|[@yu-so-young2](https://github.com/yu-so-young2)|뭐임마|
| FE, Entertainer | FE, FootBall Manager | BE, 사장, 앙대요 | BE, CTO | DESIGN, 총괄, 운영, 마케팅 |

</div>

<br />

## 🖥️ 서버 개발환경
- **Common**
  - Spring(6.1.2), SpringBoot(3.2.1), Java(JDK 21)
  - 
- **DEV** 
  - AWS EC2: t2.micro(vCPU 1 Core, Memory 1GB, free tier)
  - AWS RDS: db.t4g.micro(vCPU 2 Core, Memory 1GB, free tier)
  - Amazon OpenSearch Service: t3.small.search(vCPU 2 Core, Memory 2GB, free tier)
- **PROD**
  - AWS EC2: t2.micro(vCPU 1 Core, Memory 1GB, free tier)
  - AWS RDS: db.t4g.micro(vCPU 2 Core, Memory 1GB, free tier)
  - Amazon OpenSearch Service: t3.small.search(vCPU 2 Core, Memory 2GB, free tier)
  - AWS ElasticCache: cache.t2.micro(vCPU 1 Core, Memory 0.5GB)

<div align="right">
  
[목차로](#목차)

</div>

<br/>

## 🛠 기술스택

- **Environment**
    - IntelliJ, GitHub
- **Development**
    - Java, JUnit, Spring Boot, Spring Data JPA, Querydsl, Spring Data ElasticSearch, Spring Security, OAuth2.0
    - Swagger, Spring REST Docs
    - Open AI embeddings, Selenium
- **DB**
    - MariaDB(AWS RDS), Elasticsearch, S3, Redis, H2Database, EmbeddedRedis
- **Test**
    - 약 450개의 테스트 코드를 작성함.
- **Deploy**
    - GitHub Actions, S3, CodeDeploy, Docker
- **Communication**
    - Slack, Jira, Gather

<div align="right">
  
[목차로](#목차)

</div>

<br/>

## 📎 주요 기능 [Docs](https://api.dev.devdevdev.co.kr/docs/index.html)

- 인증, 인가
    - OAuth2.0 로그인 - KAKAO
    - JWT 발급, 인증, 리프레시
- 픽픽픽
    - 조회
    - 작성, 수정, 삭제
    - 이미지 업로드, 삭제
    - 연관 게시글
- 기술블로그
    - 조회
    - 검색
    - 검색어 자동완성
    - 북마크
- 회원
    - 작성한 픽픽픽 조회
    - 북마크한 기술블로그 조회
    - 회원탈퇴 설문조사
    - 회원탈퇴

<div align="right">
  
[목차로](#목차)

</div>

<br/>

## 🧩 아키텍처
![devdevdev-architecture](https://github.com/user-attachments/assets/60718c31-94c0-4edf-9bb6-b2f90a8f2bb8)

<div align="right">
  
[목차로](#목차)

</div>

<br/>

## 🏷 DB ERD

![무제 001 1](https://github.com/user-attachments/assets/39cee5c8-5734-4e11-b494-0723db6e8e42)

<div align="right">
  
[목차로](#목차)

</div>

<br/>

## 📂 디렉토리 구조

```
java.com.dreamypatisiel.devdevdev
  ├── aws
  │   └── s3
  │       ├── config
  │       └── properties
  ├── domain
  │   ├── entity
  │   │   ├── embedded
  │   │   └── enums
  │   ├── exception
  │   ├── policy
  │   ├── repository
  │   │   ├── member
  │   │   │   └── memberNicknameDictionary
  │   │   │       └── custom
  │   │   ├── pick
  │   │   │   └── custom
  │   │   ├── survey
  │   │   │   └── custom
  │   │   └── techArticle
  │   │       └── custom
  │   └── service
  │       ├── member
  │       ├── pick
  │       │   └── dto
  │       ├── response
  │       │   └── util
  │       └── techArticle
  ├── elastic
  │   ├── config
  │   ├── constant
  │   ├── data
  │   │   └── domain
  │   └── domain
  │       ├── document
  │       ├── repository
  │       └── service
  ├── exception
  ├── global
  │   ├── common
  │   ├── config
  │   ├── constant
  │   ├── properties
  │   ├── security
  │   │   ├── config
  │   │   ├── filter
  │   │   ├── jwt
  │   │   │   ├── filter
  │   │   │   ├── handler
  │   │   │   ├── model
  │   │   │   └── service
  │   │   └── oauth2
  │   │       ├── handler
  │   │       ├── model
  │   │       └── service
  │   ├── utils
  │   └── validator
  ├── limiter
  │   ├── config
  │   ├── exception
  │   └── filter
  ├── openai
  │   ├── constant
  │   ├── embeddings
  │   ├── request
  │   └── response
  └── web
      ├── controller
      │   ├── exception
      │   └── request
      └── response
```

<div align="right">
  
[목차로](#목차)

</div>

