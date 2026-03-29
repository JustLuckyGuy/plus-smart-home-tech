package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", path = "/api/v1/shopping-cart")
public interface ShoppingCartOperations {
    @GetMapping
    ShoppingCartDto getShoppingCart(@RequestParam(name = "username") @NotNull String username);

    @GetMapping("/name")
    String getUsernameById(@RequestParam @NotNull UUID cartId);

    @PostMapping("/remove")
    ShoppingCartDto removeFromShoppingCart(@RequestParam(name = "username") @NotNull String username, @RequestBody List<UUID> products);

    @PostMapping("/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam(name = "username") @NotNull String username, @RequestBody @Valid ChangeProductQuantityRequest request);

    @PutMapping
    ShoppingCartDto addProductToShoppingCart(@RequestParam(name = "username") @NotNull String username, @RequestBody Map<UUID, Integer> products);

    @DeleteMapping
    void deactivateCurrentShoppingCart(@RequestParam(name = "username") @NotNull String username);
}
