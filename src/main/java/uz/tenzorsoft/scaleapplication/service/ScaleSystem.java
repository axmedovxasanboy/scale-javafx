package uz.tenzorsoft.scaleapplication.service;

import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import javafx.scene.image.Image;
import uz.tenzorsoft.scaleapplication.domain.Settings;

import java.net.InetAddress;

import static uz.tenzorsoft.scaleapplication.domain.Settings.SCALE_PORT;

public class ScaleSystem {
    public static final Integer COIL_GREEN_LIGHT_1 = 0;
    public static final Integer COIL_RED_LIGHT_2 = 0;
    public static final Integer COIL_GREEN_LIGHT_2 = 1;
    public static final Integer COIL_OPEN_GATE_1 = 2;
    public static final Integer COIL_CLOSE_GATE_1 = 3;
    public static final Integer COIL_OPEN_GATE_2 = 4;
    public static final Integer COIL_CLOSE_GATE_2 = 5;
    public static final Integer COIL_SENSOR_1 = 15;
    public static final Integer COIL_SENSOR_2 = 16;
    public static final Integer COIL_SENSOR_3 = 17;
    public static final String GOOGLE_DNS = "8.8.8.8";

    public static final Image greenLight = new Image("/images/green_light.png");
    public static final Image redLight = new Image("/images/red_light.png");

    public static ModbusTCPTransaction transaction;
    public static TCPMasterConnection connection;
    public static InetAddress address;
    public static Integer truckPosition = -1;
    public static SerialPort scalePort;

}
