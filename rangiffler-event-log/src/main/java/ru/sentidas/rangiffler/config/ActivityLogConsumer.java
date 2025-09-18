package ru.sentidas.rangiffler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.data.entity.LogEventEntity;
import ru.sentidas.rangiffler.data.repository.LogEventRepository;

import ru.sentidas.rangiffler.events.ActivityEvent;
import ru.sentidas.rangiffler.events.ActivityHeaders;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogConsumer {

    private final LogEventRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(
            topics = "${app.activity-topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "3"
    )
    @Transactional
    public void onEvent(ActivityEvent e,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                        @Header(KafkaHeaders.OFFSET) Long offset,
                        @Header(value = ActivityHeaders.TRACE_ID, required = false) String traceId,
                        @Header(value = ActivityHeaders.EVENT_VERSION, required = false) String ver) {

        if (repo.existsByEventId(e.eventId())) {
            log.debug("duplicate eventId={}, skip", e.eventId());
            return;
        }

        ObjectNode payloadNode = mapper.valueToTree(e.payload() == null ? java.util.Map.of() : e.payload());

        var le = LogEventEntity.builder()
                .eventId(e.eventId())
                .eventType(e.eventType() != null ? e.eventType().name() : "UNKNOWN")
                .userId(e.userId())
                .photoId(e.photoId())
                .targetUserId(e.targetUserId())
                .sourceService(e.sourceService())
                .occurredAt(e.occurredAt())
                .receivedAt(Instant.now())
                .traceId(traceId)
                .payload(payloadNode)
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .build();

        repo.save(le);
        log.info("logged {} (v:{}), user={}, photo={}, target={}, topic={}, part={}, offset={}",
                le.getEventType(), ver, le.getUserId(), le.getPhotoId(), le.getTargetUserId(),
                topic, partition, offset);
    }
}
