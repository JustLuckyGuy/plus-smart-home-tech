package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Table(name = "conditions")
@SecondaryTable(name = "scenario_conditions", pkJoinColumns = @PrimaryKeyJoinColumn(name = "condition_id"))
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ConditionTypeAvro type;

    @Enumerated(EnumType.STRING)
    private ConditionOperationAvro operation;

    @Column(name = "value")
    private Integer value;

    @ManyToOne
    @JoinColumn(name = "scenario_id", table = "scenario_conditions")
    private Scenario scenario;

    @ManyToOne
    @JoinColumn(name = "sensor_id", table = "scenario_conditions")
    private Sensor sensor;
}
