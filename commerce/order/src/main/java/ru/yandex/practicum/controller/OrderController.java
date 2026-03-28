package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.OrderOperations;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController implements OrderOperations {
    private final OrderService service;

    @Override
    public List<OrderDto> getClientsOrders(String username) {
        log.info("Пришел запрос на получение заказов пользователя: username={}", username);
        return service.getClientOrders(username);
    }

    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("Пришел запрос на возврат товаров заказа #{}", request.getOrderId());
        return service.productReturn(request);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        log.info("Пришел запрос на оплату заказа #{}", orderId);
        return service.payment(orderId);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        log.info("Пришел запрос на ошибку оплаты заказа #{}", orderId);
        return service.paymentFailed(orderId);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        log.info("Пришел запрос на доставку заказа #{}", orderId);
        return service.delivery(orderId);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("Пришел запрос на ошибку доставки заказа #{}", orderId);
        return service.deliveryFailed(orderId);
    }

    @Override
    public OrderDto complete(UUID orderId) {
        log.info("Пришел запрос на завершение заказа #{}", orderId);
        return service.complete(orderId);
    }

    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("Пришел запрос на расчет общей стоимости заказа #{}", orderId);
        return service.calculateTotalCost(orderId);
    }

    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("Пришел запрос на расчет доставки заказа заказа #{}", orderId);
        return service.calculateDeliveryCost(orderId);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        log.info("Пришел запрос на сборку заказа #{}", orderId);
        return service.assembly(orderId);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("Пришел запрос на ошибку сборки заказа #{}", orderId);
        return service.assemblyFailed(orderId);
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("Пришел запрос на создание нового заказа {}", request.getShoppingCartDto());
        return service.createNewOrder(request);
    }
}
