package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;

@Repository
public interface TruckRepository extends JpaRepository<TruckEntity, Long> {
}
