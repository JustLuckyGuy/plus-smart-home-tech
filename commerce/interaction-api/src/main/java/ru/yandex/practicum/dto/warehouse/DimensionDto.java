package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DimensionDto {
    @NotNull
    @Positive
    @Min(1)
    private Double width;
    @NotNull
    @Positive
    @Min(1)
    private Double height;
    @NotNull
    @Positive
    @Min(1)
    private Double depth;
}
