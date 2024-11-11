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
import uz.tenzorsoft.scaleapplication.domain.entity.CargoEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.request.TruckRequest;
import uz.tenzorsoft.scaleapplication.domain.response.AttachIdWithStatus;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.ActionResponse;
import uz.tenzorsoft.scaleapplication.repository.TruckActionRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckPhotoRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;

import java.time.LocalDateTime;
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
    @Autowired
    private CargoService cargoService;

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
        List<TruckEntity> notSentData = truckRepository.findByIsSentToCloud(false);
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
        return truckRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
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

    public void saveTruck(TruckResponse currentTruck, Integer id) {
        if (id == 1) {
            currentTruckEntity = new TruckEntity();
            currentTruckEntity.setTruckNumber(currentTruck.getTruckNumber());
            List<TruckPhotosEntity> truckPhotos = new ArrayList<>();
            for (AttachIdWithStatus attach : currentTruck.getAttaches()) {
                truckPhotos.add(truckPhotoRepository.save(
                        new TruckPhotosEntity(attachService.findById(attach.getId()), attach.getStatus())
                ));
            }
            currentTruckEntity.setTruckPhotos(truckPhotos);
            currentTruckEntity.setIsFinished(false);
            truckRepository.save(currentTruckEntity);
        } else {
            currentTruckEntity = truckRepository.findByTruckNumberAndIsFinishedOrderByCreatedAt(currentTruck.getTruckNumber(), false).get(0);
            if (currentTruckEntity == null)
                throw new RuntimeException("Truck not found with truck number: " + currentTruck.getTruckNumber());
            List<TruckPhotosEntity> truckPhotos = new ArrayList<>();
            for (AttachIdWithStatus attach : currentTruck.getAttaches()) {
                truckPhotos.add(truckPhotoRepository.save(
                        new TruckPhotosEntity(attachService.findById(attach.getId()), attach.getStatus())
                ));
            }
            currentTruckEntity.getTruckPhotos().addAll(truckPhotos);

            truckRepository.save(currentTruckEntity);
        }
    }

    @Transactional
    public TruckEntity saveCurrentTruck(TruckResponse currentTruck, boolean isFinished) {
        currentTruckEntity.setIsFinished(isFinished);
        currentTruckEntity.setIsSentToCloud(false);
        try {
            currentTruckEntity = truckRepository.save(currentTruckEntity);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
    public void saveTruckAttaches(TruckResponse currentTruck, Long id) {
        currentTruckEntity = truckRepository.findByTruckNumberAndIsFinished(currentTruck.getTruckNumber(), false)
                .orElse(new TruckEntity());
        currentTruckEntity.setTruckNumber(currentTruck.getTruckNumber());
        List<TruckPhotosEntity> truckPhotos = currentTruckEntity.getTruckPhotos();
        TruckPhotosEntity photo = new TruckPhotosEntity(
                attachService.findById(id), AttachStatus.ENTRANCE_CARGO_PHOTO
        );
        truckPhotoRepository.save(photo);
        truckPhotos.add(photo);
        currentTruckEntity = truckRepository.save(currentTruckEntity);
    }

    public void saveTruckEnteredActions(TruckResponse currentTruck) {
        TruckActionEntity truckActionEntity = new TruckActionEntity();
        truckActionEntity.setCreatedAt(currentTruck.getEnteredAt());
        truckActionEntity.setWeight(currentTruck.getEnteredWeight());
        truckActionEntity.setAction(TruckAction.ENTRANCE);
        truckActionEntity.setOnDuty(Instances.currentUser);
        truckActionRepository.save(truckActionEntity);
        currentTruckEntity.getTruckActions().add(truckActionEntity);
        currentTruckEntity.setIsSentToCloud(false);
        currentTruckEntity.setNextEntranceTime(currentTruck.getEnteredAt().plusMinutes(1));
        truckRepository.save(currentTruckEntity);
    }

    public void saveTruckExitedAction(TruckResponse currentTruck) {
        TruckActionEntity truckActionEntity = new TruckActionEntity();
        truckActionEntity.setCreatedAt(currentTruck.getExitedAt());
        truckActionEntity.setWeight(currentTruck.getExitedWeight());
        truckActionEntity.setAction(TruckAction.EXIT);
        truckActionEntity.setOnDuty(Instances.currentUser);
        truckActionRepository.save(truckActionEntity);
        currentTruckEntity.getTruckActions().add(truckActionEntity);
        currentTruckEntity.setIsSentToCloud(false);
        currentTruckEntity.setNextEntranceTime(currentTruck.getExitedAt().plusMinutes(1));
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
        return truckRepository.findByIsFinished(false).stream().map(TruckEntity::getTruckNumber).toList();
    }

    public boolean isEntranceAvailableForCamera1(String truckNumber) {
        if (!isTruckNumberExists(truckNumber)) {
            return true;
        }
        return !truckRepository.existsByTruckNumberAndNextEntranceTimeIsBeforeAndIsFinishedFalse(truckNumber, LocalDateTime.now());
    }

    private boolean isTruckNumberExists(String truckNumber) {
        return truckRepository.existsByTruckNumber(truckNumber);
    }

    public boolean isNotFinishedTrucksExists() {
        return truckRepository.existsByIsFinishedFalse();
    }


    public boolean isEntranceAvailableForCamera2(String truckNumber) {
        if (!isTruckNumberExists(truckNumber)) {
            return false;
        }
        return truckRepository.existsByTruckNumberAndNextEntranceTimeIsBeforeAndIsFinishedFalse(truckNumber, LocalDateTime.now());

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
}
