package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TruckRepository extends JpaRepository<TruckEntity, Long> {

    List<TruckEntity> findByIsSent(boolean isSent);

    Optional<TruckEntity> findTopByOrderByIdDesc();

    @Query("SELECT t FROM trucks t " +
            "JOIN t.truckActions ta " +
            "WHERE t.truckNumber = :truckNumber " +
            "AND ta.action = 'ENTRANCE' " +
            "AND NOT EXISTS (" +
            "    SELECT ta2 FROM truck_actions ta2 " +
            "    WHERE ta2 MEMBER OF t.truckActions " +
            "    AND ta2.action IN (" +
            "uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction.EXIT, " +
            "uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction.MANUAL_EXIT)" +
            ")")
    Optional<TruckEntity> findTruckWithEntranceNoExit(@Param("truckNumber") String truckNumber);

    Optional<TruckEntity> findByTruckPhotosContains(TruckPhotosEntity truckPhotos);


    List<TruckEntity> findByTruckNumberOrderByCreatedAtDesc(String truckNumber);

    Optional<TruckEntity> findByTruckNumberAndActive(String truckNumber, Boolean active);


}
