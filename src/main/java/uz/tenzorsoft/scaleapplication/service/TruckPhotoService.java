package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.repository.TruckPhotoRepository;

@Service
@RequiredArgsConstructor
public class TruckPhotoService {

    @Autowired
    private TruckPhotoRepository truckPhotosRepository;

    public TruckPhotosEntity findById(Long id) {
        return truckPhotosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Truck photo not found with ID: " + id));
    }
}
