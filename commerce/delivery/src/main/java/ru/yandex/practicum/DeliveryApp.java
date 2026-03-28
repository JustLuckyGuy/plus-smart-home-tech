package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.yandex.practicum.api.OrderOperations;
import ru.yandex.practicum.api.WarehouseOperations;

@SpringBootApplication
@EnableFeignClients(clients = {WarehouseOperations.class, OrderOperations.class})
public class DeliveryApp {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryApp.class, args);
    }
}
