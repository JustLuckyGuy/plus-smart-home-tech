package ru.yandex.practicum.telemetry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.yandex.practicum.telemetry.kafka_producer," +
        "ru.yandex.practicum.telemetry.collector," +
        "main.avro.serializator")
public class TelemetryApp {
    public static void main(String[] args) {
        SpringApplication.run(TelemetryApp.class, args);
    }
}
