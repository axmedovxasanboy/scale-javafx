package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.CargoEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.CargoStatus;
import uz.tenzorsoft.scaleapplication.domain.response.CargoResponse;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.WeighingResponse;
import uz.tenzorsoft.scaleapplication.repository.CargoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uz.tenzorsoft.scaleapplication.domain.Instances.truckNumber;

@Service
@RequiredArgsConstructor

public class CargoService {

    private final CargoRepository cargoRepository;


    public void saveCargo(TruckEntity truckEntity) {
        CargoEntity cargoEntity = new CargoEntity();
        cargoEntity.setTruck(truckEntity);
        double enteredWeight = 0.0;
        double exitedWeight = 0.0;
        double netWeight = 0.0;

        List<TruckActionEntity> truckAction = truckEntity.getTruckActions();
        for (TruckActionEntity action : truckAction) {
            switch (action.getAction()) {
                case ENTRANCE, MANUAL_ENTRANCE -> {
                    enteredWeight = action.getWeight();
                }
                case EXIT, MANUAL_EXIT -> {
                    exitedWeight = action.getWeight();
                }
            }
        }

        netWeight = exitedWeight - enteredWeight;
        System.out.println("enteredWeight = " + enteredWeight);
        System.out.println("exitedWeight = " + exitedWeight);
        System.out.println("netWeight = " + netWeight);

        if (netWeight < 0) {
            cargoEntity.setCargoStatus(CargoStatus.DROP);
            cargoEntity.setNetWeight(Math.abs(netWeight));
        } else {
            cargoEntity.setCargoStatus(CargoStatus.PICKUP);
            cargoEntity.setNetWeight(Math.abs(netWeight));
        }
        cargoRepository.save(cargoEntity);
        System.out.println("cargoEntity = " + cargoEntity);
    }

    public void saveCargo(TruckResponse truckResponse) {
        CargoEntity cargoEntity = new CargoEntity();
        double enteredWeight = truckResponse.getEnteredWeight();
        double exitedWeight = truckResponse.getExitedWeight();
        double netWeight = exitedWeight - enteredWeight;

        System.out.println("enteredWeight = " + enteredWeight);
        System.out.println("exitedWeight = " + exitedWeight);
        System.out.println("netWeight = " + netWeight);

        if (netWeight < 0) {
            cargoEntity.setCargoStatus(CargoStatus.DROP);
            cargoEntity.setNetWeight(Math.abs(netWeight));
        } else {
            cargoEntity.setCargoStatus(CargoStatus.PICKUP);
            cargoEntity.setNetWeight(Math.abs(netWeight));
        }
        cargoRepository.save(cargoEntity);
        System.out.println("cargoEntity = " + cargoEntity);
    }


    public List<WeighingResponse> getNotSentData() {
        List<WeighingResponse> result = new ArrayList<>();
        List<CargoEntity> notSentData = cargoRepository.findByIsSent(false);
        for (CargoEntity cargo : notSentData) {

            WeighingResponse response = new WeighingResponse(
                    1L, cargo.getTruck().getTruckNumber(),
                    cargo.getCargoStatus(), cargo.getNetWeight(), cargo.getCreatedAt()
            );
            response.setId(cargo.getId());
            response.setIdOnServer(cargo.getIdOnServer());

            result.add(response);
        }

        return result;
    }

    public void dataSent(List<WeighingResponse> notSentData, Map<Long,Long> cargoMap) {
        if (cargoMap == null || cargoMap.isEmpty()) {
            return;
        }
        notSentData.forEach(cargo -> {
            CargoEntity entity = cargoRepository.findById(cargo.getId()).orElseThrow(() -> new RuntimeException(cargo.getId() + " is not found from database"));
            entity.setIsSent(true);
            entity.setIdOnServer(cargoMap.get(entity.getId()));
            cargoRepository.save(entity);
        });
    }
}
