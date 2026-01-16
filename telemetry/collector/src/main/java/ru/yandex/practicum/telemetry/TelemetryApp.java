package ru.yandex.practicum.telemetry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.yandex.practicum.telemetry.kafka_client," +
        "ru.yandex.practicum.telemetry.collector")
public class TelemetryApp {
    public static void main(String[] args) {
        SpringApplication.run(TelemetryApp.class, args);
    }
}
