package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.DeliveryOperations;
import ru.yandex.practicum.api.PaymentOperations;
import ru.yandex.practicum.api.ShoppingCartOperations;
import ru.yandex.practicum.api.WarehouseOperations;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exception.*;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WarehouseOperations warehouseClient;
    private final ShoppingCartOperations shoppingCartClient;
    private final PaymentOperations paymentClient;
    private final DeliveryOperations deliveryClient;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        validateUsername(username);
        List<Order> orders = orderRepository.findAllByUsername(username);
        log.info("Получен список заказов пользователя {}", username);

        return orders.stream().map(orderMapper::toOrderDto).toList();
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("Создаем новый заказ: shoppingCartId {}, products {}", request.getShoppingCartDto().getCartId(), request.getShoppingCartDto().getProducts());
        BookedProductsDto bookedProductsDto;
        try {
            bookedProductsDto = warehouseClient.checkProductQuantityEnoughForShoppingCart(request.getShoppingCartDto());
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(e.getMessage());
            } else if (e.status() == 404) {
                throw new NoSpecifiedProductInWarehouseException(e.getMessage());
            } else {
                throw new RuntimeException(e.getMessage());
            }
        }

        String username;
        try {
            username = shoppingCartClient.getUsernameById(request.getShoppingCartDto().getCartId());
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new NoCartException(e.getMessage());
            } else {
                throw new RuntimeException(e.getMessage());
            }
        }
        Order newOrder = orderMapper.toOrder(request, bookedProductsDto, username);
        newOrder = orderRepository.save(newOrder);

        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setFromAddress(warehouseClient.getWarehouseAddress());
        deliveryDto.setToAddress(request.getAddressDto());
        deliveryDto.setOrderId(newOrder.getOrderId());
        deliveryDto.setDeliveryState(DeliveryState.CREATED);

        DeliveryDto newDeliveryDto = deliveryClient.requestDelivery(deliveryDto);

        newOrder.setDeliveryId(newDeliveryDto.getDeliveryId());
        newOrder = orderRepository.save(newOrder);

        return orderMapper.toOrderDto(newOrder);
    }

    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("Возврат заказа OrderId: {}, products: {}", request.getOrderId(), request.getProducts());
        Order orderToReturn = findOrder(request.getOrderId());
        Map<UUID, Integer> productsToReturn = request.getProducts();
        Set<UUID> ids = productsToReturn.keySet();
        for (UUID id : ids) {
            AddProductToWarehouseRequest addProductToWarehouseRequest = new AddProductToWarehouseRequest(id, productsToReturn.get(id));
            warehouseClient.addProductToWarehouse(addProductToWarehouseRequest);
        }
        orderToReturn = changeOrderStateAndSave(orderToReturn, OrderState.PRODUCT_RETURNED);
        return orderMapper.toOrderDto(orderToReturn);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        log.info("Оплата заказа OrderId: {}", orderId);
        Order orderToPay = findOrder(orderId);
        orderToPay = changeOrderStateAndSave(orderToPay, OrderState.PAID);
        warehouseClient.assemblyProductsForOrder(new AssemblyProductsForOrderRequest(orderToPay.getProducts(), orderId));

        return orderMapper.toOrderDto(orderToPay);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        log.info("Оплата заказа не прошла OrderId: {}", orderId);
        Order orderToPay = findOrder(orderId);
        orderToPay = changeOrderStateAndSave(orderToPay, OrderState.PAYMENT_FAILED);
        return orderMapper.toOrderDto(orderToPay);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        Order orderToDeliver = findOrder(orderId);
        orderToDeliver = changeOrderStateAndSave(orderToDeliver, OrderState.DELIVERED);
        return orderMapper.toOrderDto(orderToDeliver);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        Order orderToDeliver = findOrder(orderId);
        orderToDeliver = changeOrderStateAndSave(orderToDeliver, OrderState.DELIVERY_FAILED);
        return orderMapper.toOrderDto(orderToDeliver);
    }

    @Override
    public OrderDto complete(UUID orderId) {
        Order orderToComplete = findOrder(orderId);
        orderToComplete = changeOrderStateAndSave(orderToComplete, OrderState.COMPLETED);
        return orderMapper.toOrderDto(orderToComplete);
    }

    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("Вычисляем стоимость заказа #: {}", orderId);
        Order orderToCalculate = findOrder(orderId);
        BigDecimal totalCost = paymentClient.getTotalCost(orderMapper.toOrderDto(orderToCalculate));
        orderToCalculate.setTotalPrice(totalCost);

        PaymentDto paymentDto = paymentClient.payment(orderMapper.toOrderDto(orderToCalculate));
        orderToCalculate.setPaymentId(paymentDto.getPaymentId());

        orderToCalculate = orderRepository.save(orderToCalculate);
        return orderMapper.toOrderDto(orderToCalculate);
    }

    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("Вычисляем стоимость доставки заказа #: {}", orderId);
        Order orderToCalculate = findOrder(orderId);
        BigDecimal productCost = paymentClient.productCost(orderMapper.toOrderDto(orderToCalculate));
        orderToCalculate.setProductPrice(productCost);
        BigDecimal deliveryCost = deliveryClient.deliveryCost(orderMapper.toOrderDto(orderToCalculate));
        orderToCalculate.setDeliveryPrice(deliveryCost);

        orderToCalculate = orderRepository.save(orderToCalculate);
        return orderMapper.toOrderDto(orderToCalculate);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        Order orderToAssembly = findOrder(orderId);
        orderToAssembly = changeOrderStateAndSave(orderToAssembly, OrderState.ASSEMBLED);
        return orderMapper.toOrderDto(orderToAssembly);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        Order orderToAssembly = findOrder(orderId);
        orderToAssembly = changeOrderStateAndSave(orderToAssembly, OrderState.ASSEMBLY_FAILED);
        return orderMapper.toOrderDto(orderToAssembly);
    }

    private void validateUsername(String username) {
        if (username.isBlank()) {
            throw new NotAuthorizedUserException(username);
        }
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не нашелся заказ с id: " + orderId));
    }

    private Order changeOrderStateAndSave(Order order, OrderState orderState) {
        order.setOrderState(orderState);
        log.info("Изменили статус заказа # {} на {}", order.getOrderId(), orderState);
        order = orderRepository.save(order);
        return order;
    }
}
