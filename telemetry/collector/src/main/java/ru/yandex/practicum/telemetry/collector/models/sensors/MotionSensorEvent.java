package ru.yandex.practicum.telemetry.collector.models.sensors;

import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class MotionSensorEvent extends SensorEvent {
    @NonNull
    private Integer linkQuality;

    @NonNull
    private Boolean motion;

    @NonNull
    private Integer voltage;

}
