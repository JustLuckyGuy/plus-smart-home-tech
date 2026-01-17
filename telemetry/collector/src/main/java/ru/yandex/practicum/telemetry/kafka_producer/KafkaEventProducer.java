package ru.yandex.practicum.telemetry.kafka_producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaEventProducer implements AutoCloseable {
    private final KafkaProducer<String, SpecificRecordBase> producer;

    @Value("${kafka.topics.sensors}")
    private String sensorTopic;

    @Value("${kafka.topics.hubs}")
    private String hubTopic;

    public void sendSensorEvent(String hubId, Long timestamp, SpecificRecordBase event) {
        sendEvent(sensorTopic, hubId, timestamp, event, "SensorEvent");
    }

    public void sendHubEvent(String hubId, Long timestamp, SpecificRecordBase event) {
        sendEvent(hubTopic, hubId, timestamp, event, "HubEvent");
    }

    private void sendEvent(String topic, String hubId, Long timestamp, SpecificRecordBase event, String eventClass) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic,
                null,
                timestamp,
                hubId,
                event
        );


        Future<RecordMetadata> futureResult = producer.send(record);
        producer.flush();
        try {
            RecordMetadata metadata = futureResult.get();
            log.info("Событие {} было успешно сохранено в топик {} в партицию {} со смещением {}",
                    eventClass, metadata.topic(), metadata.partition(), metadata.offset());
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Не удалось записать событие {} в топик {}", eventClass, topic, e);
        }
    }

    @Override
    public void close() {
        producer.flush();
        producer.close(Duration.ofSeconds(10));
    }
}
