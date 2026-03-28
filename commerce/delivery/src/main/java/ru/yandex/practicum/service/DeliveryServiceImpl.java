package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.OrderOperations;
import ru.yandex.practicum.api.WarehouseOperations;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final WarehouseOperations warehouseClient;
    private final OrderOperations orderClient;

    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(5.0);
    private static final BigDecimal WAREHOUSE_1_ADDRESS_MULTIPLIER = BigDecimal.valueOf(1);
    private static final BigDecimal WAREHOUSE_2_ADDRESS_MULTIPLIER = BigDecimal.valueOf(2);
    private static final BigDecimal FRAGILE_MULTIPLIER = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_MULTIPLIER = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_MULTIPLIER = BigDecimal.valueOf(0.2);
    private static final BigDecimal STREET_MULTIPLIER = BigDecimal.valueOf(0.2);

    @Override
    public DeliveryDto requestDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = deliveryMapper.toDelivery(deliveryDto);
        delivery = deliveryRepository.save(delivery);
        log.info("Доставка #{}, добавлена в базу данных: {}", delivery.getDeliveryId(), delivery);
        return deliveryMapper.toDeliveryDto(delivery);
    }

    @Override
    public BigDecimal deliveryCost(OrderDto orderDto) {
        log.info("Начался процесс расчета стоимости доставки");
        Delivery delivery = deliveryRepository.findById(orderDto.getDeliveryId()).orElseThrow(
                () -> new NoDeliveryFoundException("Не найдено досавки #" + orderDto.getDeliveryId() + " с заказом #" + orderDto.getOrderId()));
        Address warehouseAddress = delivery.getFromAddress();
        Address destinationAddress = delivery.getToAddress();
        BigDecimal totalCost = BASE_RATE;
        if (warehouseAddress.getCity().equals("ADDRESS_1")) {
            totalCost = totalCost.add(totalCost.multiply(WAREHOUSE_1_ADDRESS_MULTIPLIER));
        } else {
            totalCost = totalCost.add(totalCost.multiply(WAREHOUSE_2_ADDRESS_MULTIPLIER));
        }
        if (orderDto.getFragile()) {
            totalCost = totalCost.multiply(FRAGILE_MULTIPLIER);
        }
        totalCost = totalCost.add(BigDecimal.valueOf(orderDto.getDeliveryVolume()).multiply(VOLUME_MULTIPLIER));
        totalCost = totalCost.add(BigDecimal.valueOf(orderDto.getDeliveryWeight()).multiply(WEIGHT_MULTIPLIER));
        if (!warehouseAddress.getStreet().equals(destinationAddress.getStreet())) {
            totalCost = totalCost.add(totalCost.multiply(STREET_MULTIPLIER));
        }

        log.info("Готовая стоимость доставки = {}", totalCost);

        return totalCost;
    }

    @Override
    public void deliveryPicked(UUID orderId) {
        log.info("Заказ #{} передан в доставку", orderId);
        Delivery delivery = findDelivery(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        delivery = deliveryRepository.save(delivery);
        orderClient.assembly(orderId);
        warehouseClient.shippedToDelivery(new ShippedToDeliveryRequest(orderId, delivery.getDeliveryId()));
    }

    @Override
    public void deliverySuccessful(UUID orderId) {
        log.info("Заказ #{} успешно доставлен");
        Delivery delivery = findDelivery(orderId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderClient.delivery(orderId);
    }

    @Override
    public void deliveryFailed(UUID orderId) {
        log.info("Произошла ошибка с доставкой заказа #{}", orderId);
        Delivery delivery = findDelivery(orderId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderClient.deliveryFailed(orderId);
    }

    private Delivery findDelivery(UUID orderId) {
        return deliveryRepository.findById(orderId).orElseThrow(
                () -> new NoDeliveryFoundException("Не найдено досавки с заказом #" + orderId));
    }
}
