package ru.yandex.practicum.telemetry.collector.models.sensors;

import lombok.*;


@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class ClimateSensorEvent extends SensorEvent {
    @NonNull
    private Integer temperatureC;
    @NonNull
    private Integer humidity;
    @NonNull
    private Integer co2Level;

}
