# 상품 데이터 생성 및 실행 스크립트 (PowerShell)

param(
    [switch]$Help,
    [switch]$GenerateOnly,
    [switch]$ExecuteOnly,
    [switch]$Force,
    [string]$Database = "loopers",
    [string]$User = "application",
    [string]$Password = "application",
    [string]$ServerHost = "localhost",
    [string]$Port = "3306"
)

# 설정 변수
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$CsvDir = Join-Path $ScriptDir "..\qa"
$OutputSql = Join-Path $ScriptDir "generated_product_data.sql"
$LogFile = Join-Path $ScriptDir "execution.log"

# MySQL 연결 정보
$DBHost = if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { $ServerHost }
$DBPort = if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { $Port }
$DBName = if ($env:MYSQL_DATABASE) { $env:MYSQL_DATABASE } else { $Database }
$DBUser = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { $User }
$DBPassword = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { $Password }

# 로그 함수
function Write-Log {
    param([string]$Message)
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] $Message"
    
    Write-Host $logMessage -ForegroundColor Green
    Add-Content -Path $LogFile -Value $logMessage
}

function Write-Warning {
    param([string]$Message)
    Write-Host "WARNING: $Message" -ForegroundColor Yellow
    Add-Content -Path $LogFile -Value "WARNING: $Message"
}

function Write-Error {
    param([string]$Message)
    Write-Host "ERROR: $Message" -ForegroundColor Red
    Add-Content -Path $LogFile -Value "ERROR: $Message"
    exit 1
}

# 헬프 메시지
function Show-Help {
    Write-Host "사용법: $($MyInvocation.MyCommand.Name) [옵션]" -ForegroundColor Blue
    Write-Host ""
    Write-Host "옵션:"
    Write-Host "  -Help              이 도움말을 표시"
    Write-Host "  -GenerateOnly      SQL 파일만 생성 (실행하지 않음)"
    Write-Host "  -ExecuteOnly       기존 SQL 파일만 실행 (생성하지 않음)"
    Write-Host "  -Force             기존 데이터 삭제 확인 없이 실행"
    Write-Host "  -Database DB_NAME  데이터베이스 이름 지정 (기본값: $Database)"
    Write-Host "  -User USER         MySQL 사용자명 지정 (기본값: $User)"
    Write-Host "  -Password PASS     MySQL 비밀번호 지정"
    Write-Host "  -ServerHost HOST   MySQL 호스트 지정 (기본값: $ServerHost)"
    Write-Host "  -Port PORT         MySQL 포트 지정 (기본값: $Port)"
    Write-Host ""
    Write-Host "환경변수:"
    Write-Host "  MYSQL_HOST, MYSQL_PORT, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD"
    Write-Host ""
    Write-Host "예시:"
    Write-Host "  $($MyInvocation.MyCommand.Name)                           # 기본 설정으로 실행"
    Write-Host "  $($MyInvocation.MyCommand.Name) -GenerateOnly              # SQL 파일만 생성"
    Write-Host "  $($MyInvocation.MyCommand.Name) -Database mydb -User root -Password pass   # 특정 DB에 실행"
}

# 도움말 표시
if ($Help) {
    Show-Help
    exit 0
}

# 필수 도구 확인
function Test-Requirements {
    Write-Log "필수 도구 확인 중..."
    
    # Node.js 확인
    try {
        $nodeVersion = node --version 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Node.js가 설치되어 있지 않습니다."
        }
        Write-Log "Node.js: $nodeVersion"
    }
    catch {
        Write-Error "Node.js가 설치되어 있지 않습니다."
    }
    
    # MySQL 클라이언트 확인
    try {
        $mysqlVersion = mysql --version 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-Error "MySQL 클라이언트가 설치되어 있지 않습니다."
        }
        Write-Log "MySQL: $mysqlVersion"
    }
    catch {
        Write-Error "MySQL 클라이언트가 설치되어 있지 않습니다."
    }
    
    Write-Log "모든 필수 도구가 설치되어 있습니다."
}

# CSV 파일 확인
function Test-CsvFiles {
    Write-Log "CSV 파일 확인 중..."
    
    $files = @(
        (Join-Path $CsvDir "determiner.csv"),
        (Join-Path $CsvDir "item.csv"),
        (Join-Path $CsvDir "brand_dummy.csv")
    )
    
    foreach ($file in $files) {
        if (-not (Test-Path $file)) {
            Write-Error "CSV 파일을 찾을 수 없습니다: $file"
        }
        Write-Log "파일 확인됨: $file"
    }
}

# SQL 파일 생성
function Generate-Sql {
    if ($ExecuteOnly) {
        Write-Log "SQL 생성 단계를 건너뜁니다."
        return
    }
    
    Write-Log "SQL 파일 생성 중..."
    
    $generateScript = Join-Path $ScriptDir "generate_sql.js"
    if (-not (Test-Path $generateScript)) {
        Write-Error "generate_sql.js 파일을 찾을 수 없습니다."
    }
    
    Set-Location $ScriptDir
    
    try {
        $result = node generate_sql.js 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Error "SQL 파일 생성에 실패했습니다: $result"
        }
    }
    catch {
        Write-Error "SQL 파일 생성 중 오류가 발생했습니다: $_"
    }
    
    if (-not (Test-Path $OutputSql)) {
        Write-Error "SQL 파일이 생성되지 않았습니다."
    }
    
    Write-Log "SQL 파일이 생성되었습니다: $OutputSql"
}

# MySQL 연결 테스트
function Test-MySqlConnection {
    Write-Log "MySQL 연결 테스트 중..."
    
    $testQuery = "SELECT 1 as test;"
    $mysqlArgs = @(
        "-h$DBHost",
        "-P$DBPort",
        "-u$DBUser",
        "-p$DBPassword",
        $DBName,
        "-e",
        $testQuery
    )
    
    try {
        $result = mysql @mysqlArgs 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-Error "MySQL 연결에 실패했습니다. 연결 정보를 확인해주세요."
        }
        Write-Log "MySQL 연결 성공"
    }
    catch {
        Write-Error "MySQL 연결 중 오류가 발생했습니다: $_"
    }
}

# 데이터 삭제 확인
function Confirm-DataDeletion {
    if ($Force) {
        Write-Log "강제 모드: 데이터 삭제 확인을 건너뜁니다."
        return
    }
    
    Write-Host "경고: 이 작업은 기존 상품 데이터를 모두 삭제합니다." -ForegroundColor Yellow
    $response = Read-Host "계속하시겠습니까? (y/N)"
    
    if ($response -notmatch "^[Yy]$") {
        Write-Log "사용자가 작업을 취소했습니다."
        exit 0
    }
}

# SQL 실행
function Execute-Sql {
    if ($GenerateOnly) {
        Write-Log "SQL 실행 단계를 건너뜁니다."
        return
    }
    
    Write-Log "SQL 실행 중..."
    Write-Log "데이터베이스: $DBName@$DBHost`:$DBPort"
    
    # 실행 시간 측정
    $startTime = Get-Date
    
    $mysqlArgs = @(
        "-h$DBHost",
        "-P$DBPort",
        "-u$DBUser",
        "-p$DBPassword",
        $DBName
    )
    
    try {
        Get-Content $OutputSql | mysql @mysqlArgs
        if ($LASTEXITCODE -ne 0) {
            Write-Error "SQL 실행에 실패했습니다. 로그를 확인해주세요."
        }
    }
    catch {
        Write-Error "SQL 실행 중 오류가 발생했습니다: $_"
    }
    
    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalSeconds
    
    Write-Log "SQL 실행 완료 (소요시간: $([math]::Round($duration, 2))초)"
}

# 결과 확인
function Verify-Results {
    if ($GenerateOnly) {
        return
    }
    
    Write-Log "결과 확인 중..."
    
    $countQuery = "SELECT COUNT(*) as product_count FROM product;"
    $mysqlArgs = @(
        "-h$DBHost",
        "-P$DBPort",
        "-u$DBUser",
        "-p$DBPassword",
        $DBName,
        "-s",
        "-e",
        $countQuery
    )
    
    try {
        $countResult = mysql @mysqlArgs 2>$null
        if ($countResult -and $countResult -match "^\d+$") {
            Write-Log "생성된 상품 수: $countResult개"
            
            # 샘플 데이터 확인
            $sampleQuery = "SELECT id, name, brand_id, price, stock FROM product LIMIT 5;"
            $mysqlArgs[-2] = $sampleQuery
            Write-Log "샘플 데이터:"
            mysql @mysqlArgs | Tee-Object -FilePath $LogFile -Append
        }
        else {
            Write-Warning "상품 수를 확인할 수 없습니다."
        }
    }
    catch {
        Write-Warning "결과 확인 중 오류가 발생했습니다: $_"
    }
}

# 정리
function Cleanup {
    Write-Log "작업 완료!"
    Write-Log "로그 파일: $LogFile"
    
    if (Test-Path $OutputSql) {
        Write-Log "생성된 SQL 파일: $OutputSql"
    }
}

# 메인 실행
function Main {
    Write-Log "상품 데이터 생성 및 실행 스크립트 시작"
    Write-Log "작업 디렉토리: $ScriptDir"
    
    # 초기화
    Test-Requirements
    Test-CsvFiles
    
    # SQL 생성
    Generate-Sql
    
    # SQL 실행
    if (-not $GenerateOnly) {
        Test-MySqlConnection
        Confirm-DataDeletion
        Execute-Sql
        Verify-Results
    }
    
    # 정리
    Cleanup
}

# 스크립트 실행
try {
    Main
}
catch {
    Write-Error "스크립트 실행 중 오류가 발생했습니다: $_"
}
finally {
    if (Test-Path $LogFile) {
        Write-Log "로그 파일이 생성되었습니다: $LogFile"
    }
}
