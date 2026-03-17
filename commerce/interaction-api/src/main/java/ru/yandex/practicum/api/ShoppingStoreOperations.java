package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.store.ProductCategory;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.QuantityState;

import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreOperations {
    @GetMapping
    Page<ProductDto> getProducts(@RequestParam(name = "category") @NotNull ProductCategory category, Pageable pageable);

    @GetMapping("{productId}")
    ProductDto getProduct(@PathVariable @NotNull UUID productId);

    @PostMapping
    ProductDto updateProduct(@RequestBody @Valid ProductDto productDto);

    @PostMapping("/removeProductFromStore")
    boolean removeProductFromStore(@RequestBody @NotNull UUID productId);

    @PostMapping("/quantityState")
    boolean setProductQuantityState(@RequestParam @NotNull UUID productId, @RequestParam @NotNull QuantityState quantityState);

    @PutMapping
    ProductDto createNewProduct(@RequestBody @Valid ProductDto productDto);
}
