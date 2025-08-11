const fs = require('fs');
const path = require('path');

console.log('SQL INSERT 문 생성을 시작합니다...');

// 1. 사용자 샘플 데이터 로드 (CSV 파싱)
const userCsv = fs.readFileSync(path.join(__dirname, '../data/user_sample_100.csv'), 'utf8');
const userLines = userCsv.split('\n').slice(1); // 헤더 제거
const users = userLines
  .filter(line => line.trim()) // 빈 줄 제거
  .map(line => {
    const [loginId, gender, birthDate] = line.split(',');
    return {
      loginId: loginId.trim(),
      gender: gender.trim(),
      birthDate: birthDate.trim()
    };
  });

// 2. 브랜드 샘플 데이터 로드 (CSV 파싱)
const brandCsv = fs.readFileSync(path.join(__dirname, '../data/brand_sample_unique.csv'), 'utf8');
const brandLines = brandCsv.split('\n').slice(1); // 헤더 제거
const brands = brandLines
  .filter(line => line.trim()) // 빈 줄 제거
  .map(line => line.trim());

// 3. 형용사 데이터 로드 (CSV 파싱)
const adjectiveCsv = fs.readFileSync(path.join(__dirname, '../data/adjectives_1000_attributive.csv'), 'utf8');
const adjectiveLines = adjectiveCsv.split('\n').slice(1); // 헤더 제거
const adjectives = adjectiveLines
  .filter(line => line.trim()) // 빈 줄 제거
  .map(line => line.trim());

// 4. 명사 데이터 로드 (CSV 파싱)
const nounCsv = fs.readFileSync(path.join(__dirname, '../data/nouns_1000.csv'), 'utf8');
const nounLines = nounCsv.split('\n').slice(1); // 헤더 제거
const nouns = nounLines
  .filter(line => line.trim()) // 빈 줄 제거
  .map(line => line.trim());

// 5. 카테고리 데이터 로드 (CSV 파싱)
const categoryCsv = fs.readFileSync(path.join(__dirname, '../data/categories_50.csv'), 'utf8');
const categoryLines = categoryCsv.split('\n').slice(1); // 헤더 제거
const categories = categoryLines
  .filter(line => line.trim()) // 빈 줄 제거
  .map(line => line.trim());

console.log(`로드된 데이터:`);
console.log(`- 사용자: ${users.length}명`);
console.log(`- 브랜드: ${brands.length}개`);
console.log(`- 형용사: ${adjectives.length}개`);
console.log(`- 명사: ${nouns.length}개`);
console.log(`- 카테고리: ${categories.length}개`);
console.log();

// 6. SQL INSERT 문 생성 함수들
function generateUserInserts() {
  console.log('=== 사용자 INSERT 문 생성 ===');
  const inserts = [];
  
  users.forEach((user, index) => {
    const id = index + 1;
    const insert = `INSERT INTO user (id, login_id, gender, birth_date, created_at, updated_at) VALUES (${id}, '${user.loginId}', '${user.gender}', '${user.birthDate}', NOW(), NOW());`;
    inserts.push(insert);
  });
  
  return inserts;
}

function generateBrandInserts() {
  console.log('=== 브랜드 INSERT 문 생성 ===');
  const inserts = [];
  
  brands.forEach((brand, index) => {
    const id = index + 1;
    const insert = `INSERT INTO brand (id, name, created_at, updated_at) VALUES (${id}, '${brand}', NOW(), NOW());`;
    inserts.push(insert);
  });
  
  return inserts;
}

function generateCategoryInserts() {
  console.log('=== 카테고리 INSERT 문 생성 ===');
  const inserts = [];
  
  categories.forEach((category, index) => {
    const id = index + 1;
    const insert = `INSERT INTO category (id, name, created_at, updated_at) VALUES (${id}, '${category}', NOW(), NOW());`;
    inserts.push(insert);
  });
  
  return inserts;
}

function generateProductInserts() {
  console.log('=== 상품 INSERT 문 생성 ===');
  const inserts = [];
  
  // 상품 개수 설정 (브랜드당 20개씩)
  const productsPerBrand = 20;
  let productId = 1;
  
  brands.forEach((brand, brandIndex) => {
    const brandId = brandIndex + 1;
    
    for (let i = 0; i < productsPerBrand; i++) {
      // 랜덤 형용사와 명사 선택
      const randomAdjective = adjectives[Math.floor(Math.random() * adjectives.length)];
      const randomNoun = nouns[Math.floor(Math.random() * nouns.length)];
      const productName = `${randomAdjective} ${randomNoun}`;
      
      // 랜덤 가격 (50,000 ~ 1,000,000)
      const price = Math.floor(Math.random() * 950000) + 50000;
      
      // 랜덤 재고 (10 ~ 100)
      const stock = Math.floor(Math.random() * 91) + 10;
      
      // 랜덤 카테고리 ID
      const categoryId = Math.floor(Math.random() * categories.length) + 1;
      
      const insert = `INSERT INTO product (id, name, brand_id, category_id, price, stock, likes, created_at, updated_at) VALUES (${productId}, '${productName}', ${brandId}, ${categoryId}, ${price}, ${stock}, 0, NOW(), NOW());`;
      inserts.push(insert);
      
      productId++;
    }
  });
  
  return inserts;
}

function generateOrderInserts() {
  console.log('=== 주문 INSERT 문 생성 ===');
  const inserts = [];
  
  // 주문 개수 설정 (사용자당 1~3개씩)
  let orderId = 1;
  let orderItemId = 1;
  
  users.forEach((user, userIndex) => {
    const userId = userIndex + 1;
    const orderCount = Math.floor(Math.random() * 3) + 1; // 1~3개
    
    for (let i = 0; i < orderCount; i++) {
      // 랜덤 주문 상태
      const orderStatuses = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
      const randomStatus = orderStatuses[Math.floor(Math.random() * orderStatuses.length)];
      
      // 랜덤 주문일 (최근 1년 내)
      const orderDate = new Date();
      orderDate.setDate(orderDate.getDate() - Math.floor(Math.random() * 365));
      const orderDateStr = orderDate.toISOString().slice(0, 19).replace('T', ' ');
      
      // 주문 생성
      const orderInsert = `INSERT INTO \`order\` (id, user_id, status, total_amount, created_at, updated_at) VALUES (${orderId}, ${userId}, '${randomStatus}', 0, '${orderDateStr}', '${orderDateStr}');`;
      inserts.push(orderInsert);
      
      // 주문 아이템 생성 (1~5개)
      const itemCount = Math.floor(Math.random() * 5) + 1;
      let totalAmount = 0;
      
      for (let j = 0; j < itemCount; j++) {
        const productId = Math.floor(Math.random() * 1000) + 1; // 상품 ID 1~1000
        const quantity = Math.floor(Math.random() * 3) + 1; // 수량 1~3
        const price = Math.floor(Math.random() * 500000) + 50000; // 가격 50,000~550,000
        
        const itemInsert = `INSERT INTO order_item (id, order_id, product_id, quantity, price, created_at, updated_at) VALUES (${orderItemId}, ${orderId}, ${productId}, ${quantity}, ${price}, '${orderDateStr}', '${orderDateStr}');`;
        inserts.push(itemInsert);
        
        totalAmount += price * quantity;
        orderItemId++;
      }
      
      // 주문 총액 업데이트
      const updateOrder = `UPDATE \`order\` SET total_amount = ${totalAmount} WHERE id = ${orderId};`;
      inserts.push(updateOrder);
      
      orderId++;
    }
  });
  
  return inserts;
}

function generateCouponInserts() {
  console.log('=== 쿠폰 INSERT 문 생성 ===');
  const inserts = [];
  
  const couponTypes = ['PERCENTAGE', 'FIXED_AMOUNT'];
  const couponNames = ['신규가입 쿠폰', '생일 축하 쿠폰', '첫 구매 쿠폰', '할인 쿠폰', '무료배송 쿠폰'];
  
  for (let i = 1; i <= 50; i++) {
    const couponType = couponTypes[Math.floor(Math.random() * couponTypes.length)];
    const couponName = couponNames[Math.floor(Math.random() * couponNames.length)];
    
    let discountValue;
    if (couponType === 'PERCENTAGE') {
      discountValue = Math.floor(Math.random() * 30) + 10; // 10~40%
    } else {
      discountValue = Math.floor(Math.random() * 50000) + 10000; // 10,000~60,000원
    }
    
    const insert = `INSERT INTO coupon (id, name, type, discount_value, created_at, updated_at) VALUES (${i}, '${couponName} ${i}', '${couponType}', ${discountValue}, NOW(), NOW());`;
    inserts.push(insert);
  }
  
  return inserts;
}

function generatePointInserts() {
  console.log('=== 포인트 INSERT 문 생성 ===');
  const inserts = [];
  
  let pointId = 1;
  
  users.forEach((user, userIndex) => {
    const userId = userIndex + 1;
    const pointCount = Math.floor(Math.random() * 5) + 1; // 1~5개
    
    for (let i = 0; i < pointCount; i++) {
      const pointTypes = ['EARN', 'USE', 'EXPIRE'];
      const randomType = pointTypes[Math.floor(Math.random() * pointTypes.length)];
      
      let amount;
      if (randomType === 'EARN') {
        amount = Math.floor(Math.random() * 10000) + 1000; // 1,000~11,000
      } else if (randomType === 'USE') {
        amount = -(Math.floor(Math.random() * 5000) + 500); // -500~-5,500
      } else {
        amount = -(Math.floor(Math.random() * 2000) + 100); // -100~-2,100
      }
      
      const pointDate = new Date();
      pointDate.setDate(pointDate.getDate() - Math.floor(Math.random() * 365));
      const pointDateStr = pointDate.toISOString().slice(0, 19).replace('T', ' ');
      
      const insert = `INSERT INTO point (id, user_id, type, amount, created_at, updated_at) VALUES (${pointId}, ${userId}, '${randomType}', ${amount}, '${pointDateStr}', '${pointDateStr}');`;
      inserts.push(insert);
      
      pointId++;
    }
  });
  
  return inserts;
}

// 7. 모든 INSERT 문 생성
console.log('SQL INSERT 문을 생성하고 있습니다...\n');

const allInserts = [
  ...generateUserInserts(),
  ...generateBrandInserts(),
  ...generateCategoryInserts(),
  ...generateProductInserts(),
  ...generateOrderInserts(),
  ...generateCouponInserts(),
  ...generatePointInserts()
];

// 8. SQL 파일로 저장
const outputPath = path.join(__dirname, 'generated_inserts.sql');
const sqlContent = allInserts.join('\n');

fs.writeFileSync(outputPath, sqlContent, 'utf8');

console.log(`\n✅ SQL INSERT 문 생성 완료!`);
console.log(`📁 저장 위치: ${outputPath}`);
console.log(`📊 총 INSERT 문 개수: ${allInserts.length}개`);
console.log(`\n생성된 테이블:`);
console.log(`- user: ${users.length}개`);
console.log(`- brand: ${brands.length}개`);
console.log(`- category: ${categories.length}개`);
console.log(`- product: ${brands.length * 20}개`);
console.log(`- order: ${users.reduce((sum, user, index) => sum + (Math.floor(Math.random() * 3) + 1), 0)}개`);
console.log(`- coupon: 50개`);
console.log(`- point: ${users.length * 3}개`);

console.log('\n🎉 모든 작업이 완료되었습니다!');
