package uz.tenzorsoft.scaleapplication.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name = "truck_actions")
public class TruckActionEntity extends BaseEntity {
    @Column(nullable = false)
    private Double weight;

    @Enumerated(EnumType.STRING)
    private TruckAction action;

    @ManyToOne
    private UserEntity onDuty;

}
