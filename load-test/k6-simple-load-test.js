import http from 'k6/http';
import { check, sleep } from 'k6';

// 부하 테스트 설정 - P99, P99.9 중점
export const options = {
  vus: 100,        // 가상 사용자 100명
  duration: '5m',  // 5분간 테스트
  
  thresholds: {
    // P99와 P99.9가 가장 중요!
    http_req_duration: [
      'p(99)<500',    // 99% 요청이 500ms 이내
      'p(99.9)<800',  // 99.9% 요청이 800ms 이내
    ],
    
    // 오류율 제한
    http_req_failed: ['rate<0.02'],  // 실패율 2% 미만
    
    // TPS (Transaction Per Second) 확인
    http_reqs: ['rate>80'],          // 초당 최소 80 요청
    
    // Duration 관련 임계값 (가장 중요!)
    http_req_waiting: [
      'p(99)<500',    // 99% 요청의 waiting 시간이 500ms 이내
      'p(99.9)<700',  // 99.9% 요청의 waiting 시간이 700ms 이내
    ],
    
    // 전체 요청 지속 시간
    http_req_duration: [
      'p(99)<300',    // 99% 요청이 300ms 이내
      'p(99.9)<500',  // 99.9% 요청이 500ms 이내
    ],
  },
};

export default function () {
  // 브랜드별 상품 조회 + 다양한 정렬 옵션
  const brandId = Math.floor(Math.random() * 10) + 1;
  const page = Math.floor(Math.random() * 5);
  const sortOptions = ['likes', 'price', 'name', 'created'];
  const sort = sortOptions[Math.floor(Math.random() * sortOptions.length)];
  
  const url = `http://localhost:8080/api/v1/products?brandId=${brandId}&sort=${sort}&page=${page}&size=20`;
  
  const response = http.get(url);
  
  // 응답 검증 - P99, P99.9 중점
  check(response, {
    'status is 200': (r) => r.status === 200,
    
    // Duration 체크
    'response time < 300ms (P99 기준)': (r) => r.timings.duration < 300,
    'response time < 500ms (P99.9 기준)': (r) => r.timings.duration < 500,
    
    // Waiting 시간 체크 (서버 처리 시간)
    'waiting time < 250ms (P99 기준)': (r) => r.timings.waiting < 250,
    'waiting time < 400ms (P99.9 기준)': (r) => r.timings.waiting < 400,
    
    // 전체 요청 시간 체크
    'total duration < 300ms (P99 기준)': (r) => r.timings.duration < 300,
    'total duration < 500ms (P99.9 기준)': (r) => r.timings.duration < 500,
  });
  
  // 실제 사용자처럼 적절한 대기 시간
  sleep(0.5); // 0.5초 대기 (연속 요청 방지)
}
