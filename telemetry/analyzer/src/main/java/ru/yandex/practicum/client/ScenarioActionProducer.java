package ru.yandex.practicum.client;

import com.google.protobuf.Empty;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.convertor.Convertor;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.model.Action;

@Slf4j
@Service
public class ScenarioActionProducer {
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterControllerBlockingStub;

    public ScenarioActionProducer(@GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterControllerBlockingStub) {
        this.hubRouterControllerBlockingStub = hubRouterControllerBlockingStub;
    }

    public void send(Action action) {
        DeviceActionRequest request = Convertor.mapToDeviceActionRequest(action);

        try {
            Empty response = hubRouterControllerBlockingStub.handleDeviceAction(request);
            log.info("Действие {} отправлено в hub-router", request);
            if (response.isInitialized()) {
                log.info("Получили ответ от хаба");
            } else {
                log.info("Нет ответа от хаба");
            }
        } catch (RuntimeException e) {
            log.info("Поймали ошибку отправки в хаброутер {}", e.getMessage(), e);
        }
    }
}
