package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Table(name = "warehouse_products", schema = "warehouse")
public class WarehouseProduct {
    @Id
    @Column(name = "product_id")
    private UUID productId;
    private Boolean fragile;
    private Double width;
    private Double height;
    private Double depth;
    private Double weight;
    private Integer quantity = 0;
}
