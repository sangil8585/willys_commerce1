---
name: clean-architecture-conventions
description: 클린 아키텍처 5계층 컨벤션 가이드 - 코드 작성/리뷰 시 아키텍처 규칙 적용
alwaysApply: true
---

# 클린 아키텍처 컨벤션

## 1. 패키지 구조 (5계층)

일반적으로 통용되도록 아래 순서와 이름을 사용한다.

```
{base}.interfaces     → 외부 요청 진입 (HTTP, 메시지, 배치 등)
{base}.application    → 유스케이스 조합·진입 서비스
{base}.domain         → 비즈니스 로직, 저장소·도메인 인터페이스
{base}.infrastructure → 저장소·외부 연동 구현체
{base}.support        → 공통 예외·에러·유틸
```

- **interfaces**: 레이어드/클린 아키텍처에서 널리 쓰는 이름. (대안: `interfaces` — 진입점 전반을 강조할 때)
- **support**: 공통/크로스커팅. (대안: `common` — 팀 컨벤션에 맞춰 선택)

## 2. 의존성 방향 · DIP (Dependency Inversion Principle)

- **안쪽 → 바깥쪽만** 허용: `domain` ← `application` ← `interfaces`, `domain` ← `infrastructure`.
- **domain**은 다른 계층에 의존하지 않는다 (순수 비즈니스).
- **infrastructure**는 `domain` 인터페이스만 구현하고, interfaces/application 타입을 직접 참조하지 않는다.

**DIP**: 고수준(domain, application)은 저수준(infrastructure)에 의존하지 않고 **추상(인터페이스)**에만 의존한다.
저장소·외부 연동 등은 domain에 인터페이스를 두고, infrastructure에서 구현체를 둔다.
이렇게 해야 domain이 DB·HTTP·외부 라이브러리 변경에 흔들리지 않는다.

## 3. 레이어별 역할 (원칙)

요구사항에 맞게 구체적인 클래스 이름은 자유롭게 정한다. 아래는 **역할과 원칙**만 고정한다.

### interfaces (진입 계층)

- **역할**: HTTP·메시지·배치 등 외부 요청을 받아 application/domain으로 넘긴다.
- **포함**: 컨트롤러/핸들러, 요청·응답 DTO, 공통 응답 래퍼·헤더 상수.
- **원칙**: 도메인/인프라 타입을 그대로 노출하지 않고, DTO 등으로 변환해 반환한다.

### application (유스케이스)

- **역할**: 하나의 유스케이스를 하나의 진입 서비스로 묶고, 필요한 도메인 서비스를 조합한다.
- **포함**: 유스케이스 진입 클래스, 진입용 요청/반환 모델 (필요에 따라 생략 가능).
- **원칙**: 트랜잭션·도메인 이벤트 발행은 도메인 서비스에 위임한다.
- **원칙**: Repository를 직접 호출하지 않는다. 반드시 도메인 서비스(`XxxService`)를 통해 접근한다 (Facade → Service → Repository).

### domain (비즈니스)

- **역할**: 비즈니스 규칙과 도메인 모델, 저장소·외부 연동의 **인터페이스**만 둔다.
- **포함**: 도메인 서비스, Repository 인터페이스, 도메인 모델, 도메인 예외·이벤트.
- **원칙**: JPA·HTTP·외부 라이브러리에 의존하지 않는다. 저장소는 인터페이스만 두고 구현은 infrastructure에 둔다.

### infrastructure (구현체)

- **역할**: domain에 정의한 Repository·어댑터 등을 DB·캐시·외부 API로 구현한다.
- **포함**: Repository 구현체 (JPA Repository), JPA Entity, 외부 API 클라이언트.
- **원칙**: 도메인 인터페이스를 구현하고, JPA Entity ↔ 도메인 변환은 이 계층에서만 한다 (예: `toDomain()` / `fromDomain()`).
- **원칙**: JPA Repository(Spring Data JPA)는 Repository 구현체 내부에서만 사용하고, 다른 계층에 노출하지 않는다.

#### infrastructure 패키지 내부 구조 (필수 패턴)

각 도메인 영역의 infrastructure 패키지는 **JpaRepository + RepositoryImpl 분리 패턴**을 따른다.

```
infrastructure/{domain}/
├── XxxJpaRepository.java     ← JpaRepository<XxxEntity, Long> 상속 (순수 Spring Data JPA 인터페이스, domain Repository를 상속하지 않음)
├── XxxEntity.java            ← @Entity JPA 엔티티 (도메인 모델과 분리)
├── XxxRepositoryImpl.java    ← @Repository @RequiredArgsConstructor, domain Repository를 implements, JpaRepository를 주입받아 위임
└── (필요 시) 추가 JpaRepository나 어댑터 구현체
```

- **XxxJpaRepository**: `JpaRepository`를 상속한 순수 Spring Data JPA 인터페이스. domain의 Repository를 **extends하지 않는다**.
- **XxxEntity**: `@Entity` JPA 엔티티. 도메인 모델과 분리하여 `toDomain()` / `fromDomain()` 변환 메서드를 제공한다.
- **XxxRepositoryImpl**: `@Repository`로 domain Repository를 구현하고, 내부에서 JpaRepository를 주입받아 위임 호출한다.
- **금지**: JpaRepository가 domain Repository를 직접 상속하는 패턴 (JPA 인터페이스와 도메인 Repository 계약이 결합되어 유연성이 떨어짐).

#### JPA 사용 규칙

- **Spring Data JPA**를 사용하며, 쿼리 메서드 네이밍 컨벤션(`findByXxx`, `existsByXxx` 등)을 우선 활용한다.
- **복잡한 쿼리**: `@Query` 어노테이션으로 JPQL을 작성하거나, QueryDSL을 사용한다.
- **네이티브 쿼리**: 성능상 꼭 필요한 경우에만 `@Query(nativeQuery = true)`를 사용한다.
- **JPA Entity**: `@Entity` 클래스에는 비즈니스 로직을 넣지 않는다. 순수 매핑 용도로만 사용한다.
- **연관관계**: 양방향 매핑은 꼭 필요한 경우에만 사용하고, 기본적으로 단방향을 선호한다. `FetchType.LAZY`를 기본으로 한다.

### support (공통)

- **역할**: 여러 계층에서 쓰는 예외·에러 코드·전역 예외 처리.
- **포함**: 공통 예외 클래스, 에러 코드 enum, `@ControllerAdvice` 등 (필요할 때만 사용).

## 4. 도메인 모델 원칙

- 도메인 모델은 **프레임워크에 종속되지 않도록** (JPA 매핑 관련 어노테이션·설정은 infrastructure의 Entity에서만 사용).
- 생성 방식은 팀/프로젝트에 맞게 (Builder, 정적 팩토리, record 등).
- 비즈니스 검증은 도메인 내부에서 수행한다.

## 5. 검증(Validation) 책임 분리

- **DTO**: `@NotBlank`, `@Email` 등 입력값 존재 여부와 기본 타입 형식만 검증한다. `@Pattern`, `@Size` 같은 비즈니스 규칙성 어노테이션은 붙이지 않는다.
- **도메인 서비스**: 아이디 형식, 비밀번호 복잡도, 연구자번호 형식 등 비즈니스 규칙 검증을 수행한다. 중복 ID, 계정 잠금, 토큰 만료 등 도메인 상태 검증도 여기서 한다.
- 검증 실패 시 `CoreException(ErrorType.BAD_REQUEST, "메시지")`를 던진다.
- 규칙 변경 시 도메인 서비스 한 곳만 수정하면 되도록, 검증 로직이 DTO와 서비스에 분산되지 않도록 한다.

## 6. 체크리스트 (리뷰 시)

- [ ] 패키지: interfaces, application, domain, infrastructure, support 유지
- [ ] 의존성: domain은 다른 계층·프레임워크에 의존하지 않음 (DIP)
- [ ] DIP: 저장소·외부 연동은 domain에 인터페이스, infrastructure에 구현체
- [ ] application(Facade)에서 Repository 직접 호출 금지 — Service를 통해 접근
- [ ] application에서 interfaces DTO(XxxV1Dto 등) import 금지 — application 전용 타입 사용
- [ ] DB 매핑 객체·외부 모델 변환: infrastructure에서만 수행
- [ ] JPA Repository·Entity는 infrastructure 내부에서만 사용 — 다른 계층에서 직접 참조 금지
