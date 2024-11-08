package uz.tenzorsoft.scaleapplication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.request.TruckRequest;
import uz.tenzorsoft.scaleapplication.domain.response.AttachIdWithStatus;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.ActionResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.mycoal.MyCoalData;
import uz.tenzorsoft.scaleapplication.repository.TruckActionRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckPhotoRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TruckService implements BaseService<TruckEntity, TruckResponse, TruckRequest> {


    private final TruckRepository truckRepository;
    private final TruckActionRepository truckActionRepository;
    private final TruckPhotoRepository truckPhotoRepository;
    private final UserService userService;
    @Lazy
    @Autowired
    private AttachService attachService;

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
                    truck.getExitedWeight(), TruckAction.EXIT, userService.findByPhoneNumber(truck.getExitConfirmedBy())
            );
            truckEntity.getTruckActions().add(truckAction);
            return truckRepository.save(truckEntity);
        }

        List<TruckActionEntity> truckActions = new ArrayList<>();
        truckActions.add(new TruckActionEntity(truck.getEnteredWeight(),
                TruckAction.ENTRANCE, userService.findByPhoneNumber(truck.getEntranceConfirmedBy()))
        );
        truckActions.add(new TruckActionEntity(truck.getExitedWeight(),
                TruckAction.EXIT, userService.findByPhoneNumber(truck.getEntranceConfirmedBy()))
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
        List<TruckEntity> notSentData = truckRepository.findByIsSent(false);
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
        }
        return result;
    }

    public void dataSent(List<ActionResponse> notSentData, Map<Long, Long> truckMap) {
        if (truckMap == null || truckMap.isEmpty()) {
            return;
        }
        notSentData.forEach(truck -> {
            TruckEntity entity = truckRepository.findById(truck.getId()).orElseThrow(() -> new RuntimeException(truck.getId() + " is not found from database"));
            entity.setIsSent(true);
            entity.setIdOnServer(truckMap.get(entity.getId()));
            truckRepository.save(entity);
        });
    }

    public TableViewData findLastRecord() {
        TruckEntity truck = truckRepository.findTopByOrderByIdDesc().orElse(null);
        if (truck == null) {
            System.err.println("Unable to find");
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
        for (TruckActionEntity action : entity.getTruckActions()) {
            switch (action.getAction()) {
                case ENTRANCE, MANUAL_ENTRANCE -> {
                    data.setEnteredTruckNumber(entity.getTruckNumber());
                    data.setEnteredDate(getDate(action.getCreatedAt()));
                    data.setEnteredTime(getTime(action.getCreatedAt()));
                    data.setEnteredWeight(action.getWeight());
                    if (action.getOnDuty() != null) {
                        data.setEnteredOnDuty(action.getOnDuty().getUsername());
                    } else {
                        data.setEnteredOnDuty("Unknown"); // Or handle this case differently as needed
                    }
                }
                case MANUAL_EXIT, EXIT -> {
                    data.setExitedTruckNumber(entity.getTruckNumber());
                    data.setExitedDate(getDate(action.getCreatedAt()));
                    data.setExitedTime(getTime(action.getCreatedAt()));
                    data.setExitedWeight(action.getWeight());
                    if (action.getOnDuty() != null) {
                        data.setExitedOnDuty(action.getOnDuty().getUsername());
                    } else {
                        data.setExitedOnDuty("Unknown"); // Or handle this case differently as needed
                    }
                }
            }
        }
        return data;
    }

    public String getTime(LocalDateTime dateTime) {
        if(dateTime == null) return null;
        return dateTime.getHour() + ":" + dateTime.getMinute();
    }

    public String getDate(LocalDateTime dateTime) {
        if(dateTime == null) return null;
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

    public List<MyCoalData> getMyCoalData() {
        return null;
    }

    public void saveTruck(TruckResponse currentTruck, Integer id) {
        if (id == 1) {
            TruckEntity truckEntity = new TruckEntity();
            truckEntity.setTruckNumber(currentTruck.getTruckNumber());
            List<TruckPhotosEntity> truckPhotos = new ArrayList<>();
            for (AttachIdWithStatus attach : currentTruck.getAttaches()) {
                truckPhotos.add(truckPhotoRepository.save(
                        new TruckPhotosEntity(attachService.findById(attach.getId()), attach.getStatus())
                ));
            }
            truckEntity.setTruckPhotos(truckPhotos);
            truckEntity.setIsFinished(false);
            truckRepository.save(truckEntity);
        } else {
            TruckEntity truck = truckRepository.findByTruckNumberAndIsFinishedOrderByCreatedAt(currentTruck.getTruckNumber(), false).get(0);
            if (truck == null)
                throw new RuntimeException("Truck not found with truck number: " + currentTruck.getTruckNumber());
            List<TruckPhotosEntity> truckPhotos = new ArrayList<>();
            for (AttachIdWithStatus attach : currentTruck.getAttaches()) {
                truckPhotos.add(truckPhotoRepository.save(
                        new TruckPhotosEntity(attachService.findById(attach.getId()), attach.getStatus())
                ));
            }
            truck.getTruckPhotos().addAll(truckPhotos);

            truckRepository.save(truck);
        }
    }

    @Transactional
    public TruckEntity saveCurrentTruck(TruckResponse currentTruck, boolean isFinished) {
        TruckEntity truck = truckRepository.findByTruckNumberAndIsFinishedOrderByCreatedAt(currentTruck.getTruckNumber(), false).get(0);

        List<AttachIdWithStatus> attaches = currentTruck.getAttaches();
        List<Long> attachIds = new ArrayList<>(attaches.stream().map(AttachIdWithStatus::getId).toList());
        List<Long> truckAttachIds = truck.getTruckPhotos().stream().map(
                photo -> photo.getTruckPhoto().getId()
        ).toList();

        attachIds.removeAll(truckAttachIds);

        for (Long id : attachIds) {
            attaches.removeIf(attach -> attach.getId().equals(id));
        }

        List<TruckPhotosEntity> truckPhotos = new ArrayList<>();

        for (AttachIdWithStatus attach : attaches) {
            try {
                truckPhotos.add(truckPhotoRepository.save(
                        new TruckPhotosEntity(attachService.findById(attach.getId()), attach.getStatus())
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        truck.getTruckPhotos().addAll(truckPhotos);
        truck.setIsFinished(isFinished);
        truckRepository.save(truck);
        return truck;
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
        return new TableViewData(
                truckResponse.getId(), truckResponse.getTruckNumber(),
                getDate(truckResponse.getEnteredAt()), getTime(truckResponse.getEnteredAt()), truckResponse.getEnteredWeight(),
                truckResponse.getEntranceConfirmedBy(), truckResponse.getTruckNumber(), getDate(truckResponse.getExitedAt()),
                getTime(truckResponse.getExitedAt()), truckResponse.getExitedWeight(), truckResponse.getExitConfirmedBy()
        );
    }
}
