package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.repository.AttachRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachService implements BaseService<AttachEntity, AttachResponse, Object> {

    private final AttachRepository attachRepository;

//    @Value("${attach.upload.folder}")
    private String attachUploadFolder;

    public AttachResponse saveToSystem(MultipartFile file) {
        try {

            String pathFolder = getYmDString(); // 2022/04/23
            File folder = new File(attachUploadFolder + pathFolder); // attaches/2022/04/23

            if (!folder.exists()) folder.mkdirs();

            String fileName = UUID.randomUUID().toString(); // dasdasd-dasdasda-asdasda-asdasd
            String extension = getExtension(file.getOriginalFilename()); //zari.jpg

            // attaches/2022/04/23/dasdasd-dasdasda-asdasda-asdasd.jpg
            byte[] bytes = file.getBytes();
            Path path = Paths.get(attachUploadFolder + pathFolder + "/" + fileName + "." + extension);
            File f = Files.write(path, bytes).toFile();


            AttachEntity entity = new AttachEntity(
                    file.getOriginalFilename(), fileName, file.getSize(),
                    extension, file.getContentType(), path.toString()
            );
            attachRepository.save(entity);

            return entityToResponse(entity);

        } catch (IOException e) {
            throw new RuntimeException("File could not upload");
        }
    }

    public AttachResponse saveToSystem(MultipartFile file, AttachStatus attachStatus) {
        return null;
    }


    public String getYmDString() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DATE);

        return year + "/" + month + "/" + day; // 2022/04/23
    }


    public String getExtension(String fileName) {
        if (fileName == null) {
            throw new RuntimeException("File name null");
        }
        int lastIndex = fileName.lastIndexOf(".");
        return fileName.substring(lastIndex + 1);
    }


    @Override
    public AttachResponse entityToResponse(AttachEntity entity) {
        return new AttachResponse(
                entity.getId(), entity.getOriginalName(), entity.getFileName(), entity.getSize(),
                entity.getType(), entity.getContentType(), entity.getPath(), entity.getCreatedAt()
        );
    }

    @Override
    public AttachEntity requestToEntity(Object request) {
        return null;
    }
}
