package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.DeliveryOperations;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryOperations {
    private final DeliveryService service;

    @Override
    public BigDecimal deliveryCost(OrderDto orderDto) {
        log.info("Пришел запрос на расчет суммы доставки заказа: {}", orderDto);
        return service.deliveryCost(orderDto);
    }

    @Override
    public void deliveryPicked(UUID orderId) {
        log.info("Пришел запрос на отправку заказа #{}", orderId);
        service.deliveryPicked(orderId);
    }

    @Override
    public void deliverySuccessful(UUID orderId) {
        log.info("Отметить об успешной доставки заказа #{}", orderId);
        service.deliverySuccessful(orderId);
    }

    @Override
    public void deliveryFailed(UUID orderId) {
        log.info("Отметить об ошибке при доставки заказа #{}", orderId);
        service.deliveryFailed(orderId);
    }

    @Override
    public DeliveryDto requestDelivery(DeliveryDto deliveryDto) {
        log.info("Пришел запрос на создание новой доставки {}", deliveryDto);
        return service.requestDelivery(deliveryDto);
    }
}
