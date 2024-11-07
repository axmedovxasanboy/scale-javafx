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

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<TruckActionEntity> truckActions;

    @OneToMany(fetch = FetchType.EAGER)
    private List<TruckPhotosEntity> truckPhotos;

}
