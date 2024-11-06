package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name = "truck_photos")
public class TruckPhotosEntity extends BaseEntity {

    @OneToOne
    private AttachEntity truckPhoto;

    @Enumerated(EnumType.STRING)
    private AttachStatus attachStatus;

}
