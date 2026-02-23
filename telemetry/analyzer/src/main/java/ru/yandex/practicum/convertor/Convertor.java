package ru.yandex.practicum.convertor;

import com.google.protobuf.Timestamp;
import lombok.experimental.UtilityClass;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.Sensor;

import java.time.Instant;


@UtilityClass
public class Convertor {

    public DeviceActionRequest mapToDeviceActionRequest(Action action) {
        return DeviceActionRequest.newBuilder()
                .setHubId(action.getScenario().getHubId())
                .setScenarioName(action.getScenario().getName())
                .setAction(DeviceActionProto.newBuilder()
                        .setSensorId(action.getSensor().getId())
                        .setType(mapActionType(action.getType()))
                        .setValue(action.getValue()))
                .setTimestamp(setTimestamp())
                .build();
    }

    public Sensor mapToSensor(HubEventAvro eventAvro) {
        DeviceAddedEventAvro deviceAddedEventAvro = (DeviceAddedEventAvro) eventAvro.getPayload();
        return Sensor.builder()
                .id(deviceAddedEventAvro.getId())
                .hubId(eventAvro.getHubId())
                .build();
    }

    public Scenario mapToScenario(HubEventAvro eventAvro) {
        ScenarioAddedEventAvro scenarioAddedEventAvro = (ScenarioAddedEventAvro) eventAvro.getPayload();

        return Scenario.builder()
                .name(scenarioAddedEventAvro.getName())
                .hubId(eventAvro.getHubId())
                .build();
    }


    private ActionTypeProto mapActionType(ActionTypeAvro actionType) {
        return switch (actionType) {
            case ACTIVATE -> ActionTypeProto.ACTIVATE;
            case DEACTIVATE -> ActionTypeProto.DEACTIVATE;
            case INVERSE -> ActionTypeProto.INVERSE;
            case SET_VALUE -> ActionTypeProto.SET_VALUE;
        };
    }

    private Timestamp setTimestamp() {
        Instant instant = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

}
