package uz.tenzorsoft.scaleapplication.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TruckResponse {
    private Long id;
    private String truckNumber;

    private List<AttachIdWithStatus> attaches;
    private TruckAction enteredStatus;
    private LocalDateTime enteredAt;
    private Double enteredWeight;
    private String entranceConfirmedBy;

    private TruckAction exitedStatus;
    private LocalDateTime exitedAt;
    private Double exitedWeight;
    private String exitConfirmedBy;

}
