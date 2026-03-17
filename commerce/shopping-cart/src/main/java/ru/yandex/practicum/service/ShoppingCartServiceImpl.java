package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exception.DeactivateCartException;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartMapper cartMapper;
    private final WarehouseClient warehouseOperations;

    @Transactional(readOnly = true)
    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        validationUsername(username);
        log.info("Получаем корзину пользователя {}", username);
        ShoppingCart cart = getOrCreateShoppingCart(username);
        return cartMapper.toCartDto(cart);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> products) {
        validationUsername(username);
        log.info("Пользователь {} добавляет продукты в корзину:  {}", username, products);
        ShoppingCart cart = getOrCreateShoppingCart(username);
        checkCartIsActivate(cart);

        Map<UUID, Integer> oldProducts = cart.getProducts();
        oldProducts.putAll(products);
        cart.setProducts(oldProducts);

        BookedProductsDto bookedProductsDto = warehouseOperations.checkProductQuantity(cartMapper.toCartDto(cart));
        log.info("Проверили наличие товаров на складе, параметры заказа: {}", bookedProductsDto);
        cartRepository.save(cart);
        return cartMapper.toCartDto(cart);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        validationUsername(username);
        ShoppingCart cart = getOrCreateShoppingCart(username);
        checkCartIsActivate(cart);
        cart.setActive(false);
        log.info("Произошла деактивация корзины пользователя {}", username);
        cartRepository.save(cart);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products) {
        validationUsername(username);
        ShoppingCart cart = getOrCreateShoppingCart(username);
        checkCartIsActivate(cart);
        Map<UUID, Integer> oldProducts = cart.getProducts();
        for (UUID id : products) {
            if (oldProducts.containsKey(id)) {
                oldProducts.remove(id);
            }
        }
        cart.setProducts(oldProducts);
        log.info("Были удалены продукты из корзины");
        cartRepository.save(cart);
        return cartMapper.toCartDto(cart);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        validationUsername(username);
        ShoppingCart cart = getOrCreateShoppingCart(username);
        checkCartIsActivate(cart);
        Map<UUID, Integer> oldProducts = cart.getProducts();
        if (oldProducts.containsKey(request.getProductId())) {
            oldProducts.put(request.getProductId(), request.getNewQuantity());
        } else {
            throw new NoProductsInShoppingCartException("Продукт не найден в корзине");
        }

        cart.setProducts(oldProducts);
        log.info("Было измененно количество продукта в корзине пользователя {}", username);

        BookedProductsDto bookedProductsDto = warehouseOperations.checkProductQuantity(cartMapper.toCartDto(cart));
        log.info("Проверили наличие товаров на складе: {}", bookedProductsDto);
        cartRepository.save(cart);
        return cartMapper.toCartDto(cart);
    }

    private void validationUsername(String username) {
        if (username.isBlank()) {
            throw new NotAuthorizedUserException("Возникла с авторизацией пользователя " + username);
        }
    }

    private ShoppingCart getOrCreateShoppingCart(String username) {
        return cartRepository.findByUsernameAndActiveTrue(username).orElseGet(() -> {
            log.debug("Создается новая корзина для пользователя {}", username);
            ShoppingCart newCart = new ShoppingCart();
            newCart.setUsername(username);
            return cartRepository.save(newCart);
        });
    }

    private void checkCartIsActivate(ShoppingCart cart) {
        if (!cart.getActive()) {
            throw new DeactivateCartException("Корзина пользователя " + cart.getUsername() + "не активна");
        }
    }
}
