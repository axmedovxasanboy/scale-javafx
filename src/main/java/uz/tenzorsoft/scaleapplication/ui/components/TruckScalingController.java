package uz.tenzorsoft.scaleapplication.ui.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachIdWithStatus;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.service.CargoService;
import uz.tenzorsoft.scaleapplication.service.TruckService;
import uz.tenzorsoft.scaleapplication.ui.ButtonController;
import uz.tenzorsoft.scaleapplication.ui.CameraViewController;
import uz.tenzorsoft.scaleapplication.ui.TableController;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.CAMERA_2;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.truckPosition;
import static uz.tenzorsoft.scaleapplication.ui.MainController.showCargoScaleConfirmationDialog;

@Component
@RequiredArgsConstructor
@Slf4j
public class TruckScalingController {
    private final ButtonController buttonController;
    private final CameraViewController cameraViewController;
    private final TableController tableController;
    private final TruckService truckService;
    private final CargoService cargoService;
    private boolean isTruckEntered = false;
    private boolean isOnScale = false;
    private boolean isScaled = false;
    private boolean isCargoPhotoTaken = false;
    private boolean isCargoConfirmationDialogOpened = false;
    private boolean isTruckExited = false;
    private double weigh = 0.0;

    private final ExecutorService executors;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void start() {
        executors.execute(() -> {
            while (true) {
                try {
                    if (!sensor1Connection && truckPosition == 0) {
                        truckPosition = 1;
                        isTruckEntered = true;
                        isOnScale = true;
                        System.out.println("truckPosition = " + truckPosition);
                    }

                    if (truckPosition == 1 && sensor1Connection && (!sensor2Connection || isOnScale)) {
                        currentTruck.setEnteredStatus(TruckAction.ENTRANCE);
                        truckPosition = 2;
                        buttonController.closeGate1();
                        System.out.println("truckPosition = " + truckPosition);
                    }

                    if ((!sensor2Connection || isOnScale) && truckPosition == 2 && currentTruck.getEnteredStatus() == TruckAction.ENTRANCE) {
                        scheduler.schedule(() -> {
                            double helper = buttonController.getTruckWeigh();
                            System.out.println("weigh = " + helper);

                            if (weigh != helper) weigh = helper;
                            else if (weigh == helper) {
                                log.info("Truck weigh: {}", weigh);
                                isScaled = weigh == helper;
                            }

                            if (isScaled && !isCargoPhotoTaken) {
                                currentTruck.getAttaches().add(new AttachIdWithStatus(cameraViewController.takePicture(CAMERA_2).getId(), AttachStatus.ENTRANCE_CARGO_PHOTO));
                                buttonController.openGate2();
                                currentTruck.setEnteredWeight(weigh);
                                log.info("Truck entered weigh: {}", currentTruck.getEnteredWeight());
                                currentTruck.setEnteredAt(LocalDateTime.now());
                                currentTruck.setEntranceConfirmedBy(currentUser.getUsername());
                                TruckEntity truck = truckService.save(currentTruck);
                                tableController.updateTableRow(truck);
                                isTruckEntered = true;
                                currentTruck = new TruckResponse();
                            }

                        }, 4, TimeUnit.SECONDS);

                    }

                    if (truckPosition == 2 && sensor2Connection && !sensor3Connection && isScaled) {
                        truckPosition = 3;
                        System.out.println("truckPosition = " + truckPosition);
                    }

                    if (truckPosition == 3 && sensor2Connection && sensor3Connection && isScaled) {
                        System.out.println("Gate 2 is closing");
                        scheduler.schedule(() -> {
                            System.out.println("isTruckEntered = " + isTruckEntered);
                            if (isTruckEntered) {
                                isScaled = false;
                                truckPosition = 0;
                                buttonController.closeGate2();
                                isTruckEntered = false;
                                isCargoPhotoTaken = false;
                                isCargoConfirmationDialogOpened = false;
                                isOnScale = false;
                                truckPosition = -1;
                                weigh = 0.0;
                            }
                        }, 10, TimeUnit.SECONDS);
                    }

                    /////////////////////////////////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////

                    if (!sensor3Connection && truckPosition == 7) {
                        currentTruck.setExitedStatus(TruckAction.EXIT);
                        isTruckExited = true;
                        truckPosition = 6;
                        System.out.println("truckPosition = " + truckPosition);
                    }

                    if (truckPosition == 6 && sensor3Connection && !sensor2Connection) {
                        isOnScale = true;
                        truckPosition = 5;
                        buttonController.closeGate2();
                        System.out.println("truckPosition = " + truckPosition);
                    }


                    if ((!sensor2Connection || isOnScale) && truckPosition == 5 && currentTruck.getExitedStatus() == TruckAction.EXIT) {
                        scheduler.schedule(() -> {
                            double helper = buttonController.getTruckWeigh();
                            System.out.println("helper = " + helper);

                            if (weigh != helper) weigh = helper;
                            else if (weigh == helper) {
                                log.info("Truck weigh: {}", weigh);
                                isScaled = true;
                            }
                            if (!isScaleControlOn) cargoConfirmationStatus = 1;
                            else cargoConfirmationStatus = -1;


                            if (!isCargoConfirmationDialogOpened && isScaled && isScaleControlOn) {
                                isCargoConfirmationDialogOpened = true;
                                cargoConfirmationStatus = showCargoScaleConfirmationDialog(helper);
                                System.out.println("cargoConfirmationStatus = " + cargoConfirmationStatus);
                            }

                            if (isScaled && !isCargoPhotoTaken && cargoConfirmationStatus == 1) {
                                currentTruck.getAttaches().add(new AttachIdWithStatus(cameraViewController.takePicture(CAMERA_2).getId(), AttachStatus.EXIT_CARGO_PHOTO));
                                weigh = helper;
                                currentTruck.setExitedWeight(weigh);
                                log.info("Truck weigh: {}", currentTruck.getExitedWeight());
                                currentTruck.setExitedAt(LocalDateTime.now());
                                currentTruck.setExitConfirmedBy(currentUser.getUsername());
                                TruckEntity truck = truckService.save(currentTruck);
                                tableController.updateTableRow(truck);
                                cargoService.saveCargo(truck);
                                isTruckExited = true;
                                currentTruck = new TruckResponse();
                            }

                        }, 4, TimeUnit.SECONDS);

                    }

                    if (truckPosition == 5 && (!sensor2Connection || isOnScale) && isScaled && cargoConfirmationStatus == 0) {
                        truckPosition = 2;
                        cargoConfirmationStatus = -1;
                        isTruckEntered = true;
                        isTruckExited = false;
                        currentTruck = new TruckResponse();
                        buttonController.openGate2();
                    }


                    if (truckPosition == 5 && (!sensor2Connection || isOnScale) && isScaled && cargoConfirmationStatus == 1) {
                        isTruckExited = true;
                        buttonController.openGate1();
                    }

                    if (truckPosition == 5 && sensor2Connection && !sensor1Connection && isScaled && cargoConfirmationStatus == 1 && !gate1Connection) {
                        truckPosition = 4;
                        System.out.println("truckPosition = " + truckPosition);
                    }

                    if (truckPosition == 4 && sensor2Connection && sensor1Connection && isScaled && cargoConfirmationStatus == 1) {
                        scheduler.schedule(() -> {
                            if (isTruckExited) {
                                isScaled = false;
                                truckPosition = 0;
                                buttonController.closeGate1();
                                isTruckExited = false;
                                isCargoPhotoTaken = false;
                                isCargoConfirmationDialogOpened = false;
                                isOnScale = false;
                                cargoConfirmationStatus = -1;
                                truckPosition = -1;
                                weigh = 0.0;
                            }
                        }, 10, TimeUnit.SECONDS);
                    }


                    Thread.sleep(500);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }
}
