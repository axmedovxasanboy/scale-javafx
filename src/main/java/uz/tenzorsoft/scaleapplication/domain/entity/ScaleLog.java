package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name="scale_data_logs")
public class ScaleLog extends BaseEntity{
    private String data;
    private String convertedData;
}
