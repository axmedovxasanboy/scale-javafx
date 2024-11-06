package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name = "trucks")
public class TruckEntity extends BaseEntity {
    private String truckNumber;

    @OneToOne(cascade = CascadeType.PERSIST)
    private TruckActionEntity truckAction;

    @OneToMany
    private List<TruckPhotosEntity> truckPhotos;

}
