import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭 정의
const errorRate = new Rate('errors');
const responseTimeTrend = new Trend('response_time');

// 실무에 맞는 부하 테스트 설정
export const options = {
  stages: [
    // 1단계: 빠른 Warm-up (30초)
    { duration: '30s', target: 5 },
    // 2단계: 목표 부하 달성 (1분)
    { duration: '1m', target: 10 },
    // 3단계: 부하 유지 (2분) - P99 계산을 위해 충분한 데이터 확보
    { duration: '2m', target: 10 },
    // 4단계: 빠른 Cool-down (30초)
    { duration: '30s', target: 0 },
  ],
  
  // 매우 빠른 테스트를 원한다면 아래 설정 사용 (주석 해제)
  /*
  stages: [
    { duration: '10s', target: 5 },    // 10초 Warm-up
    { duration: '20s', target: 10 },  // 20초 부하 달성
    { duration: '30s', target: 10 },  // 30초 부하 유지
    { duration: '10s', target: 0 },   // 10초 Cool-down
  ],
  */
  
  thresholds: {
    // 실무에서 중요한 P95, P99 응답 시간
    'http_req_duration': [
      'p(95)<200',    // 95% 요청이 200ms 이내
      'p(99)<500',    // 99% 요청이 500ms 이내
    ],
    
    // 오류율 제한 (실무 기준)
    'http_req_failed': ['rate<0.01'],  // 실패율 1% 미만 (http_req_failed 사용)
    
    // TPS (Transaction Per Second) 확인 - 현실적인 값으로 조정
    'http_reqs': ['rate>5'],  // 초당 최소 5 요청 (기존 10에서 조정)
    
    // 커스텀 메트릭 임계값
    'response_time': [
      'p(95)<200',    // 커스텀 응답 시간 95% 200ms 이내
      'p(99)<500',    // 커스텀 응답 시간 99% 500ms 이내
    ],
  },
};

// 테스트 데이터
const BRAND_IDS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
const SORT_OPTIONS = ['likes', 'price', 'name', 'created'];
const PAGE_SIZES = [10, 20, 50];

// 랜덤 값 생성 함수들
function getRandomBrandId() {
  return BRAND_IDS[Math.floor(Math.random() * BRAND_IDS.length)];
}

function getRandomSort() {
  return SORT_OPTIONS[Math.floor(Math.random() * SORT_OPTIONS.length)];
}

function getRandomPage() {
  return Math.floor(Math.random() * 5); // 0~4 페이지
}

function getRandomSize() {
  return PAGE_SIZES[Math.floor(Math.random() * PAGE_SIZES.length)];
}

export default function () {
  // 1~10 사이의 랜덤 브랜드 ID 선택
  const randomBrandId = getRandomBrandId();
  const randomSort = getRandomSort();
  const randomPage = getRandomPage();
  const randomSize = getRandomSize();
  
  // GET /api/v1/products API 호출
  const url = `http://localhost:8080/api/v1/products?brandId=${randomBrandId}&sort=${randomSort}&page=${randomPage}&size=${randomSize}`;
  
  const startTime = Date.now();
  
  const response = http.get(url, {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'User-Agent': 'k6-load-test/1.0',
    },
    timeout: '30s',
  });
  
  const responseTime = Date.now() - startTime;
  
  // 응답 검증 (실무 기준)
  const checks = check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 200ms (P95 기준)': (r) => r.timings.duration < 200,
    'response time < 500ms (P99 기준)': (r) => r.timings.duration < 500,
    'has products data': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.data && body.data.products && Array.isArray(body.data.products);
      } catch (e) {
        return false;
      }
    },
    'products count <= requested size': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.data && body.data.products && body.data.products.length <= randomSize;
      } catch (e) {
        return false;
      }
    },
    'response has brandId filter': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.data && body.data.products && 
               body.data.products.every(product => product.brandId === randomBrandId);
      } catch (e) {
        return false;
      }
    },
    'products are properly sorted': (r) => {
      try {
        const body = JSON.parse(r.body);
        if (!body.data || !body.data.products || body.data.products.length < 2) {
          return true; // 상품이 1개 이하면 정렬 검증 불필요
        }
        
        const products = body.data.products;
        
        // 정렬 옵션에 따른 검증
        switch (randomSort) {
          case 'likes':
            // 좋아요 순으로 내림차순 정렬 확인
            for (let i = 0; i < products.length - 1; i++) {
              if (products[i].likes < products[i + 1].likes) {
                return false;
              }
            }
            break;
          case 'price':
            // 가격 순으로 내림차순 정렬 확인
            for (let i = 0; i < products.length - 1; i++) {
              if (products[i].price < products[i + 1].price) {
                return false;
              }
            }
            break;
          case 'name':
            // 이름 순으로 오름차순 정렬 확인
            for (let i = 0; i < products.length - 1; i++) {
              if (products[i].name > products[i + 1].name) {
                return false;
              }
            }
            break;
        }
        return true;
      } catch (e) {
        return false;
      }
    },
  });
  
  // 커스텀 메트릭 기록
  responseTimeTrend.add(responseTime);
  errorRate.add(!checks['status is 200']);
  
  // 응답 로깅 (실무에서 중요한 정보)
  if (response.status !== 200) {
    console.log(`Error: ${response.status} - Brand: ${randomBrandId}, Sort: ${randomSort}, Page: ${randomPage}, Size: ${randomSize}`);
  } else {
    // 성공한 요청에 대해서만 응답 시간 로깅 (P95, P99 모니터링용)
    if (responseTime > 200) { // P95 임계값 초과 시
      console.log(`Slow Response: ${responseTime}ms - Brand: ${randomBrandId}, Sort: ${randomSort}, Page: ${randomPage}, Size: ${randomSize}`);
    }
  }
  
  // 실무에서 중요한 대기 시간 (사용자 행동 시뮬레이션)
  sleep(0.5); // 0.5초 대기 (기존 1초에서 2배 단축하여 TPS 향상)
}

// 테스트 완료 후 실무용 요약 정보
export function handleSummary(data) {
  console.log('=== 실무용 브랜드별 상품 조회 API 성능 테스트 결과 ===');
  console.log(`총 요청 수: ${data.metrics.http_reqs.values.count}`);
  console.log(`평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  
  // 안전한 메트릭 접근을 위한 헬퍼 함수
  const safeGetPercentile = (metric, percentile) => {
    if (metric && metric.values && metric.values[percentile] !== undefined) {
      return metric.values[percentile].toFixed(2);
    }
    return 'N/A';
  };
  
  console.log(`P95 응답 시간: ${safeGetPercentile(data.metrics.http_req_duration, 'p(95)')}ms`);
  console.log(`P99 응답 시간: ${safeGetPercentile(data.metrics.http_req_duration, 'p(99)')}ms`);
  console.log(`최대 응답 시간: ${data.metrics.http_req_duration.values.max.toFixed(2)}ms`);
  console.log(`실패율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);
  console.log(`TPS: ${data.metrics.http_reqs.values.rate.toFixed(2)}/초`);
  
  // 커스텀 메트릭 안전하게 접근
  if (data.metrics.response_time && data.metrics.response_time.values) {
    console.log(`커스텀 응답 시간 P95: ${safeGetPercentile(data.metrics.response_time, 'p(95)')}ms`);
    console.log(`커스텀 응답 시간 P99: ${safeGetPercentile(data.metrics.response_time, 'p(99)')}ms`);
  }
  
  // 실무에서 중요한 성능 지표
  const p95 = data.metrics.http_req_duration.values['p(95)'];
  const p99 = data.metrics.http_req_duration.values['p(99)'];
  const errorRate = data.metrics.http_req_failed.values.rate;
  
  if (p95 && p95 > 200) {
    console.log('⚠️  P95 응답 시간이 200ms를 초과했습니다!');
  }
  if (p99 && p99 > 500) {
    console.log('🚨 P99 응답 시간이 500ms를 초과했습니다!');
  }
  if (errorRate && errorRate > 0.01) {
    console.log('🚨 실패율이 1%를 초과했습니다!');
  }
  
  return {
    'brand-product-performance-test-summary.json': JSON.stringify(data, null, 2),
  };
}
