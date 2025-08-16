#!/bin/bash

# 상품 데이터 생성 및 실행 스크립트
# CSV 파일을 읽어서 SQL을 생성하고 MySQL에서 실행

set -e  # 에러 발생 시 스크립트 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 설정 변수
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CSV_DIR="$SCRIPT_DIR/../qa"
OUTPUT_SQL="$SCRIPT_DIR/generated_product_data.sql"
LOG_FILE="$SCRIPT_DIR/execution.log"

# MySQL 연결 정보 (환경변수 또는 기본값 사용)
DB_HOST="${MYSQL_HOST:-localhost}"
DB_PORT="${MYSQL_PORT:-3306}"
DB_NAME="${MYSQL_DATABASE:-loopers}"
DB_USER="${MYSQL_USER:-application}"
DB_PASSWORD="${MYSQL_PASSWORD:-application}"

# 로그 함수
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" | tee -a "$LOG_FILE"
    exit 1
}

# 헬프 메시지
show_help() {
    echo "사용법: $0 [옵션]"
    echo ""
    echo "옵션:"
    echo "  -h, --help              이 도움말을 표시"
    echo "  -g, --generate-only     SQL 파일만 생성 (실행하지 않음)"
    echo "  -e, --execute-only      기존 SQL 파일만 실행 (생성하지 않음)"
    echo "  -f, --force             기존 데이터 삭제 확인 없이 실행"
    echo "  -d, --database DB_NAME  데이터베이스 이름 지정 (기본값: $DB_NAME)"
    echo "  -u, --user USER         MySQL 사용자명 지정 (기본값: $DB_USER)"
    echo "  -p, --password PASS     MySQL 비밀번호 지정"
    echo "  -H, --host HOST         MySQL 호스트 지정 (기본값: $DB_HOST)"
    echo "  -P, --port PORT         MySQL 포트 지정 (기본값: $DB_PORT)"
    echo ""
    echo "환경변수:"
    echo "  MYSQL_HOST, MYSQL_PORT, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD"
    echo ""
    echo "예시:"
    echo "  $0                           # 기본 설정으로 실행"
    echo "  $0 -g                        # SQL 파일만 생성"
    echo "  $0 -d mydb -u root -p pass   # 특정 DB에 실행"
}

# 옵션 파싱
GENERATE_ONLY=false
EXECUTE_ONLY=false
FORCE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -g|--generate-only)
            GENERATE_ONLY=true
            shift
            ;;
        -e|--execute-only)
            EXECUTE_ONLY=true
            shift
            ;;
        -f|--force)
            FORCE=true
            shift
            ;;
        -d|--database)
            DB_NAME="$2"
            shift 2
            ;;
        -u|--user)
            DB_USER="$2"
            shift 2
            ;;
        -p|--password)
            DB_PASSWORD="$2"
            shift 2
            ;;
        -H|--host)
            DB_HOST="$2"
            shift 2
            ;;
        -P|--port)
            DB_PORT="$2"
            shift 2
            ;;
        *)
            error "알 수 없는 옵션: $1"
            ;;
    esac
done

# 필수 도구 확인
check_requirements() {
    log "필수 도구 확인 중..."
    
    if ! command -v node &> /dev/null; then
        error "Node.js가 설치되어 있지 않습니다."
    fi
    
    if ! command -v mysql &> /dev/null; then
        error "MySQL 클라이언트가 설치되어 있지 않습니다."
    fi
    
    log "✓ 모든 필수 도구가 설치되어 있습니다."
}

# CSV 파일 확인
check_csv_files() {
    log "CSV 파일 확인 중..."
    
    local files=("$CSV_DIR/determiner.csv" "$CSV_DIR/item.csv" "$CSV_DIR/brand_dummy.csv")
    
    for file in "${files[@]}"; do
        if [[ ! -f "$file" ]]; then
            error "CSV 파일을 찾을 수 없습니다: $file"
        fi
        log "✓ $file"
    done
}

# SQL 파일 생성
generate_sql() {
    if [[ "$EXECUTE_ONLY" == true ]]; then
        log "SQL 생성 단계를 건너뜁니다."
        return 0
    fi
    
    log "SQL 파일 생성 중..."
    
    if [[ ! -f "$SCRIPT_DIR/generate_sql.js" ]]; then
        error "generate_sql.js 파일을 찾을 수 없습니다."
    fi
    
    cd "$SCRIPT_DIR"
    
    if ! node generate_sql.js; then
        error "SQL 파일 생성에 실패했습니다."
    fi
    
    if [[ ! -f "$OUTPUT_SQL" ]]; then
        error "SQL 파일이 생성되지 않았습니다."
    fi
    
    log "✓ SQL 파일이 생성되었습니다: $OUTPUT_SQL"
}

# MySQL 연결 테스트
test_mysql_connection() {
    log "MySQL 연결 테스트 중..."
    
    local test_query="SELECT 1 as test;"
    
    if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "$test_query" &>/dev/null; then
        error "MySQL 연결에 실패했습니다. 연결 정보를 확인해주세요."
    fi
    
    log "✓ MySQL 연결 성공"
}

# 데이터 삭제 확인
confirm_data_deletion() {
    if [[ "$FORCE" == true ]]; then
        log "강제 모드: 데이터 삭제 확인을 건너뜁니다."
        return 0
    fi
    
    echo -e "${YELLOW}경고: 이 작업은 기존 상품 데이터를 모두 삭제합니다.${NC}"
    read -p "계속하시겠습니까? (y/N): " -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log "사용자가 작업을 취소했습니다."
        exit 0
    fi
}

# SQL 실행
execute_sql() {
    if [[ "$GENERATE_ONLY" == true ]]; then
        log "SQL 실행 단계를 건너뜁니다."
        return 0
    fi
    
    log "SQL 실행 중..."
    log "데이터베이스: $DB_NAME@$DB_HOST:$DB_PORT"
    
    # 실행 시간 측정
    local start_time=$(date +%s)
    
    if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$OUTPUT_SQL"; then
        error "SQL 실행에 실패했습니다. 로그를 확인해주세요."
    fi
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    log "✓ SQL 실행 완료 (소요시간: ${duration}초)"
}

# 결과 확인
verify_results() {
    if [[ "$GENERATE_ONLY" == true ]]; then
        return 0
    fi
    
    log "결과 확인 중..."
    
    local count_query="SELECT COUNT(*) as product_count FROM product;"
    local count_result=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -s -e "$count_query" 2>/dev/null)
    
    if [[ -n "$count_result" ]]; then
        log "✓ 생성된 상품 수: $count_result개"
        
        # 샘플 데이터 확인
        local sample_query="SELECT id, name, brand_id, price, stock FROM product LIMIT 5;"
        log "샘플 데이터:"
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "$sample_query" | tee -a "$LOG_FILE"
    else
        warn "상품 수를 확인할 수 없습니다."
    fi
}

# 정리
cleanup() {
    log "작업 완료!"
    log "로그 파일: $LOG_FILE"
    
    if [[ -f "$OUTPUT_SQL" ]]; then
        log "생성된 SQL 파일: $OUTPUT_SQL"
    fi
}

# 메인 실행
main() {
    log "=== 상품 데이터 생성 및 실행 스크립트 시작 ==="
    log "작업 디렉토리: $SCRIPT_DIR"
    
    # 초기화
    check_requirements
    check_csv_files
    
    # SQL 생성
    generate_sql
    
    # SQL 실행
    if [[ "$GENERATE_ONLY" != true ]]; then
        test_mysql_connection
        confirm_data_deletion
        execute_sql
        verify_results
    fi
    
    # 정리
    cleanup
}

# 스크립트 실행
main "$@"
