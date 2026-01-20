package ru.yandex.practicum.telemetry.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.avro_converter.AvroConverter;
import ru.yandex.practicum.telemetry.collector.models.hubs.HubEvent;
import ru.yandex.practicum.telemetry.collector.models.sensors.SensorEvent;
import ru.yandex.practicum.telemetry.kafka_producer.KafkaEventProducer;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorService {
    private final KafkaEventProducer kafkaEvent;

    public void sendSensorEvent(SensorEvent event) {
        try {
            SensorEventAvro avro = AvroConverter.convertToSensorEventAvro(event);
            kafkaEvent.sendSensorEvent(avro.getHubId(), avro.getTimestamp().toEpochMilli(), avro);
        } catch (Exception e) {
            log.error("Error converting sensor event to Avro: {}", event, e);
        }
    }

    public void sendHubEvent(HubEvent event) {
        try {
            HubEventAvro avro = AvroConverter.convertToHubEventAvro(event);
            kafkaEvent.sendHubEvent(avro.getHubId(), avro.getTimestamp().toEpochMilli(), avro);
        } catch (Exception e) {
            log.error("Error converting hub event to Avro: {}", event, e);
        }
    }
}
