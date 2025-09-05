package com.loopers.infrastructure.eventlog;

import com.loopers.domain.eventlog.EventLogEntity;
import com.loopers.domain.eventlog.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventLogRepositoryImpl implements EventLogRepository {

    private final EventLogJpaRepository jpaRepository;

    @Override
    public EventLogEntity save(EventLogEntity eventLog) {
        return jpaRepository.save(eventLog);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaRepository.existsByEventId(eventId);
    }

    @Override
    public Optional<EventLogEntity> findByEventId(String eventId) {
        return jpaRepository.findByEventId(eventId);
    }
    
    public interface EventLogJpaRepository extends JpaRepository<EventLogEntity, Long> {
        boolean existsByEventId(String eventId);
        Optional<EventLogEntity> findByEventId(String eventId);
    }
}
