package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
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

    public AttachStatus findAttachStatus(AttachEntity attach) {
        TruckPhotosEntity photosEntity = truckPhotosRepository.findByTruckPhoto(attach).orElse(null);
        if (photosEntity == null) return null;
        return photosEntity.getAttachStatus();
    }

    public TruckPhotosEntity findByAttach(AttachEntity attach) {
        return truckPhotosRepository.findByTruckPhoto(attach).orElse(null);
    }
}
