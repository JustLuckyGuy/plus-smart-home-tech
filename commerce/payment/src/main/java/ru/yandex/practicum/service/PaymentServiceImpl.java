package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.OrderOperations;
import ru.yandex.practicum.api.ShoppingStoreOperations;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.payment.PaymentState;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NoPaymentFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoOrderException;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderOperations orderClient;
    private final ShoppingStoreOperations shoppingStoreClient;

    private static final BigDecimal FEE_MULTIPLIER = BigDecimal.valueOf(0.1);

    @Override
    public PaymentDto payment(OrderDto orderDto) {
        log.info("Формируем оплату для заказа #{}", orderDto);
        BigDecimal productCost = orderDto.getProductPrice();
        BigDecimal deliveryTotal = orderDto.getDeliveryPrice();
        BigDecimal totalCost = orderDto.getTotalPrice();
        if (productCost == null || deliveryTotal == null || totalCost == null) {
            throw new NotEnoughInfoOrderException("Недостаточно информации в заказе для расчёта");
        }
        BigDecimal feeTotal = productCost.multiply(FEE_MULTIPLIER);
        Payment payment = paymentMapper.toPayment(orderDto, feeTotal);
        payment = paymentRepository.save(payment);
        return paymentMapper.toPaymentDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal productCost(OrderDto orderDto) {
        log.info("Рассчет стоимости товаров в заказе #{}", orderDto);
        Map<UUID, Integer> products = orderDto.getProducts();
        if (products.isEmpty()) {
            throw new NotEnoughInfoOrderException("Недостаточно информации в заказе для расчёта");
        }
        BigDecimal productCost = BigDecimal.valueOf(0.0);
        Set<UUID> ids = products.keySet();
        for (UUID id : ids) {
            BigDecimal price;
            try {
                price = shoppingStoreClient.getProduct(id).getPrice();
            } catch (FeignException e) {
                if (e.status() == 404) {
                    throw new ProductNotFoundException(e.getMessage());
                } else {
                    throw new RuntimeException(e.getMessage());
                }
            }
            Integer quantity = products.get(id);
            productCost = productCost.add(price.multiply(BigDecimal.valueOf(quantity)));
        }
        return productCost;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(OrderDto orderDto) {
        log.info("Рассчет полной суммы заказа #{}", orderDto);

        BigDecimal productCost = orderDto.getProductPrice();
        BigDecimal deliveryTotal = orderDto.getDeliveryPrice();
        if (productCost == null || deliveryTotal == null) {
            throw new NotEnoughInfoOrderException("Недостаточно информации в заказе для расчёта");
        }
        BigDecimal feeTotal = productCost.multiply(FEE_MULTIPLIER);
        return productCost.add(feeTotal).add(deliveryTotal);
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoPaymentFoundException("Оплата не найдена"));
        payment.setPaymentState(PaymentState.SUCCESS);
        paymentRepository.save(payment);
        try {
            orderClient.payment(payment.getOrderId());
        } catch (FeignException e) {
            if (e instanceof FeignException.NotFound) {
                throw new NoOrderFoundException(e.getMessage());
            }
        }
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoPaymentFoundException("Оплата не найдена"));
        payment.setPaymentState(PaymentState.FAILED);
        paymentRepository.save(payment);
        try {
            orderClient.paymentFailed(payment.getOrderId());
        } catch (FeignException e) {
            if (e instanceof FeignException.NotFound) {
                throw new NoOrderFoundException(e.getMessage());
            }
        }
    }
}
