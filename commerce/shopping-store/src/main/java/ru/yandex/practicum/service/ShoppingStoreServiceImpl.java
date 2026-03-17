package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.store.ProductCategory;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.ProductState;
import ru.yandex.practicum.dto.store.QuantityState;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ProductRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingStoreServiceImpl implements ShoppingStoreService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        log.info("Получаем товары по категории {}", category);

        Page<Product> products;
        if (category != null) {
            products = productRepository.findAllByProductCategory(category, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        List<ProductDto> productDtos = products.stream().map(productMapper::toProductDto).toList();

        log.info("Полученный список товаров: {} из {}, всего {}", products.getNumber(), products.getTotalPages(), products.getTotalElements());

        return new PageImpl<>(productDtos, pageable, products.getTotalElements());
    }

    @Override
    public ProductDto addProduct(ProductDto productDto) {
        Product product = productMapper.toProduct(productDto);
        log.info("Был добавлен новый продукт {}", product);
        productRepository.save(product);
        return productMapper.toProductDto(product);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        if (!productRepository.existsById(productDto.getProductId()))
            throw new ProductNotFoundException("Продукта с id: " + productDto.getProductId() + " не существует!");
        Product product = productRepository.save(productMapper.toProduct(productDto));
        log.info("Обновили данные для товара {}", product.getProductName());
        return productMapper.toProductDto(product);
    }

    @Override
    public boolean updateQuantityState(UUID productId, QuantityState quantityState) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException("Продукт с id: " + productId + " не найден"));
        product.setQuantityState(quantityState);
        log.info("Обновили количесвто товара {}", product.getProductName());
        productRepository.save(product);
        return true;
    }

    @Override
    public boolean removeProduct(UUID productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException("Продукт с id: " + productId + " не найден"));
        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        log.info("Деактивирован товар {}", product.getProductName());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID productId) {
        log.info("Получаем товар по его ID: {}", productId);
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException("Продукт с id: " + productId + " не найден"));
        return productMapper.toProductDto(product);
    }
}
