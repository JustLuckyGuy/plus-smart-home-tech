package ru.yandex.practicum.handlers.event;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.repository.SensorRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeviceRemovedHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;

    @Override
    @Transactional
    public void handle(HubEventAvro event) {
        DeviceRemovedEventAvro removedEvent = (DeviceRemovedEventAvro) event.getPayload();
        log.info("Удаляем устройство с id = {} из хаба с hub_id = {}", removedEvent.getId(), event.getHubId());
        sensorRepository.deleteByIdAndHubId(removedEvent.getId(), event.getHubId());
    }

    @Override
    public String getPayloadType() {
        return DeviceRemovedEventAvro.class.getSimpleName();
    }
}
