package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.PaymentOperations;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController implements PaymentOperations {
    private final PaymentService service;

    @Override
    public BigDecimal productCost(OrderDto orderDto) {
        log.info("Пришел запрос расчета стоимости продукта заказа #{}", orderDto);
        return service.productCost(orderDto);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto orderDto) {
        log.info("Пришел запрос расчета полной стоимости заказа #{}", orderDto);
        return service.getTotalCost(orderDto);
    }

    @Override
    public PaymentDto payment(OrderDto orderDto) {
        log.info("Пришел запрос оплаты заказа #{}", orderDto);
        return service.payment(orderDto);
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        service.paymentSuccess(paymentId);
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        service.paymentFailed(paymentId);
    }
}
