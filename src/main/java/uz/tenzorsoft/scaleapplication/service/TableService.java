package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TableService {

    public TableViewData entityToTableData(TruckEntity truckEntity) {
        TableViewData data = new TableViewData();
        data.setId(truckEntity.getId());

        for (TruckActionEntity action : truckEntity.getTruckActions()) {
            switch (action.getAction()) {
                case ENTRANCE, MANUAL_ENTRANCE -> {
                    data.setEnteredTruckNumber(truckEntity.getTruckNumber());
                    data.setEnteredDate(getDate(action.getCreatedAt()));
                    data.setEnteredWeight(action.getWeight());
                    data.setEnteredTime(getTime(action.getCreatedAt()));
                    data.setEnteredOnDuty(action.getOnDuty().getPhoneNumber());
                }
                case EXIT, MANUAL_EXIT -> {
                    data.setExitedTruckNumber(truckEntity.getTruckNumber());
                    data.setExitedDate(getDate(action.getCreatedAt()));
                    data.setExitedWeight(action.getWeight());
                    data.setExitedTime(getTime(action.getCreatedAt()));
                    data.setExitedOnDuty(action.getOnDuty().getPhoneNumber());
                }
            }
        }
        return data;
    }

    private String getTime(LocalDateTime dateTime) {
        return dateTime.getHour() + ":" + dateTime.getMinute();
    }

    private String getDate(LocalDateTime dateTime) {
        return dateTime.getDayOfMonth() + "." + dateTime.getMonthValue() + "." + dateTime.getYear();
    }
}
