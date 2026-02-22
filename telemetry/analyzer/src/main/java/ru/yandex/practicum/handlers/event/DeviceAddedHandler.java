package ru.yandex.practicum.handlers.event;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.convertor.Convertor;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.SensorRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeviceAddedHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;

    @Override
    @Transactional
    public void handle(HubEventAvro event) {
        Sensor sensor = Convertor.mapToSensor(event);
        sensorRepository.save(sensor);
    }

    @Override
    public String getPayloadType() {
        return DeviceAddedEventAvro.class.getSimpleName();
    }
}
