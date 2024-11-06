package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name = "attachments")
public class AttachEntity extends BaseEntity {
    private String originalName;
    private String fileName;
    private Long size;
    private String type;
    private String contentType;
    private String path;
}
