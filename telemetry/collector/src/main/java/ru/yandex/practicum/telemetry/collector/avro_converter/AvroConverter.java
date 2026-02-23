package ru.yandex.practicum.telemetry.collector.avro_converter;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.exceptions.NonExistsEventException;

import java.time.Instant;

@UtilityClass
public final class AvroConverter {

    public static SensorEventAvro convertToSensorEventAvro(SensorEventProto event) {
        SensorEventAvro.Builder avro = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()));

        switch (event.getPayloadCase()) {
            case LIGHT_SENSOR_EVENT -> {
                LightSensorProto lightSensorProto = event.getLightSensorEvent();
                LightSensorAvro lightSensor = LightSensorAvro.newBuilder()
                        .setLinkQuality(lightSensorProto.getLinkQuality())
                        .setLuminosity(lightSensorProto.getLuminosity())
                        .build();
                avro.setPayload(lightSensor);
            }
            case MOTION_SENSOR_EVENT -> {
                MotionSensorProto motionSensorProto = event.getMotionSensorEvent();
                MotionSensorAvro motionSensor = MotionSensorAvro.newBuilder()
                        .setLinkQuality(motionSensorProto.getLinkQuality())
                        .setMotion(motionSensorProto.getMotion())
                        .setVoltage(motionSensorProto.getVoltage())
                        .build();
                avro.setPayload(motionSensor);
            }
            case CLIMATE_SENSOR_EVENT -> {
                ClimateSensorProto climateSensorProto = event.getClimateSensorEvent();
                ClimateSensorAvro climateSensor = ClimateSensorAvro.newBuilder()
                        .setTemperatureC(climateSensorProto.getTemperatureC())
                        .setHumidity(climateSensorProto.getHumidity())
                        .setCo2Level(climateSensorProto.getCo2Level())
                        .build();
                avro.setPayload(climateSensor);
            }
            case TEMPERATURE_SENSOR_EVENT -> {
                TemperatureSensorProto temperatureSensorProto = event.getTemperatureSensorEvent();
                TemperatureSensorAvro temperatureSensor = TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(temperatureSensorProto.getTemperatureC())
                        .setTemperatureF(temperatureSensorProto.getTemperatureF())
                        .build();
                avro.setPayload(temperatureSensor);
            }
            case SWITCH_SENSOR_EVENT -> {
                SwitchSensorProto switchSensorProto = event.getSwitchSensorEvent();
                SwitchSensorAvro switchSensor = SwitchSensorAvro.newBuilder()
                        .setState(switchSensorProto.getState())
                        .build();
                avro.setPayload(switchSensor);
            }
            default -> throw new NonExistsEventException("Ивента с типом=" + event.getPayloadCase() + " не существует");
        }
        return avro.build();
    }

    public static HubEventAvro convertToHubEventAvro(HubEventProto event) {
        HubEventAvro.Builder avro = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()));
        switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> {
                DeviceAddedEventProto deviceAddedEventProto = event.getDeviceAdded();
                DeviceAddedEventAvro addedEvent = DeviceAddedEventAvro.newBuilder()
                        .setId(deviceAddedEventProto.getId())
                        .setType(DeviceTypeAvro.valueOf(deviceAddedEventProto.getType().name()))
                        .build();
                avro.setPayload(addedEvent);
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEventProto deviceRemovedEventProto = event.getDeviceRemoved();
                DeviceRemovedEventAvro removedEvent = DeviceRemovedEventAvro.newBuilder()
                        .setId(deviceRemovedEventProto.getId())
                        .build();
                avro.setPayload(removedEvent);
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEventProto scenarioConditionProto = event.getScenarioAdded();
                ScenarioAddedEventAvro addedEvent = ScenarioAddedEventAvro.newBuilder()
                        .setName(scenarioConditionProto.getName())
                        .setConditions(scenarioConditionProto.getConditionList().stream()
                                .map(AvroConverter::convertToScenarioConditionAvro)
                                .toList())
                        .setActions(scenarioConditionProto.getActionList().stream()
                                .map(AvroConverter::convertToDeviceActionAvro)
                                .toList())
                        .build();
                avro.setPayload(addedEvent);
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEventProto scenarioRemovedEventProto = event.getScenarioRemoved();
                ScenarioRemovedEventAvro removedEvent = ScenarioRemovedEventAvro.newBuilder()
                        .setName(scenarioRemovedEventProto.getName())
                        .build();
                avro.setPayload(removedEvent);
            }
            default -> throw new NonExistsEventException("Ивент хаба " + event.getPayloadCase() + " не существует");
        }
        return avro.build();
    }

    private static ScenarioConditionAvro convertToScenarioConditionAvro(ScenarioConditionProto condition) {
        Object value = switch (condition.getValueCase()) {
            case BOOL_VALUE -> condition.getBoolValue();
            case INT_VALUE -> condition.getIntValue();
            case VALUE_NOT_SET -> throw new IllegalArgumentException("Условие. Значение не задано.");
        };

        ScenarioConditionAvro.Builder avro = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(value);
        return avro.build();
    }

    private static DeviceActionAvro convertToDeviceActionAvro(DeviceActionProto action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}
