package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.ShoppingStoreOperations;
import ru.yandex.practicum.dto.store.ProductCategory;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.QuantityState;
import ru.yandex.practicum.service.ShoppingStoreService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ProductController implements ShoppingStoreOperations {
    private final ShoppingStoreService service;

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        log.info("Пришел запрос получения товаров: категория = {}, страница = {}", category, pageable);
        return service.getProducts(category, pageable);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        log.info("Пришел запрос получения товара по id {}", productId);
        return service.getProductById(productId);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        log.info("Пришел запрос на обновление товара {}", productDto);
        return service.updateProduct(productDto);
    }

    @Override
    public boolean removeProductFromStore(UUID productId) {
        log.info("Приешл запрос на удаление товара с id {}", productId);
        return service.removeProduct(productId);
    }

    @Override
    public boolean setProductQuantityState(UUID productId, QuantityState quantityState) {
        log.info("Пришел запрос на обновление количества товара c id {}", productId);
        return service.updateQuantityState(productId, quantityState);
    }


    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        log.info("Пришел запрос на добавление товара {}", productDto);
        return service.addProduct(productDto);
    }
}
