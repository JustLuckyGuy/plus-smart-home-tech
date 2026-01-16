package ru.yandex.practicum.telemetry.collector.avro_converter;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.exceptions.NonExistsEventException;
import ru.yandex.practicum.telemetry.collector.models.hubs.*;
import ru.yandex.practicum.telemetry.collector.models.sensors.*;

@UtilityClass
public final class AvroConverter {

    public static SensorEventAvro convertToSensorEventAvro(SensorEvent event) {
        SensorEventAvro.Builder avro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        switch (event) {
            case LightSensorEvent lightEvent -> {
                LightSensorAvro lightSensor = LightSensorAvro.newBuilder()
                        .setLinkQuality(lightEvent.getLinkQuality())
                        .setLuminosity(lightEvent.getLuminosity())
                        .build();
                avro.setPayload(lightSensor);
            }
            case MotionSensorEvent motionSensorEvent -> {
                MotionSensorAvro motionSensor = MotionSensorAvro.newBuilder()
                        .setLinkQuality(motionSensorEvent.getLinkQuality())
                        .setMotion(motionSensorEvent.getMotion())
                        .setVoltage(motionSensorEvent.getVoltage())
                        .build();
                avro.setPayload(motionSensor);
            }
            case ClimateSensorEvent climateSensorEvent -> {
                ClimateSensorAvro climateSensor = ClimateSensorAvro.newBuilder()
                        .setTemperatureC(climateSensorEvent.getTemperatureC())
                        .setHumidity(climateSensorEvent.getHumidity())
                        .setCo2Level(climateSensorEvent.getCo2Level())
                        .build();
                avro.setPayload(climateSensor);
            }
            case TemperatureSensorEvent temperatureSensorEvent -> {
                TemperatureSensorAvro temperatureSensor = TemperatureSensorAvro.newBuilder()
                        .setId(temperatureSensorEvent.getId())
                        .setHubId(temperatureSensorEvent.getHubId())
                        .setTimestamp(temperatureSensorEvent.getTimestamp())
                        .setTemperatureC(temperatureSensorEvent.getTemperatureC())
                        .setTemperatureF(temperatureSensorEvent.getTemperatureF())
                        .build();
                avro.setPayload(temperatureSensor);
            }
            case SwitchSensorEvent switchSensorEvent -> {
                SwitchSensorAvro switchSensor = SwitchSensorAvro.newBuilder()
                        .setState(switchSensorEvent.getState())
                        .build();
                avro.setPayload(switchSensor);
            }
            default -> throw new NonExistsEventException("Ивента с типом=" + event.getType() + " не существует");
        }
        return avro.build();
    }

    public static HubEventAvro convertToHubEventAvro(HubEvent event) {
        HubEventAvro.Builder avro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());
        switch (event) {
            case DeviceAddedEvent deviceAddedEvent -> {
                DeviceAddedEventAvro addedEvent = DeviceAddedEventAvro.newBuilder()
                        .setId(deviceAddedEvent.getId())
                        .setType(DeviceTypeAvro.valueOf(deviceAddedEvent.getDeviceType()))
                        .build();
                avro.setPayload(addedEvent);
            }
            case DeviceRemovedEvent deviceRemovedEvent -> {
                DeviceRemovedEventAvro removedEvent = DeviceRemovedEventAvro.newBuilder()
                        .setId(deviceRemovedEvent.getId())
                        .build();
                avro.setPayload(removedEvent);
            }
            case ScenarioAddedEvent scenarioAddedEvent -> {
                ScenarioAddedEventAvro addedEvent = ScenarioAddedEventAvro.newBuilder()
                        .setName(scenarioAddedEvent.getName())
                        .setConditions(scenarioAddedEvent.getConditions().stream()
                                .map(AvroConverter::convertToScenarioConditionAvro)
                                .toList())
                        .setActions(scenarioAddedEvent.getActions().stream()
                                .map(AvroConverter::convertToDeviceActionAvro)
                                .toList())
                        .build();
                avro.setPayload(addedEvent);
            }
            case ScenarioRemovedEvent scenarioRemovedEvent -> {
                ScenarioRemovedEventAvro removedEvent = ScenarioRemovedEventAvro.newBuilder()
                        .setName(scenarioRemovedEvent.getName())
                        .build();
                avro.setPayload(removedEvent);
            }
            default -> throw new NonExistsEventException("Ивент хаба " + event.getType() + " не существует");
        }
        return avro.build();
    }

    private static ScenarioConditionAvro convertToScenarioConditionAvro(ScenarioCondition condition) {
        ScenarioConditionAvro.Builder avro = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation()))
                .setValue(condition.getValue());
        return avro.build();
    }

    private static DeviceActionAvro convertToDeviceActionAvro(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType()))
                .setValue(action.getValue())
                .build();
    }
}
