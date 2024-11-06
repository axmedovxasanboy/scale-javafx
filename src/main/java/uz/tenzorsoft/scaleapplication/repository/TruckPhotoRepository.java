package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;

@Repository
public interface TruckPhotoRepository extends JpaRepository<TruckPhotosEntity, Long> {
}
