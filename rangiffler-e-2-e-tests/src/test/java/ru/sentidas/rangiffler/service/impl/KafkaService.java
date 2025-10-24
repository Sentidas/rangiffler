package ru.sentidas.rangiffler.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.consumer.*;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.model.kafka.ActivityEvent;
import ru.sentidas.rangiffler.model.kafka.PhotoStatEvent;
import ru.sentidas.rangiffler.model.kafka.UserEvent;
import ru.sentidas.rangiffler.utils.MapWithWait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaService implements Runnable {

    private static final Config CFG = Config.getInstance();
    private static final AtomicBoolean isRun = new AtomicBoolean(false);
    private static final Properties properties = new Properties();
    private static final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final MapWithWait<String, UserEvent> userStore = new MapWithWait<>();
    private static final MapWithWait<String, PhotoStatEvent> photoStore = new MapWithWait<>();
    private static final MapWithWait<UUID, ActivityEvent> activityStore = new MapWithWait<>();

    private final List<String> topics;
    private final Consumer<String, String> consumer;

    static {
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, CFG.kafkaAddress());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    }

    public KafkaService() {
        this(CFG.kafkaTopics());
    }

    public KafkaService(List<String> topics) {
        this.topics = topics;
        this.consumer = new KafkaConsumer<>(properties);
    }

    public static UserEvent getUser(String username) throws InterruptedException {
        return userStore.get(username, 10_000L);
    }

    public static PhotoStatEvent getPhotoStat(UUID userId, String countryCode) throws InterruptedException {
        return photoStore.get(userId + ":" + countryCode, 15_000L);
    }

    public static ActivityEvent getActivity(UUID eventId) throws InterruptedException {
        return activityStore.get(eventId, 10_000L);
    }

    public static PhotoStatEvent waitPhotoStat(UUID userId, String country, long timeoutMs,
                                               java.util.function.Predicate<PhotoStatEvent> p)
            throws InterruptedException {
        return photoStore.waitFor(userId + ":" + country, timeoutMs, p);
    }

    @Override
    public void run() {
        isRun.set(true);
        try {
            consumer.subscribe(topics);
            while (isRun.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(200, ChronoUnit.MILLIS));
                for (ConsumerRecord<String, String> record : records) {
                    final String topic = record.topic();
                    final String json = record.value();

                    switch (topic) {
                        case "rangiffler_user": {
                            UserEvent e = om.readValue(json, UserEvent.class);
                            userStore.put(e.username(), e);
                            break;
                        }
                        case "rangiffler_photo": {
                            PhotoStatEvent e = om.readValue(json, PhotoStatEvent.class);
                            photoStore.put(e.userId() + ":" + e.countryCode(), e);
                            break;
                        }
                        case "rangiffler.activity": {
                            ActivityEvent e = om.readValue(json, ActivityEvent.class);
                            activityStore.put(e.eventId(), e);
                            break;
                        }
                        default:
                            // незнакомый топик
                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            consumer.close();
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        isRun.set(false);
    }
}