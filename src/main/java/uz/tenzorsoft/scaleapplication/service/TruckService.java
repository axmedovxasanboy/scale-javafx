package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.request.TruckRequest;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
}
