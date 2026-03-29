package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.WarehouseOperations;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseOperations {
    private final WarehouseService service;

    @Override
    public AddressDto getWarehouseAddress() {
        log.info("Пришел запрос на получение адреса склада");
        return service.getWarehouseAddress();
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto dto) {
        log.info("POST /api/v1/warehouse/check - Проверка количества товаров на складе: {}", dto);
        BookedProductsDto response = service.checkProductQuantityEnoughForShoppingCart(dto);
        log.info("Товары зарезервированы: {}", response);
        return response;
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        log.info("Пришел запрос принять продукт на склад");
        service.addProductToWarehouse(request);
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        log.info("Запрос на сборку товара для заказа{}", request.getOrderId());
        return service.assemblyProductsForOrder(request);
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("Запрос на передачу в доставку");
        service.shippedToDelivery(request);
    }

    @Override
    public void returnProduct(Map<UUID, Integer> productsToReturn) {
        log.info("Запрос на возвран товара");
        service.acceptReturn(productsToReturn);
    }

    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        log.info("Пришел запрос добавит товар на склад");
        service.newProductInWarehouse(request);
    }
}
