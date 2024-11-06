package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.request.TruckRequest;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.repository.AttachRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckPhotoRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TruckService implements BaseService<TruckEntity, TruckResponse, TruckRequest> {


    private final TruckRepository truckRepository;
    private final AttachRepository attachRepository;
    private final TruckPhotoRepository truckPhotoRepository;

    public List<TableViewData> getTruckData() {
        List<TruckEntity> all = truckRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        List<TableViewData> data = new ArrayList<>();

        for (TruckEntity truck : all) {
            TableViewData tableData = new TableViewData();
            tableData.setId(truck.getId());

            TruckActionEntity truckAction = truck.getTruckAction();
            switch (truckAction.getAction()) {
                case ENTRANCE, MANUAL_ENTRANCE -> {
                    tableData.setEnteredTruckNumber(truck.getTruckNumber());
                    tableData.setEnteredDate(getDate(truckAction.getCreatedAt()));
                    tableData.setEnteredTime(getTime(truckAction.getCreatedAt()));
                    tableData.setEnteredWeight(truckAction.getWeight());
                    tableData.setEnteredOnDuty(truckAction.getOnDuty().getUsername());
                }
                case MANUAL_EXIT, EXIT -> {
                    tableData.setExitedTruckNumber(truck.getTruckNumber());
                    tableData.setExitedDate(getDate(truckAction.getCreatedAt()));
                    tableData.setExitedTime(getTime(truckAction.getCreatedAt()));
                    tableData.setExitedWeight(truckAction.getWeight());
                    tableData.setExitedOnDuty(truckAction.getOnDuty().getUsername());
                }
            }
            data.add(tableData);
        }
        return data;
    }

    public String getTime(LocalDateTime dateTime) {
        return dateTime.getHour() + ":" + dateTime.getMinute();
    }

    public String getDate(LocalDateTime dateTime) {
        return dateTime.getDayOfMonth() + "." + dateTime.getMonth().getValue() + "." + dateTime.getYear();
    }

    @Override
    public TruckResponse entityToResponse(TruckEntity entity) {
        return null;
    }

    @Override
    public TruckEntity requestToEntity(TruckRequest request) {
        return null;
    }

    public TruckEntity save(TruckEntity truck) {
        return truckRepository.save(truck);
    }

    public TruckEntity findEnteredByTruckNumber(String truckNumber) {
        return null;
    }

    public List<TruckEntity> findEnteredTrucks() {
        return truckRepository.findEnteredTrucks(TruckAction.ENTRANCE);
    }

    public TruckEntity create(Long attachId, String truckNumber) {
        TruckEntity truckEntity = new TruckEntity();
        truckEntity.setTruckNumber(truckNumber);

        TruckActionEntity truckAction = new TruckActionEntity();
        truckAction.setAction(TruckAction.ENTRANCE); // Set action as ENTRANCE
        truckEntity.setTruckAction(truckAction);

        AttachEntity attachEntity = attachRepository.findById(attachId).orElseThrow(() -> new RuntimeException("Attach not found"));
        TruckPhotosEntity truckPhotoEntity = new TruckPhotosEntity();
        truckPhotoEntity.setTruckPhoto(attachEntity);
        truckPhotoEntity.setAttachStatus(AttachStatus.ENTRANCE_PHOTO);

        truckPhotoRepository.save(truckPhotoEntity);
        truckEntity.setTruckPhotos(Collections.singletonList(truckPhotoEntity));

        return truckRepository.save(truckEntity);
    }

    public TruckEntity findByTruckNumber(String truckNumber, TruckAction truckAction) {
        return truckRepository.findByTruckNumberAndAction(truckNumber, truckAction);
    }

    public List<TableViewData> getNotSentData() {
        List<TableViewData> result = new ArrayList<>();
        List<TruckEntity> notSentData = truckRepository.findByIsSent(false);
        for (TruckEntity truck : notSentData) {
            if (truck.getTruckAction() == null)
                continue;
            result.add(new TableViewData()
            );
        }
        return result;
    }

    public void dataSent(List<TableViewData> notSentData, Map<Long, Long> truckMap) {
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
}
