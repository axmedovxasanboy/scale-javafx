package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "logs")
public class LogEntity extends BaseEntity {

    private String truckNumber;
    private String event;

}
