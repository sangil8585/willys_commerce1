package com.loopers.domain.eventlog;

import com.loopers.domain.eventlog.EventLogEntity;

import java.util.Optional;

public interface EventLogRepository {
    
    /**
     * 이벤트 로그 저장
     */
    EventLogEntity save(EventLogEntity eventLog);
    
    /**
     * eventId로 중복 체크 (멱등성 보장)
     */
    boolean existsByEventId(String eventId);
    
    /**
     * eventId로 이벤트 로그 조회
     */
    Optional<EventLogEntity> findByEventId(String eventId);
}
