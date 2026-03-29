package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.yandex.practicum.api.OrderOperations;
import ru.yandex.practicum.api.ShoppingStoreOperations;

@SpringBootApplication
@EnableFeignClients(clients = {OrderOperations.class, ShoppingStoreOperations.class})
public class PaymentApp {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApp.class, args);
    }
}
