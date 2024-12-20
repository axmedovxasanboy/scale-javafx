package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.CargoEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TableService {

    @Autowired
    @Lazy
    private CargoService cargoService;

    public TableViewData entityToTableData(TruckEntity truckEntity) {
        TableViewData data = new TableViewData();
        data.setId(truckEntity.getId());
        double enteredWeight = 0.0;
        double exitedWeight = 0.0;

        for (TruckActionEntity action : truckEntity.getTruckActions()) {
            if (action.getAction() == null || action.getAction() == TruckAction.NO_ACTION) {
                continue; // Skip invalid or no-action entries
            }
            switch (action.getAction()) {
                case ENTRANCE, MANUAL_ENTRANCE -> {
                    data.setEnteredTruckNumber(truckEntity.getTruckNumber());
                    data.setEnteredDate(getDate(action.getCreatedAt()));
                    data.setEnteredWeight(action.getWeight() == null ? 0.0 : action.getWeight());
                    enteredWeight = action.getWeight() == null ? 0.0 : action.getWeight();
                    data.setEnteredTime(getTime(action.getCreatedAt()));
                    data.setEnteredOnDuty(action.getOnDuty() == null ? "unknown" : action.getOnDuty().getPhoneNumber());
                }
                case EXIT, MANUAL_EXIT -> {
                    data.setExitedTruckNumber(truckEntity.getTruckNumber());
                    data.setExitedDate(getDate(action.getCreatedAt()));
                    data.setExitedWeight(action.getWeight() == null ? 0.0 : action.getWeight());
                    exitedWeight = action.getWeight() == null ? 0.0 : action.getWeight();
                    data.setExitedTime(getTime(action.getCreatedAt()));
                    data.setExitedOnDuty(action.getOnDuty().getPhoneNumber());
                }
                default -> {

                }
            }
        }
        data.setProductType(truckEntity.getProducts() == null ? "" : truckEntity.getProducts().getName());
        CargoEntity cargo = cargoService.findByTruckId(truckEntity.getId());
        if (cargo == null) return data;
        switch (cargo.getCargoStatus()) {
            case PICKUP -> data.setPickupWeight(String.valueOf(cargo.getNetWeight()));
            case DROP -> data.setDropWeight(String.valueOf(cargo.getNetWeight()));
        }
        data.setMinWeight(String.valueOf(Math.min(enteredWeight, exitedWeight)));
        data.setMaxWeight(String.valueOf(Math.max(enteredWeight, exitedWeight)));
        return data;
    }

    private String getTime(LocalDateTime dateTime) {
        return dateTime.getHour() + ":" + dateTime.getMinute();
    }

    private String getDate(LocalDateTime dateTime) {
        return dateTime.getDayOfMonth() + "." + dateTime.getMonthValue() + "." + dateTime.getYear();
    }
}
