package com.loopers.domain.eventlog;

import com.loopers.domain.eventlog.EventLogEntity;
import com.loopers.domain.eventlog.EventLogRepository;
import com.loopers.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventLogService {

    private final EventLogRepository eventLogRepository;

    @Transactional
    public void saveEvent(BaseEvent event) {
        try {
            // 멱등성 체크
            if (eventLogRepository.existsByEventId(event.getEventId())) {
                log.info("이벤트 이미 저장됨 - eventId: {}, eventType: {}", 
                        event.getEventId(), event.getEventType());
                return;
            }

            EventLogEntity eventLog = EventLogEntity.from(event);
            eventLogRepository.save(eventLog);

            log.info("이벤트 로그 저장 완료 - eventId: {}, eventType: {}", 
                    event.getEventId(), event.getEventType());

        } catch (Exception e) {
            log.error("이벤트 로그 저장 실패 - eventId: {}, eventType: {}, error: {}", 
                    event.getEventId(), event.getEventType(), e.getMessage(), e);
        }
    }
}
