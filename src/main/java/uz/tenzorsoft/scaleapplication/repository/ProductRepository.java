package uz.tenzorsoft.scaleapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.tenzorsoft.scaleapplication.domain.entity.ProductsEntity;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductsEntity, Long> {
    List<ProductsEntity> findAllByIsDeletedFalse();
}
