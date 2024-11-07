package uz.tenzorsoft.scaleapplication.domain.response.sendData;

import lombok.Getter;
import lombok.Setter;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class AttachmentResponse extends BaseResponse {

    private Long truckId;

    private String originName;
    private Double size;
    private String type;
    private String contentType;
    private String localPath;
    private String cloudPath;
    private AttachStatus attachStatus;
    private LocalDateTime createdDate;

    private String bytes;
}
