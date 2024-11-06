package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.tenzorsoft.scaleapplication.domain.entity.CargoEntity;

import java.util.List;

public interface CargoRepository extends JpaRepository<CargoEntity, Long> {
    List<CargoEntity> findByIsSent(boolean isSent);
}
