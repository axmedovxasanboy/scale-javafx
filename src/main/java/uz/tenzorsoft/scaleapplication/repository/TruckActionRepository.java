package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;

@Repository
public interface TruckActionRepository extends JpaRepository<TruckActionEntity, Long> {

}
