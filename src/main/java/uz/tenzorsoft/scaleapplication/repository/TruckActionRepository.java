package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TruckActionRepository extends JpaRepository<TruckActionEntity, Long> {

    @Query("SELECT ta.action, SUM(ta.weight), CAST(ta.createdAt AS date) " +
            "FROM truck_actions ta " +
            "WHERE CAST(ta.createdAt AS date) BETWEEN :startDate AND :endDate " +
            "GROUP BY ta.action, CAST(ta.createdAt AS date)")
    List<Object[]> findTruckWeightsByDate(LocalDate startDate, LocalDate endDate);


}
