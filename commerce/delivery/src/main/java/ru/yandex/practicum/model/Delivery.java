package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.delivery.DeliveryState;

import java.util.UUID;

@Entity
@Slf4j
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "deliveries", schema = "delivery")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_address_id")
    @ToString.Exclude
    private Address fromAddress;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "to_address_id")
    @ToString.Exclude
    private Address toAddress;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "delivery_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryState deliveryState;
}
