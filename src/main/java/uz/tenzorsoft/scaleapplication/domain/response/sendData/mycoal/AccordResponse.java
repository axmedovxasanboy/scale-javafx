package uz.tenzorsoft.scaleapplication.domain.response.sendData.mycoal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AccordResponse {
    private String number;
    private LocalDateTime date;
}