package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachRepository extends JpaRepository<AttachEntity, Long> {
    List<AttachEntity> findByIsSentToCloud(boolean isSent);

    Optional<AttachEntity> findByFileName(String fileName);
}
