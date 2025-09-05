package com.loopers.application.eventlog;

import com.loopers.domain.eventlog.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventLogFacade {

    private final EventLogService eventLogService;

    @Transactional
    public EventLogResult processEventLog(EventLogCriteria criteria) {
        try {
            log.info("이벤트 로그 처리 시작 - eventId: {}, eventType: {}, topic: {}, partition: {}, offset: {}",
                    criteria.getEvent().getEventId(), criteria.getEvent().getEventType(), 
                    criteria.getTopic(), criteria.getPartition(), criteria.getOffset());

            eventLogService.saveEvent(criteria.getEvent());

            log.info("이벤트 로그 처리 완료 - eventId: {}, eventType: {}",
                    criteria.getEvent().getEventId(), criteria.getEvent().getEventType());

            return EventLogResult.success(
                    criteria.getEvent().getEventId(),
                    criteria.getEvent().getEventType(),
                    "이벤트 로그 처리 완료"
            );

        } catch (Exception e) {
            log.error("이벤트 로그 처리 실패 - eventId: {}, eventType: {}, error: {}",
                    criteria.getEvent().getEventId(), criteria.getEvent().getEventType(), e.getMessage(), e);

            return EventLogResult.failure(
                    criteria.getEvent().getEventId(),
                    criteria.getEvent().getEventType(),
                    e.getMessage()
            );
        }
    }
}
