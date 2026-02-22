package ru.yandex.practicum.handlers.snapshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.client.ScenarioActionProducer;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ActionRepository;
import ru.yandex.practicum.repository.ConditionRepository;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotHandler {
    private final ConditionRepository conditionRepository;
    private final ScenarioRepository scenarioRepository;
    private final ActionRepository actionRepository;
    private final ScenarioActionProducer scenarioActionProducer;

    public void handleSnapshot(SensorsSnapshotAvro snapshotAvro) {
        Map<String, SensorStateAvro> sensorStateAvroMap = snapshotAvro.getSensorsState();
        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshotAvro.getHubId());
        scenarios.stream()
                .filter(scenario -> handleScenario(scenario, sensorStateAvroMap))
                .forEach(scenario -> sendScenarioActions(scenario));
    }

    private Boolean handleScenario(Scenario scenario, Map<String, SensorStateAvro> sensorStateAvroMap) {
        List<Condition> conditions = conditionRepository.findAllByScenario(scenario);
        return conditions.stream().noneMatch(condition -> !handleCondition(condition, sensorStateAvroMap));
    }

    private Boolean handleCondition(Condition condition, Map<String, SensorStateAvro> sensorStateAvroMap) {
        String sensorId = condition.getSensor().getId().toString();
        SensorStateAvro sensorStateAvro = sensorStateAvroMap.get(sensorId);
        if (sensorStateAvro == null) {
            return false;
        }

        switch (condition.getType()) {
            case LUMINOSITY -> {
                LightSensorAvro lightSensorAvro = (LightSensorAvro) sensorStateAvro.getData();
                return handleOperation(condition, lightSensorAvro.getLuminosity());
            }
            case TEMPERATURE -> {
                ClimateSensorAvro climateSensorAvro = (ClimateSensorAvro) sensorStateAvro.getData();
                return handleOperation(condition, climateSensorAvro.getTemperatureC());
            }
            case MOTION -> {
                MotionSensorAvro motionSensorAvro = (MotionSensorAvro) sensorStateAvro.getData();
                return handleOperation(condition, motionSensorAvro.getMotion() ? 1 : 0);
            }
            case SWITCH -> {
                SwitchSensorAvro switchSensorAvro = (SwitchSensorAvro) sensorStateAvro.getData();
                return handleOperation(condition, switchSensorAvro.getState() ? 1 : 0);
            }
            case CO2LEVEL -> {
                ClimateSensorAvro climateSensorAvro = (ClimateSensorAvro) sensorStateAvro.getData();
                return handleOperation(condition, climateSensorAvro.getCo2Level());
            }
            case HUMIDITY -> {
                ClimateSensorAvro climateSensorAvro = (ClimateSensorAvro) sensorStateAvro.getData();
                return handleOperation(condition, climateSensorAvro.getHumidity());
            }
            case null -> {
                return false;
            }
        }
    }

    private Boolean handleOperation(Condition condition, Integer currentValue) {
        ConditionOperationAvro operationAvro = condition.getOperation();
        Integer targetValue = condition.getValue();
        switch (operationAvro) {
            case EQUALS -> {
                return currentValue == targetValue;
            }
            case LOWER_THAN -> {
                return currentValue < targetValue;
            }
            case GREATER_THAN -> {
                return currentValue > targetValue;
            }
            case null -> {
                return false;
            }
        }
    }

    private void sendScenarioActions(Scenario scenario) {
        actionRepository.findAllByScenario(scenario).forEach(scenarioActionProducer::send);
    }
}
