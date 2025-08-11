const fs = require('fs');
const path = require('path');

// CSV 파일 경로
const CSV_PATHS = {
    determiner: '../qa/determiner.csv',
    item: '../qa/item.csv',
    brand: '../qa/brand_dummy.csv'
};

// SQL 파일 출력 경로
const OUTPUT_SQL_PATH = './generated_product_data.sql';

/**
 * CSV 파일을 읽어서 배열로 반환
 * @param {string} filePath - CSV 파일 경로
 * @returns {string[]} CSV 데이터 배열
 */
function readCSV(filePath) {
    try {
        const fullPath = path.resolve(__dirname, filePath);
        const content = fs.readFileSync(fullPath, 'utf8');
        
        // 첫 번째 줄이 헤더인 경우 제거 (brand_dummy.csv의 경우)
        const lines = content.split('\n').filter(line => line.trim());
        
        if (filePath.includes('brand_dummy.csv')) {
            // 브랜드 파일은 첫 번째 줄이 헤더이므로 제거
            return lines.slice(1).filter(line => line.trim());
        }
        
        return lines.filter(line => line.trim());
    } catch (error) {
        console.error(`CSV 파일 읽기 실패: ${filePath}`, error);
        return [];
    }
}

/**
 * SQL INSERT 문 생성
 * @param {string} tableName - 테이블명
 * @param {string} columnName - 컬럼명
 * @param {string[]} values - 값 배열
 * @returns {string} SQL INSERT 문
 */
function generateInsertSQL(tableName, columnName, values) {
    if (values.length === 0) return '';
    
    const valueStrings = values.map(value => `('${value.replace(/'/g, "''")}')`);
    
    return `INSERT INTO ${tableName} (${columnName}) VALUES\n${valueStrings.join(',\n')};`;
}

/**
 * 메인 SQL 스크립트 생성
 * @param {Object} data - CSV 데이터
 * @returns {string} 전체 SQL 스크립트
 */
function generateMainSQL(data) {
    const { determiner, item, brand } = data;
    
    return `-- 상품 데이터 생성 스크립트
-- CSV 파일들을 임시 테이블로 넣고 크로스 조인해서 상품 테이블에 데이터 생성
-- 생성 시간: ${new Date().toISOString()}

-- 1. 임시 테이블 생성
CREATE TEMPORARY TABLE temp_determiner (
    id INT AUTO_INCREMENT PRIMARY KEY,
    determiner VARCHAR(100) NOT NULL
);

CREATE TEMPORARY TABLE temp_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item VARCHAR(100) NOT NULL
);

CREATE TEMPORARY TABLE temp_brand (
    id INT AUTO_INCREMENT PRIMARY KEY,
    brand_name VARCHAR(500) NOT NULL
);

-- 2. CSV 데이터 삽입

-- 관형사 데이터 삽입 (총 ${determiner.length}개)
${generateInsertSQL('temp_determiner', 'determiner', determiner)}

-- 아이템 데이터 삽입 (총 ${item.length}개)
${generateInsertSQL('temp_item', 'item', item)}

-- 브랜드 데이터 삽입 (총 ${brand.length}개)
${generateInsertSQL('temp_brand', 'brand_name', brand)}

-- 3. 기존 상품 테이블이 있다면 비우기
TRUNCATE TABLE product;

-- 4. 크로스 조인으로 상품 데이터 생성 (관형사 + 아이템만 조합)
-- 총 조합 가능한 상품 수: ${determiner.length} × ${item.length} = ${determiner.length * item.length}
-- 브랜드는 랜덤하게 할당
INSERT INTO product (name, brand_id, price, stock, likes, created_at, updated_at)
SELECT 
    CONCAT(d.determiner, ' ', i.item) as name,
    -- 브랜드는 1부터 ${brand.length} 사이의 랜덤 값으로 할당
    FLOOR(1 + (RAND() * ${brand.length})) as brand_id,
    -- 가격: 1000 ~ 1000000 사이의 랜덤 값
    FLOOR(1000 + (RAND() * 999000)) as price,
    -- 재고: 0 ~ 1000 사이의 랜덤 값
    FLOOR(RAND() * 1001) as stock,
    0 as likes,
    NOW() as created_at,
    NOW() as updated_at
FROM temp_determiner d
CROSS JOIN temp_item i
-- 브랜드는 크로스 조인하지 않고 랜덤 할당
LIMIT 900000;  -- 90만개 상품 생성

-- 5. 생성된 상품 수 확인
SELECT COUNT(*) as total_products FROM product;

-- 6. 샘플 데이터 확인
SELECT 
    p.id,
    p.name,
    b.brand_name,
    p.price,
    p.stock,
    p.likes
FROM product p
JOIN temp_brand b ON p.brand_id = b.id
LIMIT 20;

-- 7. 임시 테이블 정리
DROP TEMPORARY TABLE IF EXISTS temp_determiner;
DROP TEMPORARY TABLE IF EXISTS temp_item;
DROP TEMPORARY TABLE IF EXISTS temp_brand;

-- 8. 통계 정보
SELECT 
    '데이터 생성 완료' as status,
    ${determiner.length} as determiner_count,
    ${item.length} as item_count,
    ${brand.length} as brand_count,
    ${determiner.length * item.length} as total_combinations,
    (SELECT COUNT(*) FROM product) as actual_products;
`;
}

/**
 * 메인 실행 함수
 */
function main() {
    console.log('CSV 파일 읽는 중...');
    
    // CSV 파일들 읽기
    const data = {
        determiner: readCSV(CSV_PATHS.determiner),
        item: readCSV(CSV_PATHS.item),
        brand: readCSV(CSV_PATHS.brand)
    };
    
    console.log(`관형사: ${data.determiner.length}개`);
    console.log(`아이템: ${data.item.length}개`);
    console.log(`브랜드: ${data.brand.length}개`);
    console.log(`총 조합 가능한 상품 수: ${data.determiner.length * data.item.length}개`);
    console.log(`브랜드는 랜덤 할당으로 90만개 상품 생성 예정`);
    
    // SQL 스크립트 생성
    const sqlScript = generateMainSQL(data);
    
    // SQL 파일 저장
    const outputPath = path.resolve(__dirname, OUTPUT_SQL_PATH);
    fs.writeFileSync(outputPath, sqlScript, 'utf8');
    
    console.log(`\nSQL 파일이 생성되었습니다: ${outputPath}`);
    console.log('이제 MySQL에서 이 파일을 실행하여 상품 데이터를 생성할 수 있습니다.');
    
    // 실행 예시 출력
    console.log('\n실행 방법:');
    console.log(`mysql -u [username] -p [database_name] < ${OUTPUT_SQL_PATH}`);
    console.log('또는 MySQL Workbench에서 파일을 열어서 실행');
}

// 스크립트 실행
if (require.main === module) {
    main();
}

module.exports = {
    readCSV,
    generateInsertSQL,
    generateMainSQL
};
