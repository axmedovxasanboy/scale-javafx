package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;

import java.util.List;

import java.util.List;

@Repository
public interface TruckRepository extends JpaRepository<TruckEntity, Long> {

    @Query("SELECT t FROM trucks t WHERE t.truckNumber = :truckNumber AND t.truckAction.action = :entranceAction")
    TruckEntity findEnteredTruckByTruckNumberAndNotExited(@Param("truckNumber") String truckNumber, @Param("entranceAction") TruckAction entranceAction);

    // Query to find a truck by truck number and truck action
    @Query("SELECT t FROM trucks t JOIN t.truckAction ta WHERE t.truckNumber = :truckNumber AND ta.action = :truckAction")
    TruckEntity findByTruckNumberAndAction(@Param("truckNumber") String truckNumber, @Param("truckAction") TruckAction truckAction);

    @Query("SELECT t FROM trucks t WHERE t.truckAction.action = :action")
    List<TruckEntity> findEnteredTrucks(TruckAction action);

    List<TruckEntity> findByIsSent(boolean isSent);
}
