package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.ProductsEntity;
import uz.tenzorsoft.scaleapplication.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {


    private final ProductRepository productRepository;

    public List<String> getAllProducts() {
        return productRepository.findAllByIsDeletedFalse().stream().map(ProductsEntity::getName).toList();
    }


    public ProductsEntity saveProduct(String productName) {
        ProductsEntity entity = new ProductsEntity();
        entity.setName(productName);
        return productRepository.save(entity);
    }
}
