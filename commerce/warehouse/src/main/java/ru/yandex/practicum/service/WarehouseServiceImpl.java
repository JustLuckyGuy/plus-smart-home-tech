package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.BookingRepository;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository repository;
    private final WarehouseMapper mapper;
    private final BookingRepository bookingRepository;
    private final AddressDto warehouseAddress = initAddress();

    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        log.info("Добавляется новый продукт на склад {}", request);
        repository.findById(request.getProductId()).ifPresent(product -> {
            log.warn("Попытка доабвить уже существующий продукт с id = {}", request.getProductId());
            throw new SpecifiedProductAlreadyInWarehouseException("Товар уже находится на складе");
        });
        repository.save(mapper.toEntity(request));

    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cartDto) {
        log.info("Запрашиваем товары из корзины {}", cartDto);
        Map<UUID, Integer> products = cartDto.getProducts();
        log.info("Запрашиваем количество доступных товаров на складе {}", products.keySet());
        List<WarehouseProduct> availableProductsList = repository.findAllById(products.keySet());
        Map<UUID, WarehouseProduct> availableProductsMap = availableProductsList.stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        BookedProductsDto bookedProductsDto = new BookedProductsDto();
        for (Map.Entry<UUID, Integer> product : products.entrySet()) {
            UUID id = product.getKey();
            WarehouseProduct availableProduct = availableProductsMap.get(id);
            if (availableProduct == null) {
                throw new NoSpecifiedProductInWarehouseException("Такого товара нет в перечне товаров на складе:" + product.getKey().toString());
            }
            if (availableProduct.getQuantity() >= product.getValue()) {
                Double volume = bookedProductsDto.getDeliveryVolume() + (availableProduct.getWidth() * availableProduct.getHeight() * availableProduct.getDepth()) * product.getValue();
                bookedProductsDto.setDeliveryVolume(volume);
                Double weight = bookedProductsDto.getDeliveryWeight() + (availableProduct.getWeight()) * product.getValue();
                bookedProductsDto.setDeliveryWeight(weight);
                if (availableProduct.getFragile()) {
                    bookedProductsDto.setFragile(true);
                }
            } else {
                String message = "Количества продукта " + availableProduct.getProductId() + " недостаточно на складе. Уменьшите количество продукта до " + availableProduct.getQuantity();
                log.info(message);
                throw new ProductInShoppingCartLowQuantityInWarehouse(message);
            }
        }
        log.info("Параметры заказа: {}", bookedProductsDto);
        return bookedProductsDto;
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProduct product = repository.findById(request.getProductId()).orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Такого продукта нет на складе"));
        Integer oldQuantity = product.getQuantity();
        Integer newQuantity = oldQuantity + request.getQuantity();
        product.setQuantity(newQuantity);
        repository.save(product);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseAddress;
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        log.info("Собираем товары к заказу {} для передачи в доставку", request.getOrderId());
        Map<UUID, Integer> products = request.getProducts();
        List<WarehouseProduct> availableProductsList = repository.findAllById(products.keySet());
        Map<UUID, WarehouseProduct> availableProductsMap = availableProductsList.stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        BookedProductsDto bookedProductsDto = new BookedProductsDto();
        for (Map.Entry<UUID, Integer> product : products.entrySet()) {
            UUID id = product.getKey();
            WarehouseProduct availableProduct = availableProductsMap.get(id);
            if (availableProduct == null) {
                throw new NoSpecifiedProductInWarehouseException("Такого товара нет в перечне товаров на складе:" + product.getKey().toString());
            }
            if (availableProduct.getQuantity() >= product.getValue()) {
                Double volume = bookedProductsDto.getDeliveryVolume() + (availableProduct.getWidth() * availableProduct.getHeight() * availableProduct.getDepth()) * product.getValue();
                bookedProductsDto.setDeliveryVolume(volume);
                Double weight = bookedProductsDto.getDeliveryWeight() + (availableProduct.getWeight()) * product.getValue();
                bookedProductsDto.setDeliveryWeight(weight);
                if (availableProduct.getFragile()) {
                    bookedProductsDto.setFragile(true);
                }
                availableProduct.setQuantity(availableProduct.getQuantity() - product.getValue());
                log.info("Уменьшаем доступный остаток товара {}", availableProduct.getProductId());
                repository.save(availableProduct);
            } else {
                String message = "Количества продукта " + availableProduct.getProductId() + " недостаточно на складе. Уменьшите количество продукта до " + availableProduct.getQuantity();
                log.info(message);
                throw new ProductInShoppingCartLowQuantityInWarehouse(message);
            }
        }
        OrderBooking orderBooking = mapper.toOrderBooking(request);
        bookingRepository.save(orderBooking);
        log.info("Возвращаем параметры заказа: {}", bookedProductsDto);
        return bookedProductsDto;
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("Запрос на передачу в доставку - {}", request);
        OrderBooking orderBooking = bookingRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new NoOrderFoundException("В базе нет бронирования такого заказа {}" + request.getOrderId()));
        orderBooking.setDeliveryId(request.getDeliveryId());
        bookingRepository.save(orderBooking);
    }

    @Override
    public void acceptReturn(Map<UUID, Integer> productsToReturn) {
        log.info("Запрошено принятие возврата товаров на склад {}", productsToReturn);

        List<WarehouseProduct> productsToIncreaseList = repository.findAllById(productsToReturn.keySet());
        Map<UUID, WarehouseProduct> productsToIncreaseMap = productsToIncreaseList.stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));

        for (Map.Entry<UUID, Integer> productToReturn : productsToReturn.entrySet()) {
            UUID id = productToReturn.getKey();
            WarehouseProduct productToIncrease = productsToIncreaseMap.get(id);

            Integer oldQuantity = productToIncrease.getQuantity();
            Integer newQuantity = oldQuantity + productsToReturn.get(id);
            productToIncrease.setQuantity(newQuantity);
            repository.save(productToIncrease);
        }
    }

    private AddressDto initAddress() {
        final String[] addresses = new String[]{"ADDRESS_1", "ADDRESS_2"};
        final String address = addresses[Random.from(new SecureRandom()).nextInt(0, 1)];
        return AddressDto.builder()
                .city(address)
                .street(address)
                .house(address)
                .country(address)
                .flat(address)
                .build();
    }
}
