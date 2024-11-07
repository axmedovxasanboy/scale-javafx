package uz.tenzorsoft.scaleapplication.domain.response.sendData;

import lombok.Getter;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.enumerators.CargoStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class WeighingResponse extends BaseResponse {

    private Long scaleId;

    private String truckNumber;
    private CargoStatus cargoStatus;
    private Double weight;
    private LocalDateTime createdAt;
}
