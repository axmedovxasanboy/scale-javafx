package uz.tenzorsoft.scaleapplication.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Configurations implements Serializable {

    private static final long serialVersionUID = 1L;

    private String controllerIp;
    private Integer controllerPort;
    private Integer controllerConnectTimeout;

    private Integer closeGate1Timeout;
    private Integer closeGate2Timeout;

    private Integer scaleTimeout;
    private String scalePort;

    private String databaseName;
    private String username;
    private String password;
    private String camera1;
    private String camera2;
    private String camera3;
}
