package ru.yandex.practicum;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
public class EventDataProducer {
    private final SensorConfig sensorConfig;

    private final Map<String, Integer> lastValues = new HashMap<>();

    @GrpcClient("collector")
    private CollectorControllerGrpc.CollectorControllerBlockingStub collectorStub;

    @Autowired
    public EventDataProducer(SensorConfig sensorConfig) {
        this.sensorConfig = sensorConfig;
    }

    @Scheduled(fixedRate = 1000)
    public void generateAndSendRandomEvent() {
        int sensorType = ThreadLocalRandom.current().nextInt(5);

        switch (sensorType) {
            case 0 -> {
                SensorConfig.ClimateSensor sensor = getRandomSensor(sensorConfig.getClimateSensors());
                log.debug("Создание события от климатического датчика {}", sensor);
                if (sensor != null) {
                    sendEvent(createClimateSensorEvent(sensor));
                }
            }
            case 1 -> {
                SensorConfig.LightSensor sensor = getRandomSensor(sensorConfig.getLightSensors());
                log.debug("Создание события от датчика освещенности {}", sensor);
                if (sensor != null) {
                    sendEvent(createLightSensorEvent(sensor));
                }
            }
            case 2 -> {
                SensorConfig.MotionSensor sensor = getRandomSensor(sensorConfig.getMotionSensors());
                log.debug("Создание события от датчика движения {}", sensor);
                if (sensor != null) {
                    sendEvent(createMotionSensorEvent(sensor));
                }
            }
            case 3 -> {
                SensorConfig.SwitchSensor sensor = getRandomSensor(sensorConfig.getSwitchSensors());
                log.debug("Создание события от датчика выключателя {}", sensor);
                if (sensor != null) {
                    sendEvent(createSwitchSensorEvent(sensor));
                }
            }
            case 4 -> {
                SensorConfig.TemperatureSensor sensor = getRandomSensor(sensorConfig.getTemperatureSensors());
                log.debug("Создание события от датчика температуры {}", sensor);
                if (sensor != null) {
                    sendEvent(createTemperatureSensorEvent(sensor));
                }
            }
            default -> throw new IllegalArgumentException("Сгенерировали датчик неизвестного тип: " + sensorType);
        }
    }

    private void sendEvent(SensorEventProto event) {
        log.info("Отправляем данные: {}", event.getAllFields());
        collectorStub.collectSensorEvent(event);
    }

    private int generateValueWithDeviation(SensorConfig.Range range, String sensorId) {
        int lastValue = lastValues.getOrDefault(sensorId, range.getMinValue());
        int deviation = ThreadLocalRandom.current().nextInt(-1, 2);
        int newValue = lastValue + deviation;

        newValue = Math.min(range.getMaxValue(), newValue);
        newValue = Math.max(range.getMinValue(), newValue);
        lastValues.put(sensorId, newValue);
        return newValue;
    }

    private <T> T getRandomSensor(List<T> sensors) {
        if (sensors.isEmpty()) {
            return null;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(sensors.size());
        return sensors.get(randomIndex);
    }

    private SensorEventProto createTemperatureSensorEvent(SensorConfig.TemperatureSensor sensor) {
        int temperatureCelsius = generateValueWithDeviation(sensor.getTemperature(), sensor.getId());
        int temperatureFahrenheit = (int) (temperatureCelsius * 1.8 + 32);
        Instant now = Instant.now();

        return SensorEventProto.newBuilder()
                .setId(sensor.getId())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .setTemperatureSensorEvent(TemperatureSensorProto.newBuilder()
                        .setTemperatureC(temperatureCelsius)
                        .setTemperatureF(temperatureFahrenheit)
                        .build())
                .build();
    }

    private SensorEventProto createLightSensorEvent(SensorConfig.LightSensor sensor) {
        int luminosity = generateValueWithDeviation(sensor.getLuminosity(), sensor.getId());
        Instant now = Instant.now();

        return SensorEventProto.newBuilder()
                .setId(sensor.getId())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .setLightSensorEvent(LightSensorProto.newBuilder()
                        .setLuminosity(luminosity)
                        .build())
                .build();
    }

    private SensorEventProto createSwitchSensorEvent(SensorConfig.SwitchSensor sensor) {
        boolean state = ThreadLocalRandom.current().nextBoolean();
        Instant now = Instant.now();

        return SensorEventProto.newBuilder()
                .setId(sensor.getId())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .setSwitchSensorEvent(SwitchSensorProto.newBuilder()
                        .setState(state)
                        .build())
                .build();
    }

    private SensorEventProto createMotionSensorEvent(SensorConfig.MotionSensor sensor) {
        int linkQuality = generateValueWithDeviation(sensor.getLinkQuality(), sensor.getId() + "-linkQuality");
        int voltage = generateValueWithDeviation(sensor.getVoltage(), sensor.getId() + "-voltage");
        boolean state = ThreadLocalRandom.current().nextBoolean();
        Instant now = Instant.now();

        return SensorEventProto.newBuilder()
                .setId(sensor.getId())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .setMotionSensorEvent(MotionSensorProto.newBuilder()
                        .setLinkQuality(linkQuality)
                        .setVoltage(voltage)
                        .setMotion(state)
                        .build())
                .build();
    }

    private SensorEventProto createClimateSensorEvent(SensorConfig.ClimateSensor sensor) {
        int temperature = generateValueWithDeviation(sensor.getTemperature(), sensor.getId() + "-temperature");
        int humidity = generateValueWithDeviation(sensor.getHumidity(), sensor.getId() + "-humidity");
        int co2Level = generateValueWithDeviation(sensor.getCo2Level(), sensor.getId() + "-co2Level");
        Instant now = Instant.now();

        return SensorEventProto.newBuilder()
                .setId(sensor.getId())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                        .build())
                .setClimateSensorEvent(ClimateSensorProto.newBuilder()
                        .setTemperatureC(temperature)
                        .setHumidity(humidity)
                        .setCo2Level(co2Level)
                        .build())
                .build();
    }


}
