package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;

import java.util.List;

@Repository
public interface AttachRepository extends JpaRepository<AttachEntity, Long> {
    List<AttachEntity> findByIsSent(boolean isSent);
}
