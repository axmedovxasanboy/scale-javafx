package uz.tenzorsoft.scaleapplication.domain.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TableViewData {
    private Long id;
    private String enteredTruckNumber;
    private String enteredDate;
    private String enteredTime;
    private Double enteredWeight;
    private String enteredOnDuty;
    private String exitedTruckNumber;
    private String exitedDate;
    private String exitedTime;
    private Double exitedWeight;
    private String exitedOnDuty;

}
