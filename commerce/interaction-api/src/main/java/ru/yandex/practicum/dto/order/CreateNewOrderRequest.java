package ru.yandex.practicum.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;

@Data
public class CreateNewOrderRequest {
    @NotNull
    private ShoppingCartDto shoppingCartDto;

    @NotNull
    private AddressDto addressDto;

}
