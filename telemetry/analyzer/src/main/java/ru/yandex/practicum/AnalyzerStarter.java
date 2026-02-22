package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.processors.HubEventProcessor;
import ru.yandex.practicum.processors.SnapshotProcessor;

@Component
@RequiredArgsConstructor
public class AnalyzerStarter implements CommandLineRunner {
    final HubEventProcessor hubEventProcessor;
    final SnapshotProcessor snapshotProcessor;


    @Override
    public void run(String... args) throws Exception {
        Thread events = new Thread(hubEventProcessor);
        events.setName("HubEventHandlerThread");
        events.start();

        snapshotProcessor.start();
    }
}
