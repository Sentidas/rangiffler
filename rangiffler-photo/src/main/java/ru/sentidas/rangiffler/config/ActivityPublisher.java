package ru.sentidas.rangiffler.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.sentidas.rangiffler.ActivityEvent;
import ru.sentidas.rangiffler.ActivityHeaders;
import ru.sentidas.rangiffler.MessageKeyStrategy;

@Component
@RequiredArgsConstructor
public class ActivityPublisher {

    private final KafkaTemplate<String, ActivityEvent> template;
    @Value("${app.activity-topic:rangiffler.activity}")
    private String topic;

    @Value("${app.events-version:v1}")
    private String version;

    public void publish(ActivityEvent e) {
        Message<ActivityEvent> msg = MessageBuilder.withPayload(e)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, MessageKeyStrategy.auditSmartKey(e))
                .setHeader(ActivityHeaders.EVENT_TYPE, e.eventType().name())
                .setHeader(ActivityHeaders.EVENT_VERSION, version)
                .setHeader(ActivityHeaders.TRACE_ID, getOrCreateTraceId())
                .build();

        template.send(msg);
    }

    private String getOrCreateTraceId() {
        String fromMdc = org.slf4j.MDC.get(ActivityHeaders.TRACE_ID);
        return fromMdc != null ? fromMdc : java.util.UUID.randomUUID().toString();
    }
}
