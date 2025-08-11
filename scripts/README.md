# 상품 데이터 생성 스크립트

CSV 파일들을 읽어서 상품 테이블에 데이터를 생성하는 SQL 스크립트를 동적으로 생성하고 실행합니다.

## 파일 구조

```
scripts/
├── generate_sql.js                    # 메인 스크립트 (Node.js)
├── generate_and_execute.sh            # Linux/Mac용 실행 스크립트
├── generate_and_execute.ps1           # Windows PowerShell용 실행 스크립트
├── package.json                       # Node.js 패키지 설정
├── README.md                         # 이 파일
├── generated_product_data.sql         # 생성될 SQL 파일 (실행 후 생성됨)
└── execution.log                     # 실행 로그 (실행 후 생성됨)
```

## CSV 파일

- `../qa/determiner.csv` - 관형사 데이터 (900개)
- `../qa/item.csv` - 아이템 데이터 (1,000개)  
- `../qa/brand_dummy.csv` - 브랜드 데이터 (1,000개)

## 🚀 빠른 시작

### 1. SQL 파일만 생성
```bash
# Linux/Mac
./generate_and_execute.sh -g

# Windows PowerShell
.\generate_and_execute.ps1 -GenerateOnly
```

### 2. 전체 과정 실행 (SQL 생성 + MySQL 실행)
```bash
# Linux/Mac
./generate_and_execute.sh

# Windows PowerShell
.\generate_and_execute.ps1
```

### 3. 기존 SQL 파일만 실행
```bash
# Linux/Mac
./generate_and_execute.sh -e

# Windows PowerShell
.\generate_and_execute.ps1 -ExecuteOnly
```

## 📋 상세 사용법

### Linux/Mac (Bash)
```bash
# 기본 실행 (기본 설정)
./generate_and_execute.sh

# SQL 파일만 생성
./generate_and_execute.sh -g

# 기존 SQL 파일만 실행
./generate_and_execute.sh -e

# 강제 실행 (확인 없이)
./generate_and_execute.sh -f

# 특정 데이터베이스에 실행
./generate_and_execute.sh -d mydb -u root -p password

# 도움말
./generate_and_execute.sh -h
```

### Windows PowerShell
```powershell
# 기본 실행 (기본 설정)
.\generate_and_execute.ps1

# SQL 파일만 생성
.\generate_and_execute.ps1 -GenerateOnly

# 기존 SQL 파일만 실행
.\generate_and_execute.ps1 -ExecuteOnly

# 강제 실행 (확인 없이)
.\generate_and_execute.ps1 -Force

# 특정 데이터베이스에 실행
.\generate_and_execute.ps1 -Database mydb -User root -Password password

# 도움말
.\generate_and_execute.ps1 -Help
```

## ⚙️ 설정

### 환경변수 설정
```bash
# Linux/Mac
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=loopers
export MYSQL_USER=application
export MYSQL_PASSWORD=application

# Windows PowerShell
$env:MYSQL_HOST = "localhost"
$env:MYSQL_PORT = "3306"
$env:MYSQL_DATABASE = "loopers"
$env:MYSQL_USER = "application"
$env:MYSQL_PASSWORD = "application"
```

### 기본값
- **호스트**: localhost
- **포트**: 3306
- **데이터베이스**: loopers
- **사용자**: application
- **비밀번호**: application

## 🔧 수동 실행 방법

### 1. Node.js 스크립트만 실행
```bash
cd scripts
node generate_sql.js
```

### 2. 생성된 SQL 파일 수동 실행
```bash
mysql -u [username] -p [database_name] < generated_product_data.sql
```

## 📊 생성되는 데이터

### 상품 데이터 구조
- **상품명**: "관형사 + 아이템" 형태 (예: "하얀 바다의 지팡이혼")
- **브랜드**: 1~1,000 사이의 랜덤 값
- **가격**: 1,000 ~ 1,000,000 사이의 랜덤 값
- **재고**: 0 ~ 1,000 사이의 랜덤 값
- **좋아요**: 0 (초기값)

### 데이터 양
- **관형사**: 900개
- **아이템**: 1,000개  
- **브랜드**: 1,000개 (랜덤 할당)
- **총 조합 가능한 상품 수**: 900 × 1,000 = **90만개**
- **실제 생성되는 상품 수**: **90만개**

## 🛡️ 안전 기능

### 데이터 보호
- 기존 상품 데이터 삭제 전 사용자 확인
- `-f` 또는 `-Force` 옵션으로 확인 건너뛰기 가능

### 에러 처리
- 각 단계별 에러 체크
- 상세한 로그 기록
- 실패 시 스크립트 자동 중단

### 연결 테스트
- MySQL 연결 상태 사전 확인
- CSV 파일 존재 여부 확인
- 필수 도구 설치 상태 확인

## 📝 로그 및 모니터링

### 로그 파일
- **위치**: `scripts/execution.log`
- **내용**: 모든 실행 단계와 결과 기록
- **포맷**: 타임스탬프 + 메시지

### 실행 시간 측정
- SQL 실행 소요 시간 자동 기록
- 성능 모니터링 가능

## 🚨 주의사항

1. **데이터 백업**: 실행 전 기존 데이터 백업 권장
2. **권한 확인**: MySQL 사용자에게 적절한 권한 필요
3. **디스크 공간**: 90만개 상품 생성 시 충분한 공간 확보
4. **실행 시간**: 대량 데이터 생성으로 시간이 오래 걸릴 수 있음

## 🔍 문제 해결

### 일반적인 문제들

#### Node.js 오류
```bash
# Node.js 버전 확인
node --version  # 14.0.0 이상 필요

# 설치 확인
which node
```

#### MySQL 연결 오류
```bash
# MySQL 클라이언트 설치 확인
mysql --version

# 연결 테스트
mysql -h localhost -u application -p loopers
```

#### 권한 오류
```bash
# 스크립트 실행 권한 부여 (Linux/Mac)
chmod +x generate_and_execute.sh
```

### 로그 확인
```bash
# 실행 로그 확인
tail -f scripts/execution.log

# PowerShell에서 로그 확인
Get-Content scripts/execution.log -Tail 20
```

## 📚 고급 사용법

### 배치 실행
```bash
# 여러 데이터베이스에 순차 실행
for db in db1 db2 db3; do
    ./generate_and_execute.sh -d $db -f
done
```

### 스케줄링
```bash
# crontab에 등록 (매일 새벽 2시 실행)
0 2 * * * /path/to/scripts/generate_and_execute.sh -f
```

### 모니터링 스크립트
```bash
# 실행 결과 모니터링
./generate_and_execute.sh && echo "성공" || echo "실패"
```
