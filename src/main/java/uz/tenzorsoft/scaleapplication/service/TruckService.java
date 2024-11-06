package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
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
        return null;
    }

    public TruckEntity create(Long attachId, String truckNumber) {
        return null;
    }

    public TruckEntity findByTruckNumber(String truckNumber, TruckAction truckAction) {
        return null;
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
