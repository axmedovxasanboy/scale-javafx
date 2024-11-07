package uz.tenzorsoft.scaleapplication.domain.response.sendData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse extends BaseResponse {
    private String username;
    private String code;
    private String password;
}