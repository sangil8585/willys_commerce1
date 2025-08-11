# SQL INSERT 문 생성기

CSV 파일을 읽어서 데이터베이스 테이블용 SQL INSERT 문을 자동으로 생성하는 Node.js 도구입니다.

## 📁 파일 구성

- **`generateSqlInserts.js`** - 메인 스크립트 파일
- **`package.json`** - Node.js 프로젝트 설정
- **`README.md`** - 사용법 가이드

## 🚀 설치 및 실행

### 1. Node.js 설치 확인
```bash
node --version
# v14.0.0 이상 필요
```

### 2. 프로젝트 실행
```bash
# 방법 1: npm 스크립트 사용
npm start

# 방법 2: 직접 실행
node generateSqlInserts.js

# 방법 3: npm run 사용
npm run generate
```

## 📊 입력 데이터 구조

스크립트는 다음 CSV 파일들을 읽어서 처리합니다:

### 필수 CSV 파일들 (../data/ 폴더에 위치)
- **`user_sample_100.csv`** - 사용자 샘플 데이터
  - 컬럼: loginId, gender, birthDate
- **`brand_sample_unique.csv`** - 브랜드 샘플 데이터
  - 컬럼: name
- **`adjectives_1000_attributive.csv`** - 형용사 데이터
  - 컬럼: adjective
- **`nouns_1000.csv`** - 명사 데이터
  - 컬럼: noun
- **`categories_50.csv`** - 카테고리 데이터
  - 컬럼: name

## 🗄️ 생성되는 테이블

### 1. user 테이블
- 사용자 기본 정보 (loginId, gender, birthDate)
- CSV에서 직접 읽어온 데이터 사용

### 2. brand 테이블
- 브랜드 정보
- CSV에서 직접 읽어온 데이터 사용

### 3. category 테이블
- 상품 카테고리 정보
- CSV에서 직접 읽어온 데이터 사용

### 4. product 테이블
- 상품 정보 (형용사 + 명사로 상품명 생성)
- 브랜드당 20개씩 생성
- 랜덤 가격 (50,000 ~ 1,000,000원)
- 랜덤 재고 (10 ~ 100개)

### 5. order 테이블
- 주문 정보
- 사용자당 1~3개 주문 생성
- 주문 상태: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

### 6. order_item 테이블
- 주문 상품 정보
- 주문당 1~5개 상품
- 랜덤 수량 (1~3개)

### 7. coupon 테이블
- 쿠폰 정보
- 총 50개 쿠폰 생성
- 타입: PERCENTAGE(10~40%), FIXED_AMOUNT(10,000~60,000원)

### 8. point 테이블
- 포인트 적립/사용 내역
- 사용자당 1~5개 포인트 기록
- 타입: EARN(적립), USE(사용), EXPIRE(만료)

## 🔧 커스터마이징

### 상품 개수 조정
```javascript
// generateProductInserts() 함수에서
const productsPerBrand = 20; // 브랜드당 상품 개수
```

### 가격 범위 조정
```javascript
// 랜덤 가격 범위
const price = Math.floor(Math.random() * 950000) + 50000; // 50,000 ~ 1,000,000
```

### 재고 범위 조정
```javascript
// 랜덤 재고 범위
const stock = Math.floor(Math.random() * 91) + 10; // 10 ~ 100
```

### 주문 개수 조정
```javascript
// 사용자당 주문 개수
const orderCount = Math.floor(Math.random() * 3) + 1; // 1 ~ 3개
```

## 📤 출력 결과

스크립트 실행 후 `generated_inserts.sql` 파일이 생성됩니다.

### 실행 예시
```bash
$ npm start

SQL INSERT 문 생성을 시작합니다...
로드된 데이터:
- 사용자: 100명
- 브랜드: 50개
- 형용사: 1000개
- 명사: 1000개
- 카테고리: 50개

=== 사용자 INSERT 문 생성 ===
=== 브랜드 INSERT 문 생성 ===
=== 카테고리 INSERT 문 생성 ===
=== 상품 INSERT 문 생성 ===
=== 주문 INSERT 문 생성 ===
=== 쿠폰 INSERT 문 생성 ===
=== 포인트 INSERT 문 생성 ===

✅ SQL INSERT 문 생성 완료!
📁 저장 위치: /path/to/generated_inserts.sql
📊 총 INSERT 문 개수: 2,450개

생성된 테이블:
- user: 100개
- brand: 50개
- category: 50개
- product: 1000개
- order: 200개
- coupon: 50개
- point: 300개

🎉 모든 작업이 완료되었습니다!
```

## ⚠️ 주의사항

1. **CSV 파일 경로**: `../data/` 폴더에 필요한 CSV 파일들이 있어야 합니다.
2. **데이터 형식**: CSV 파일은 헤더가 있어야 하며, 첫 번째 줄은 건너뜁니다.
3. **데이터베이스 호환성**: MySQL/MariaDB 문법을 기준으로 생성됩니다.
4. **대용량 데이터**: 많은 데이터를 생성할 때는 메모리 사용량을 고려하세요.

## 🐛 문제 해결

### CSV 파일을 찾을 수 없는 경우
```bash
Error: ENOENT: no such file or directory, open '../data/user_sample_100.csv'
```
- CSV 파일들이 올바른 경로에 있는지 확인
- 상대 경로가 올바른지 확인

### 메모리 부족 오류
- Node.js 메모리 제한 증가:
```bash
node --max-old-space-size=4096 generateSqlInserts.js
```

## 📝 라이센스

MIT License

## 🤝 기여

버그 리포트나 기능 제안은 이슈로 등록해 주세요.
