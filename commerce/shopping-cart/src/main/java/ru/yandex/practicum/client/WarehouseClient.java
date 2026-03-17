package ru.yandex.practicum.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.api.WarehouseOperations;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseClient {
    private final WarehouseOperations warehouseOperations;
    private final CircuitBreakerFactory factory;

    public BookedProductsDto checkProductQuantity(ShoppingCartDto dto) {
        CircuitBreaker breaker = factory.create("warehouse");
        return breaker.run(
                () -> {
                    log.debug("Вызов склада для проверки корзины");
                    return warehouseOperations.checkProductQuantityEnoughForShoppingCart(dto);
                },
                throwable -> {
                    log.warn("Склад временно недоступен. Причина: {}", throwable.getMessage());
                    BookedProductsDto fallback = new BookedProductsDto();
                    fallback.setDeliveryWeight(0.0);
                    fallback.setDeliveryVolume(0.0);
                    fallback.setFragile(false);
                    return fallback;
                }
        );
    }
}
