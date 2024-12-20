package uz.tenzorsoft.scaleapplication.ui.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.sendData.SendDataService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnectedToInternet;
import static uz.tenzorsoft.scaleapplication.domain.Instances.isTesting;

@Component
@RequiredArgsConstructor
public class DataSendController {

    private final ExecutorService executors;
    private final SendDataService sendDataService;
    private final LogService logService;

    public void sendNotSentData() {
        executors.execute(() -> {

/*
            while(true) {
                try{
                    try {
                        sendDataService.sendNotSentData();
                    } catch (Exception e) {
                        logService.save(new LogEntity(5L, Instances.truckNumber, "00036: (" + getClass().getName() + ") " +e.getMessage()));
                        System.err.println(e.getMessage());
                    }

                    try {
                        sendDataService.sendDataToMyCoal();
                    } catch (Exception e) {
                        logService.save(new LogEntity(5L, Instances.truckNumber, "00037: (" + getClass().getName() + ") " + e.getMessage()));
                        System.err.println(e.getMessage());
                    }

                    try {
                        sendDataService.sendLogsToServer();
                    } catch (Exception e) {
                        logService.save(new LogEntity(5L, Instances.truckNumber, "00038: (" + getClass().getName() + ") " + e.getMessage()));
                        System.err.println(e.getMessage());
                    }

                    Thread.sleep(2000);

                }catch (Exception e) {
                    logService.save(new LogEntity(5L, Instances.truckNumber, "00040: (" + getClass().getName() + ") " +e.getMessage()));
                    System.err.println(e.getMessage());
                }
            }
*/

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!isConnectedToInternet) continue;
                    CompletableFuture.runAsync(() -> sendDataService.sendNotSentData())
                            .exceptionally(e -> {
                                logService.save(new LogEntity(5L, Instances.truckNumber, "00036: (" + getClass().getName() + ") " + e.getMessage()));
                                System.err.println(e.getMessage());
                                return null;
                            });

                    if (!isTesting) {
                        CompletableFuture.runAsync(() -> sendDataService.sendDataToMyCoal())
                                .exceptionally(e -> {
                                    logService.save(new LogEntity(5L, Instances.truckNumber, "00037: (" + getClass().getName() + ") " + e.getMessage()));
                                    System.err.println(e.getMessage());
                                    return null;
                                });

                        CompletableFuture.runAsync(() -> sendDataService.sendLogsToServer())
                                .exceptionally(e -> {
                                    logService.save(new LogEntity(5L, Instances.truckNumber, "00038: (" + getClass().getName() + ") " + e.getMessage()));
                                    System.err.println(e.getMessage());
                                    return null;
                                });

                        CompletableFuture.runAsync(() -> sendDataService.sendProductsToServer())
                                .exceptionally(e -> {
                                    logService.save(new LogEntity(5L, Instances.truckNumber, "00038: (" + getClass().getName() + ") " + e.getMessage()));
                                    System.err.println(e.getMessage());
                                    return null;
                                });
                    }

                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Tarmoqni xavfsiz to'xtatish
                    logService.save(new LogEntity(5L, Instances.truckNumber, "00039: (" + getClass().getName() + ") " + "Thread interrupted: " + e.getMessage()));
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, Instances.truckNumber, "00040: (" + getClass().getName() + ") " + e.getMessage()));
                    System.err.println(e.getMessage());
                }
            }
        });
    }

}
