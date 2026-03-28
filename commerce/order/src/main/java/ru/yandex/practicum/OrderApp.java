package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.yandex.practicum.api.*;

@SpringBootApplication
@EnableFeignClients(clients = {WarehouseOperations.class, DeliveryOperations.class,
        ShoppingCartOperations.class, PaymentOperations.class})
public class OrderApp {
    public static void main(String[] args) {
        SpringApplication.run(OrderApp.class, args);
    }
}
