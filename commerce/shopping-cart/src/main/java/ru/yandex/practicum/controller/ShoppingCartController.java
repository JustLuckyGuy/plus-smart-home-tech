package ru.yandex.practicum.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.ShoppingCartOperations;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController implements ShoppingCartOperations {
    private final ShoppingCartService shoppingCartService;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        ShoppingCartDto response = shoppingCartService.getShoppingCart(username);
        log.info("Пользователь {}, получил актуальную информацию корзины: {}", username, response);
        return response;
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products) {
        ShoppingCartDto response = shoppingCartService.removeFromShoppingCart(username, products);
        log.info("Пользователь {} удалил из корзины продукты: {}", username, products);
        return response;
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        log.info("Пришел запрос изменить количества продукта");
        return shoppingCartService.changeProductQuantity(username, request);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> products) {
        ShoppingCartDto response = shoppingCartService.addProductToShoppingCart(username, products);
        log.info("Пользователь {} добавил продукты: {}", username, products);
        return response;
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        shoppingCartService.deactivateCurrentShoppingCart(username);
    }
}
