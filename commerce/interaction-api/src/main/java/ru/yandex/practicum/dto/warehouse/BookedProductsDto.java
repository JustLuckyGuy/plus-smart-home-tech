package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BookedProductsDto {
    @NotNull
    @Positive
    private Double deliveryWeight = 0.0;
    @NotNull
    @Positive
    private Double deliveryVolume = 0.0;
    @NotNull
    private Boolean fragile = false;
}
