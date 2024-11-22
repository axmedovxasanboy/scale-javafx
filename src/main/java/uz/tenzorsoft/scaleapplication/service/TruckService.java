package uz.tenzorsoft.scaleapplication.service;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.*;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.request.TruckRequest;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.ActionResponse;
import uz.tenzorsoft.scaleapplication.repository.TruckActionRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckPhotoRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TruckService implements BaseService<TruckEntity, TruckResponse, TruckRequest> {


    private final TruckRepository truckRepository;
    private final TruckActionRepository truckActionRepository;
    private final TruckPhotoRepository truckPhotoRepository;
    private final UserService userService;
    private final CargoService cargoService;
    private final LogService logService;

    @Setter
    @Getter
    private TruckEntity currentTruckEntity = new TruckEntity();
    @Lazy
    @Autowired
    private AttachService attachService;

    @Value("${number.pattern.regexp.number}")
    private String regexPattern;
    @Value("${number.pattern.regexp.standard}")
    private String regexStandard;

    public List<TableViewData> getTruckData() {
        List<TruckEntity> all = truckRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<TableViewData> data = new ArrayList<>();
        for (TruckEntity truck : all) {
            data.add(entityToTableViewData(truck));
        }
        return data;
    }

    @Transactional
    public TruckEntity save(TruckResponse truck) {
        TruckEntity truckEntity = truckRepository.findTruckWithEntranceNoExit(truck.getTruckNumber()).orElse(new TruckEntity());

        if (truckEntity.getId() != null) {
            TruckActionEntity truckAction = new TruckActionEntity(
                    truck.getExitedWeight(), TruckAction.EXIT, userService.findByPhoneNumber(truck.getExitConfirmedBy()), false
            );
            truckEntity.getTruckActions().add(truckAction);
            return truckRepository.save(truckEntity);
        }

        List<TruckActionEntity> truckActions = new ArrayList<>();
        truckActions.add(new TruckActionEntity(truck.getEnteredWeight(),
                TruckAction.ENTRANCE, userService.findByPhoneNumber(truck.getEntranceConfirmedBy()), false)
        );
        truckActions.add(new TruckActionEntity(truck.getExitedWeight(),
                TruckAction.EXIT, userService.findByPhoneNumber(truck.getEntranceConfirmedBy()), false)
        );

        truckActionRepository.saveAll(truckActions);

        List<TruckPhotosEntity> truckPhotos = new ArrayList<>();

//        truck.getAttaches().forEach(attach -> {
//            truckPhotos.add(new TruckPhotosEntity(
//                    attachService.findById(attach.getId()), attach.getStatus()
//            ));
//        });

        Set<Long> attachIds = new HashSet<>();
        System.out.println("truck.getAttaches().size() = " + truck.getAttaches().size());
        truck.getAttaches().forEach(attach -> {
            if (!attachIds.contains(attach.getId())) { // Check for uniqueness
                System.out.println("attach.getId() = " + attach.getId());
                truckPhotos.add(truckPhotoRepository.save(new TruckPhotosEntity(
                        attachService.findById(attach.getId()), attach.getStatus()
                )));
                attachIds.add(attach.getId()); // Add to the set to track uniqueness
            }
        });
        System.out.println("saved truck photo");


        truckEntity.setTruckNumber(truck.getTruckNumber());
        truckEntity.setTruckPhotos(truckPhotos);
        truckEntity.setTruckActions(truckActions);
        return truckRepository.save(truckEntity);
    }

    public List<ActionResponse> getNotSentData() {
        List<ActionResponse> result = new ArrayList<>();
        List<TruckEntity> notSentData = truckRepository.findTop10ByIsSentToCloud(false);
        for (TruckEntity truck : notSentData) {
            ActionResponse actionResponse = new ActionResponse();
            List<Long> attachIds = truck.getTruckPhotos().stream().map(
                    act -> act.getTruckPhoto().getId()
            ).toList();
            actionResponse.setId(truck.getId());
            actionResponse.setAttachIds(attachIds);
            actionResponse.setTruckNumber(truck.getTruckNumber());
            for (TruckActionEntity action : truck.getTruckActions()) {
                switch (action.getAction()) {
                    case ENTRANCE, MANUAL_ENTRANCE -> {
                        actionResponse.setEnteredStatus(action.getAction());
                        actionResponse.setEnteredAt(action.getCreatedAt());
                        actionResponse.setEnteredWeight(action.getWeight());
                        actionResponse.setEntranceConfirmedBy(action.getOnDuty().getPhoneNumber());
                    }
                    case EXIT, MANUAL_EXIT -> {
                        actionResponse.setExitedStatus(action.getAction());
                        actionResponse.setExitedAt(action.getCreatedAt());
                        actionResponse.setExitedWeight(action.getWeight());
                        actionResponse.setExitConfirmedBy(action.getOnDuty().getPhoneNumber());
                    }
                }
            }
            actionResponse.setIdOnServer(truck.getIdOnServer());
            result.add(actionResponse);
        }
        return result;
    }

    public void dataSent(List<ActionResponse> notSentData, Map<Long, Long> truckMap) {
        if (truckMap == null || truckMap.isEmpty()) {
            return;
        }
        notSentData.forEach(truck -> {
            TruckEntity entity = truckRepository.findById(truck.getId()).orElseThrow(() -> new RuntimeException(truck.getId() + " is not found from database"));
            entity.setIsSentToCloud(true);
            entity.setIdOnServer(truckMap.get(entity.getId()));
            truckRepository.save(entity);
        });
    }

    public TableViewData findLastRecord() {
        TruckEntity truck = truckRepository.findTopByOrderByIdDesc().orElse(null);
        if (truck == null) {
            System.err.println("Unable to find last record");
            return null;
        }
        return entityToTableViewData(truck);
    }

    public List<TruckEntity> findAll() {
        return truckRepository.findAllByIsDeleted(false, Sort.by(Sort.Direction.DESC, "id"));
    }

    public TableViewData entityToTableViewData(TruckEntity entity) {
        TableViewData data = new TableViewData();
        data.setId(entity.getId());
        double enteredWeight = 0.0;
        double exitedWeight = 0.0;
        for (TruckActionEntity action : entity.getTruckActions()) {
            switch (action.getAction()) {
                case ENTRANCE, MANUAL_ENTRANCE -> {
                    data.setEnteredTruckNumber(entity.getTruckNumber());
                    data.setEnteredDate(getDate(action.getCreatedAt()));
                    data.setEnteredTime(getTime(action.getCreatedAt()));
                    data.setEnteredWeight(action.getWeight());
                    enteredWeight = action.getWeight();
                    if (action.getOnDuty() != null) {
                        data.setEnteredOnDuty(action.getOnDuty().getPhoneNumber());
                    } else {
                        data.setEnteredOnDuty("Unknown"); // Or handle this case differently as needed
                    }
                }
                case MANUAL_EXIT, EXIT -> {
                    data.setExitedTruckNumber(entity.getTruckNumber());
                    data.setExitedDate(getDate(action.getCreatedAt()));
                    data.setExitedTime(getTime(action.getCreatedAt()));
                    data.setExitedWeight(action.getWeight());
                    exitedWeight = action.getWeight();
                    if (action.getOnDuty() != null) {
                        data.setExitedOnDuty(action.getOnDuty().getPhoneNumber());
                    } else {
                        data.setExitedOnDuty("Unknown"); // Or handle this case differently as needed
                    }
                }
            }
        }
        CargoEntity cargo = cargoService.findByTruckId(entity.getId());
        if (cargo == null) return data;
        switch (cargo.getCargoStatus()) {
            case PICKUP -> data.setPickupWeight(String.valueOf(cargo.getNetWeight()));
            case DROP -> data.setDropWeight(String.valueOf(cargo.getNetWeight()));
        }
        data.setMinWeight(String.valueOf(Math.min(enteredWeight, exitedWeight)));
        data.setMaxWeight(String.valueOf(Math.max(enteredWeight, exitedWeight)));
        return data;
    }

    public String getTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.getHour() + ":" + dateTime.getMinute();
    }

    public String getDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.getDayOfMonth() + "." + dateTime.getMonth().getValue() + "." + dateTime.getYear();
    }

    public TruckEntity save(TruckEntity truck) {
        return truckRepository.save(truck);
    }

    public TruckEntity findById(Long id) {
        return truckRepository.findById(id).orElse(null);
    }

    public TruckEntity findByTruckPhoto(TruckPhotosEntity truckPhotos) {
        return truckRepository.findByTruckPhotosContains(truckPhotos).orElse(null);
    }

    @Transactional
    public void saveTruck(TruckResponse currentTruck, Integer id, AttachResponse attach) {
        if (id == 1) {
            currentTruckEntity = new TruckEntity();
            currentTruckEntity.setTruckNumber(currentTruck.getTruckNumber());
            List<TruckPhotosEntity> truckPhotos = new ArrayList<>();
            TruckPhotosEntity entity = truckPhotoRepository.save(
                    new TruckPhotosEntity(attachService.findById(attach.getId()), AttachStatus.ENTRANCE_PHOTO)
            );
            truckPhotos.add(entity);
//            for (AttachIdWithStatus attach : currentTruck.getAttaches()) {
//                if (attach.getStatus().equals(AttachStatus.ENTRANCE_PHOTO)) {
//                    break;
//                }
//            }
            currentTruckEntity.setTruckPhotos(truckPhotos);
            currentTruckEntity.setIsFinished(false);
            truckRepository.save(currentTruckEntity);
        } else {
//            1 ta malumot kelmasligi mumkin
//            currentTruckEntity = truckRepository.findByTruckNumberAndIsFinished(currentTruck.getTruckNumber(), false).orElse(null);
//            if (currentTruckEntity == null)
//                throw new RuntimeException("Truck not found with truck number: " + currentTruck.getTruckNumber());
            currentTruckEntity = truckRepository.findByTruckNumberAndIsFinishedAndIsDeletedOrderByCreatedAt(currentTruck.getTruckNumber(), false, false).get(0);
            currentTruckEntity.getTruckPhotos().removeIf(photo -> photo.getAttachStatus() == AttachStatus.EXIT_PHOTO);
            List<TruckPhotosEntity> truckPhotos = new ArrayList<>();
            TruckPhotosEntity entity = truckPhotoRepository.save(
                    new TruckPhotosEntity(attachService.findById(attach.getId()), AttachStatus.EXIT_PHOTO)
            );
            truckPhotos.add(entity);
//            for (AttachIdWithStatus attach : currentTruck.getAttaches()) {
//                if (attach.getStatus().equals(AttachStatus.EXIT_PHOTO)) {
//                    break;
//                }
//            }
            currentTruckEntity.getTruckPhotos().addAll(truckPhotos);

            truckRepository.save(currentTruckEntity);
        }
    }


    public TruckEntity saveCurrentTruck(TruckResponse currentTruck, boolean isFinished) {
        currentTruckEntity.setIsFinished(isFinished);
        currentTruckEntity.setIsSentToCloud(false);
        try {
            currentTruckEntity = truckRepository.save(currentTruckEntity);
        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00015: (" + getClass().getName() + ") " + e.getMessage()));
            System.err.println(e.getMessage());
        }
        return currentTruckEntity;
    }

    @Override
    public TruckResponse entityToResponse(TruckEntity entity) {
        return null;
    }

    @Override
    public TruckEntity requestToEntity(TruckRequest request) {
        return null;
    }

    public TableViewData getTableViewData(TruckResponse truckResponse) {
        Double enteredWeight = truckResponse.getEnteredWeight();
        Double exitedWeight = truckResponse.getExitedWeight();
        return new TableViewData(
                truckResponse.getId(), truckResponse.getTruckNumber(),
                getDate(truckResponse.getEnteredAt()), getTime(truckResponse.getEnteredAt()), enteredWeight,
                truckResponse.getEntranceConfirmedBy(), truckResponse.getTruckNumber(), getDate(truckResponse.getExitedAt()),
                getTime(truckResponse.getExitedAt()), exitedWeight, truckResponse.getExitConfirmedBy(),
                String.valueOf(Math.min(enteredWeight, exitedWeight)),
                String.valueOf(Math.max(enteredWeight, exitedWeight)),
                enteredWeight > exitedWeight ? String.valueOf(enteredWeight - exitedWeight) : "0.0",
                enteredWeight <= exitedWeight ? String.valueOf(exitedWeight - enteredWeight) : "0.0"
        );
    }

    @Transactional
    public void saveTruckAttaches(TruckResponse currentTruck, AttachResponse response, AttachStatus attachStatus) {
//        currentTruckEntity = truckRepository.findByTruckNumberAndIsFinished(currentTruck.getTruckNumber(), false)
//                .orElse(new TruckEntity());
//        currentTruckEntity.setTruckNumber(currentTruck.getTruckNumber());
        List<TruckPhotosEntity> truckPhotos = currentTruckEntity.getTruckPhotos();
        truckPhotos.removeIf(photo -> photo.getAttachStatus().equals(attachStatus));
        AttachEntity attach = attachService.findById(response.getId());
        TruckPhotosEntity photosEntity = new TruckPhotosEntity(
                attach, attachStatus
        );
        truckPhotoRepository.save(photosEntity);
        truckPhotos.add(photosEntity);
        currentTruckEntity.setTruckPhotos(truckPhotos);
        truckRepository.save(currentTruckEntity);

    }

    public void saveTruckEnteredActions(TruckResponse currentTruck) {
        currentTruckEntity.getTruckActions().removeIf(action -> action.getAction() == currentTruck.getEnteredStatus());
        TruckActionEntity truckActionEntity = new TruckActionEntity();
        truckActionEntity.setCreatedAt(currentTruck.getEnteredAt());
        truckActionEntity.setWeight(currentTruck.getEnteredWeight());
        truckActionEntity.setAction(currentTruck.getEnteredStatus() == null ? TruckAction.NO_ACTION : currentTruck.getEnteredStatus());
        truckActionEntity.setOnDuty(Instances.currentUser);
        truckActionRepository.save(truckActionEntity);
        currentTruckEntity.getTruckActions().add(truckActionEntity);
        currentTruckEntity.setIsSentToCloud(false);
        currentTruckEntity.setNextEntranceTime(currentTruck.getEnteredAt().plusMinutes(5));
        truckRepository.save(currentTruckEntity);
    }

    public void saveTruckExitedAction(TruckResponse currentTruck) {
        currentTruckEntity.getTruckActions().removeIf(action -> action.getAction() == currentTruck.getExitedStatus());
        TruckActionEntity truckActionEntity = new TruckActionEntity();
        truckActionEntity.setCreatedAt(currentTruck.getExitedAt());
        truckActionEntity.setWeight(currentTruck.getExitedWeight());
        truckActionEntity.setAction(currentTruck.getExitedStatus() == null ? TruckAction.NO_ACTION : currentTruck.getExitedStatus());
        truckActionEntity.setOnDuty(Instances.currentUser);
        truckActionRepository.save(truckActionEntity);
        currentTruckEntity.getTruckActions().add(truckActionEntity);
        currentTruckEntity.setIsSentToCloud(false);
        currentTruckEntity.setNextEntranceTime(currentTruck.getExitedAt().plusMinutes(5));
        truckRepository.save(currentTruckEntity);
    }

    public List<TruckEntity> findNotSentDataToMyCoal() {
        return truckRepository.findByIsSentToMyCoalAndIsFinished(false, true);
    }

    public boolean isNumberExists(String truckNumber) {
        if (truckNumber == null) return false;
        return isTruckNumberExists(truckNumber);
    }

    public boolean isValidTruckNumber(String truckNumber) {
        return regexChecker(truckNumber, regexPattern);
    }

    public List<String> getNotFinishedTrucks() {
        return truckRepository.findByIsFinishedAndIsDeleted(false, false).stream().map(TruckEntity::getTruckNumber).toList();
    }

    public boolean isEntranceAvailableForCamera1(String truckNumber) {
        if (!isTruckNumberExists(truckNumber)) {
            return true;
        }
        if (truckRepository.existsByTruckNumberAndIsFinishedAndIsDeleted(truckNumber, false, false)) {
            return false;
        }
        List<TruckEntity> list = truckRepository.findByTruckNumberAndIsFinishedAndIsDeletedOrderByCreatedAtDesc(truckNumber, true, false);
        if (list.isEmpty()) return false;
        LocalDateTime nextEntranceTime = list.get(0).getNextEntranceTime();
        return nextEntranceTime.isBefore(LocalDateTime.now());
    }

    private boolean isTruckNumberExists(String truckNumber) {
        return truckRepository.existsByTruckNumberAndIsDeleted(truckNumber, false);
    }

    public boolean isNotFinishedTrucksExists() {
        return truckRepository.existsByIsFinishedFalseAndIsDeletedFalse();
    }


    public boolean isEntranceAvailableForCamera2(String truckNumber) {
        if (!isTruckNumberExists(truckNumber)) {
            return false;
        }
        List<TruckEntity> list = truckRepository.findByTruckNumberAndIsFinishedAndIsDeletedOrderByCreatedAtDesc(truckNumber, false, false);
        if (list.isEmpty()) return false;
        LocalDateTime nextEntranceTime = list.get(0).getNextEntranceTime();
        return nextEntranceTime.isBefore(LocalDateTime.now());
    }

    public boolean isStandard(String truckNumber) {
        return regexChecker(truckNumber, regexStandard);
    }

    private boolean regexChecker(String str, String regex) {
        if (str == null) return false;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    public List<TruckEntity> filterWithDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
        }

        if (startDate == null) {
            return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(endDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        } else if (endDate == null) {
            return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(startDate.atStartOfDay(), startDate.atTime(LocalTime.MAX));
        }

        if (startDate.isBefore(endDate)) {
            return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        }

        if (startDate.equals(endDate)) {
            return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        }

        return truckRepository.findAllByIsDeletedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));
    }
}
