package uz.tenzorsoft.scaleapplication.ui.components;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachIdWithStatus;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.service.CargoService;
import uz.tenzorsoft.scaleapplication.service.PrintCheck;
import uz.tenzorsoft.scaleapplication.service.TruckService;
import uz.tenzorsoft.scaleapplication.ui.ButtonController;
import uz.tenzorsoft.scaleapplication.ui.CameraViewController;
import uz.tenzorsoft.scaleapplication.ui.TableController;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
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
@Setter
@Getter
public class TruckScalingController {
    private final ButtonController buttonController;
    private final CameraViewController cameraViewController;
    private final PrintCheck printCheck;
    private final TableController tableController;
    private final TruckService truckService;
    private final CargoService cargoService;
    private final ExecutorService executors;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean isTruckEntered = false;
    private boolean isOnScale = false;
    @Setter
    @Getter
    private boolean isScaled = false;
    @Setter
    @Getter
    private double weigh = 0.0;
    private boolean isCargoPhotoTaken = false;
    private boolean isCargoConfirmationDialogOpened = false;
    private boolean isTruckExited = false;

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("truckPosition = " + truckPosition);
                if (!sensor1Connection && truckPosition == 0) {
                    truckPosition = 1;
                    isTruckEntered = true;
                    isOnScale = true;
                    System.out.println("truckPosition = " + truckPosition);
                }

                if (truckPosition == 1 && sensor1Connection && (!sensor2Connection || isOnScale)) {
                    truckPosition = 2;
                    buttonController.closeGate1();
                    System.out.println("truckPosition = " + truckPosition);
                }

                if ((!sensor2Connection || isOnScale) && truckPosition == 2 &&
                        (currentTruck.getEnteredStatus() == TruckAction.ENTRANCE ||
                                currentTruck.getEnteredStatus() == TruckAction.MANUAL_ENTRANCE)) {
                    Timer timer = new Timer();
                    timer.schedule((new TimerTask() {
                        @Override
                        public void run() {
                            double holder = buttonController.getTruckWeigh();
                            double helper = holder != 0 ? holder : weigh;
                            if(helper > 0.0) System.out.println("helper weigh = " + helper);


                            if (weigh != helper && helper != 0.0 /*&& !isTesting*/) weigh = helper;
                            else if (weigh == helper && helper != 0) {
                                log.info("Truck weigh: {}", weigh);
                                isScaled = true;
                            }

                            if (isScaled && !isCargoPhotoTaken) {
                                AttachResponse response = cameraViewController.takePicture(CAMERA_2);
                                currentTruck.getAttaches().add(new AttachIdWithStatus(response.getId(), AttachStatus.ENTRANCE_CARGO_PHOTO));
                                truckService.saveTruckAttaches(currentTruck, response.getId());
                                isCargoPhotoTaken = true;
                                System.out.println("Opening gate 2");
                                buttonController.openGate2(); // Open Gate 2
                                if (weigh > 0) {
                                    currentTruck.setEnteredWeight(weigh);

                                    log.info("Truck entered weigh: {}", currentTruck.getEnteredWeight());
                                    currentTruck.setEnteredAt(LocalDateTime.now());
                                    currentTruck.setEntranceConfirmedBy(currentUser.getPhoneNumber());
                                    truckService.saveTruckEnteredActions(currentTruck);
                                    try {
                                        truckService.saveCurrentTruck(currentTruck, false);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    tableController.updateTableRow(truckService.getCurrentTruckEntity());
                                    isTruckEntered = true;
                                    currentTruck = new TruckResponse();
                                } else {
                                    isScaled = false;
                                }
                            }
                        }
                    }), 4000);
                }

                if (truckPosition == 2 && sensor2Connection && !sensor3Connection && isScaled) {
                    truckPosition = 3;
                    System.out.println("truckPosition = " + truckPosition);
                }

                if (truckPosition == 3 && sensor2Connection && sensor3Connection && isScaled) {
                    System.out.println("Gate 2 is closing");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("isTruckEntered = " + isTruckEntered);
                            if (isTruckEntered) {
                                truckPosition = -1;
                                isScaled = false;
                                buttonController.closeGate2();
                                isTruckEntered = false;
                                isCargoPhotoTaken = false;
                                isCargoConfirmationDialogOpened = false;
                                isOnScale = false;
                                weigh = 0.0;
                            }
                        }
                    }, 10000);
                }

                /////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////

                if (!sensor3Connection && truckPosition == 7) {
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

                if ((!sensor2Connection || isOnScale) && truckPosition == 5 &&
                        (currentTruck.getExitedStatus() == TruckAction.EXIT ||
                                currentTruck.getExitedStatus() == TruckAction.MANUAL_EXIT)) {
                    Timer timer = new Timer();
                    timer.schedule((new TimerTask() {
                        @Override
                        public void run() {
                            double holder = buttonController.getTruckWeigh();
                            double helper = holder != 0 ? holder : weigh;
                            if(helper > 0.0) System.out.println("helper weigh = " + helper);


                            if (weigh != helper && helper != 0.0 /*&& !isTesting*/) weigh = helper;
                            else if (weigh == helper && helper != 0) {
                                log.info("Truck weigh: {}", weigh);
                                isScaled = true;
                            }
                            if (!isScaleControlOn) cargoConfirmationStatus = -1;
                            else cargoConfirmationStatus = 1;

                            if (!isCargoConfirmationDialogOpened && isScaled && !isScaleControlOn) {
                                isCargoConfirmationDialogOpened = true;
                                cargoConfirmationStatus = showCargoScaleConfirmationDialog(weigh);
                                System.out.println("cargoConfirmationStatus = " + cargoConfirmationStatus);
                            }

                            if (isScaled && weigh > 0.0 && !isCargoPhotoTaken && cargoConfirmationStatus == 1) {
                                AttachResponse response = cameraViewController.takePicture(CAMERA_2);
                                currentTruck.getAttaches().add(new AttachIdWithStatus(response.getId(), AttachStatus.EXIT_CARGO_PHOTO));
                                truckService.saveTruckAttaches(currentTruck, response.getId());
                                isCargoPhotoTaken = true;
                                currentTruck.setExitedWeight(weigh);
                                log.info("Truck weigh: {}", currentTruck.getExitedWeight());
                                currentTruck.setExitedAt(LocalDateTime.now());
                                System.out.println("currentUser.getPhoneNumber() = " + currentUser.getPhoneNumber());
                                isTruckExited = true;
                                currentTruck.setExitConfirmedBy(currentUser.getPhoneNumber());
                                truckService.saveTruckExitedAction(currentTruck);
                                TruckEntity truck = null;
                                try {
                                    truck = truckService.saveCurrentTruck(currentTruck, true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    cargoService.saveCargo(truck);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                   printCheck.printReceipt(truckService.getCurrentTruckEntity());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                tableController.updateTableRow(truckService.getCurrentTruckEntity());

                                currentTruck = new TruckResponse();
                            }
                        }
                    }), 4000);
                }
                if (truckPosition == 5 && (!sensor2Connection || isOnScale) && isScaled && cargoConfirmationStatus == 0) {
                    truckPosition = 2;
                    cargoConfirmationStatus = -1;
                    isTruckEntered = true;
                    isTruckExited = false;
                    currentTruck = new TruckResponse();
                    System.out.println("Gate 2 is opening");
                    buttonController.openGate2();
                }

                if (truckPosition == 5 && (!sensor2Connection || isOnScale) && isScaled && cargoConfirmationStatus == 1) {
                    isTruckExited = true;
                    System.out.println("Opening gate 1");
                    buttonController.openGate1();
                }

                if (truckPosition == 5 && sensor2Connection && !sensor1Connection && isScaled && cargoConfirmationStatus == 1 && !gate1Connection) {
                    truckPosition = 4;
                    System.out.println("truckPosition = " + truckPosition);
                }

                if (truckPosition == 4 && sensor2Connection && sensor1Connection && isScaled && cargoConfirmationStatus == 1) {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (isTruckExited) {
                                isScaled = false;
                                buttonController.closeGate1();
                                isTruckExited = false;
                                isCargoPhotoTaken = false;
                                isCargoConfirmationDialogOpened = false;
                                isOnScale = false;
                                cargoConfirmationStatus = -1;
                                truckPosition = -1;
                                weigh = 0.0;
                            }
                        }
                    }, 10000);
                }

            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }
}
