package ru.yandex.practicum.telemetry.collector.models.sensors;

import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class SwitchSensorEvent extends SensorEvent {
    @NonNull
    private Boolean state;

}
