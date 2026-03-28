package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${decimal.baseRate}")
    private BigDecimal baseRate;

    @Value("${decimal.warehouse1AddressMultiplier}")
    private BigDecimal warehouse1AddressMultiplier;

    @Value("${decimal.warehouse2AddressMultiplier}")
    private BigDecimal warehouse2AddressMultiplier;

    @Value("${decimal.fragileMultiplier}")
    private BigDecimal fragileMultiplier;

    @Value("${decimal.weightMultiplier}")
    private BigDecimal weightMultiplier;

    @Value("${decimal.volumeMultiplier}")
    private BigDecimal volumeMultiplier;

    @Value("${decimal.streetMultiplier}")
    private BigDecimal streetMultiplier;

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
        BigDecimal totalCost = baseRate;
        if (warehouseAddress.getCity().equals("ADDRESS_1")) {
            totalCost = totalCost.add(totalCost.multiply(warehouse1AddressMultiplier));
        } else {
            totalCost = totalCost.add(totalCost.multiply(warehouse2AddressMultiplier));
        }
        if (orderDto.getFragile()) {
            totalCost = totalCost.multiply(fragileMultiplier);
        }
        totalCost = totalCost.add(BigDecimal.valueOf(orderDto.getDeliveryVolume()).multiply(volumeMultiplier));
        totalCost = totalCost.add(BigDecimal.valueOf(orderDto.getDeliveryWeight()).multiply(weightMultiplier));
        if (!warehouseAddress.getStreet().equals(destinationAddress.getStreet())) {
            totalCost = totalCost.add(totalCost.multiply(streetMultiplier));
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
        log.info("Заказ #{} успешно доставлен", orderId);
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
