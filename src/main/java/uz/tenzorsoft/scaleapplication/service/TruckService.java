package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.request.TruckRequest;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TruckService implements BaseService<TruckEntity, TruckResponse, TruckRequest> {


    private final TruckRepository truckRepository;
    private final UserService userService;
    private final AttachService attachService;

    public List<TableViewData> getTruckData() {
        List<TruckEntity> all = truckRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<TableViewData> data = new ArrayList<>();
        for (TruckEntity truck : all) {
            data.add(entityToTableViewData(truck));
        }
        return data;
    }

    public TruckEntity save(TruckResponse truck) {
        TruckEntity truckEntity = truckRepository.findTruckWithEntranceNoExit(truck.getTruckNumber()).orElse(new TruckEntity());

        if (truckEntity.getId() != null) {
            TruckActionEntity truckAction = new TruckActionEntity(
                    truck.getExitedWeight(), TruckAction.EXIT, userService.findByPhoneNumber(truck.getExitConfirmedBy())
            );
            truckEntity.getTruckActions().add(truckAction);
            return save(truckEntity);
        }

        List<TruckActionEntity> truckActions = new ArrayList<>();
        truckActions.add(new TruckActionEntity(truck.getEnteredWeight(),
                TruckAction.ENTRANCE, userService.findByPhoneNumber(truck.getEntranceConfirmedBy()))
        );
        truckActions.add(new TruckActionEntity(truck.getExitedWeight(),
                TruckAction.EXIT, userService.findByPhoneNumber(truck.getEntranceConfirmedBy()))
        );

        List<TruckPhotosEntity> truckPhotos = new ArrayList<>();

        truck.getAttaches().forEach(attach -> {
            truckPhotos.add(new TruckPhotosEntity(
                    attachService.findById(attach.getId()), attach.getStatus()
            ));
        });


        truckEntity.setTruckNumber(truck.getTruckNumber());
        truckEntity.setTruckPhotos(truckPhotos);
        truckEntity.setTruckActions(truckActions);
        return save(truckEntity);
    }

    public List<TableViewData> getNotSentData() {
        List<TableViewData> result = new ArrayList<>();
        List<TruckEntity> notSentData = truckRepository.findByIsSent(false);
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
                    data.setEnteredOnDuty(action.getOnDuty().getUsername());
                }
                case MANUAL_EXIT, EXIT -> {
                    data.setExitedTruckNumber(entity.getTruckNumber());
                    data.setExitedDate(getDate(action.getCreatedAt()));
                    data.setExitedTime(getTime(action.getCreatedAt()));
                    data.setExitedWeight(action.getWeight());
                    data.setExitedOnDuty(action.getOnDuty().getUsername());
                }
            }
        }
        return data;
    }

    public String getTime(LocalDateTime dateTime) {
        return dateTime.getHour() + ":" + dateTime.getMinute();
    }

    public String getDate(LocalDateTime dateTime) {
        return dateTime.getDayOfMonth() + "." + dateTime.getMonth().getValue() + "." + dateTime.getYear();
    }

    public TruckEntity save(TruckEntity truck) {
        return truckRepository.save(truck);
    }

    @Override
    public TruckResponse entityToResponse(TruckEntity entity) {
        return null;
    }

    @Override
    public TruckEntity requestToEntity(TruckRequest request) {
        return null;
    }

    public TruckEntity findById(Long id) {
        return truckRepository.findById(id).orElse(null);
    }
}
