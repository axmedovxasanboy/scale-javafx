package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.enumerators.CargoStatus;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name = "cargos")
public class CargoEntity extends BaseEntity {
    private Double netWeight;
    private CargoStatus cargoStatus;
    private Long scaleId;

    @OneToOne
    private TruckEntity truck;

}
