<div align="center">
  <img width="50%" src="https://github.com/user-attachments/assets/86472360-033a-4ee0-a537-a0ab500249a3" alt="프로젝트 로고">
</div>

# DigLog

> **계층형 폴더 구조**와 **태그 시스템**을 활용한 블로그 사이트

---

## 📖 목차

- [DigLog](#diglog)
  - [📖 목차](#-목차)
  - [🔗 배포 및 레포지토리 주소](#-배포-및-레포지토리-주소)
    - [배포](#배포)
    - [Repository 주소](#repository-주소)
  - [👨‍💻 팀 소개](#-팀-소개)
    - [🤝 팀원 소개](#-팀원-소개)
    - [⏳ 개발 기간](#-개발-기간)
  - [🚀 주요 기능](#-주요-기능)
  - [☁️ 인프라 구조 및 CI-CD](#️-인프라-구조-및-ci-cd)
    - [Infra](#infra)
    - [BackEnd CI-CD](#backend-ci-cd)
  - [🛠 기술 스택](#-기술-스택)
  - [📊 ERD](#-erd)
  - [🔑 JWT 인증](#-jwt-인증)
  - [📂 디렉토리 구조](#-디렉토리-구조)
  - [📄 문서 및 참고 자료](#-문서-및-참고-자료)

---

## 🔗 배포 및 레포지토리 주소

### 배포
- [배포 URL](배포_주소)
### Repository 주소
- [FrontEnd Repo](https://github.com/diglog-project/DigLog-React)
- [BackEnd Repo](https://github.com/diglog-project/DigLog-Spring)

---

## 👨‍💻 팀 소개

### 🤝 팀원 소개

| 이름  | 역할                 | GitHub                                   |
|-----|--------------------|------------------------------------------|
| 김태환 | Frontend / Backend | [GitHub](https://github.com/typhoon0678) |
| 이석원 | Backend            | [GitHub](https://github.com/frod90)      |

### ⏳ 개발 기간

- **2025.02 ~**

---

## 🚀 주요 기능

- ✅ 계층형 폴더 시스템 구현
  - 최대 3단계 깊이의 계층형 구조의 폴더 구현
  - 사용자가 한번에 폴더를 CRUD할 수 있도록 구현
  - 폴더 CRUD 시 폴더 구조의 유효성 검증 로직 구현
  - 폴더-게시글 1:N 구현


- ✅ 게시글 관리 시스템
  - AWS S3를 활용한 이미지 업로드 및 관리
  - 게시글-태그 N:N 구현
  - 댓글 CRUD 기능과 계층형 댓글 구조 구현
  - 제목, 태그, 작성자 기준 검색 기능 구현


- ✅ 개인 블로그 및 관리 페이지
  - 폴더/태그 별 게시글 필터링
  - 프로필 이미지, username 수정 페이지 제공
  - 폴더, 게시글 수정 페이지 제공


- ✅ JWT, OAuth2 로그인 구현
  - Spring Security, OAuth, JWT를 통한 이메일, 카카오 로그인 구현
  - accessToken을 검증하는 JWTFilter 구현
  - refreshToken을 통한 토큰 재발급 및 토큰 화이트리스트 구현


---

## ☁️ 인프라 구조 및 CI-CD

### Infra
![Infra](https://github.com/user-attachments/assets/d4d69dde-09a7-437b-b3b4-db594593b368)

### BackEnd CI-CD
![BackEnd CI-CD](https://github.com/user-attachments/assets/4e89fd15-a3e6-4f7d-9e2d-392232943140)

---

## 🛠 기술 스택

<div>
  <img src="https://img.shields.io/badge/react-61DAFB?style=for-the-badge&logo=react&logoColor=white" alt="react">
  <img src="https://img.shields.io/badge/typescript-3178C6?style=for-the-badge&logo=typescript&logoColor=black" alt="typescript"> 
  <img src="https://img.shields.io/badge/axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white" alt="axios">
  <img src="https://img.shields.io/badge/redux-764ABC?style=for-the-badge&logo=redux&logoColor=white" alt="redux">
  <img src="https://img.shields.io/badge/React_Router-CA4245?style=for-the-badge&logo=react-router&logoColor=white" alt="react router">
  <img src="https://img.shields.io/badge/tailwindcss-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=black" alt="tailwindcss"> 
  <br>

  <img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" alt="java"> 
  <img src="https://img.shields.io/badge/spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="spring"> 
  <img src="https://img.shields.io/badge/spring_boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="spring boot"> 
  <img src="https://img.shields.io/badge/spring_security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="spring security"> 
  <img src="https://img.shields.io/badge/jwt-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="jwt"> 
  <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="mysql">
  <br>

  <img src="https://img.shields.io/badge/aws-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white" alt="aws"> 
  <img src="https://img.shields.io/badge/ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=black" alt="ubuntu"> 
  <img src="https://img.shields.io/badge/nginx-009639?style=for-the-badge&logo=nginx&logoColor=white" alt="nginx"> 
  <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="docker"> 
  <br>

  <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white" alt="git">
  <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white" alt="github">
  <img src="https://img.shields.io/badge/github_actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="github actions">
  <br>
</div>

---

## 📊 ERD

![ERD](https://github.com/user-attachments/assets/295b8277-45cc-46f7-80b4-a5789ecd4713)

---

## 🔑 JWT 인증

![JWT 인증 방식](https://github.com/user-attachments/assets/b2bb9b30-0b74-4662-885d-58e525d66a18)

---

## 📂 디렉토리 구조

```
 📦 backend
┣ 📂 src/main/java/api/store/diglog
┃ ┣ 📂 common  
┃   ┣ 📂 auth
┃   ┣ 📂 config
┃   ┣ 📂 exception
┃   ┣ 📂 util
┃ ┣ 📂 model
┃   ┣ 📂 constant
┃   ┣ 📂 dto
┃   ┣ 📂 entity
┃   ┣ 📂 vo
┃ ┣ 📂 repository
┃ ┣ 📂 service
┃ ┣ 📂 controller
┃ ┗ 📜 Application.java
```

---

## 📄 문서 및 참고 자료

- 📌 [노션](https://www.notion.so/184a3f4cb4108063805ff446cee6b6b4?source=copy_link)
- 📌 [Swagger](Swagger_주소)
- 📌 [트러블 슈팅](트러블슈팅_주소)

