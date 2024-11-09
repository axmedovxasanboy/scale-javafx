package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name = "trucks")
public class TruckEntity extends BaseEntity {
    private String truckNumber;

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<TruckActionEntity> truckActions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TruckPhotosEntity> truckPhotos = new ArrayList<>();

    private Boolean isFinished = false;
    private Boolean isSentToMyCoal = false;

}
