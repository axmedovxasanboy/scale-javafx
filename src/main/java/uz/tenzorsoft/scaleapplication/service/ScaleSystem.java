package uz.tenzorsoft.scaleapplication.service;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import javafx.scene.image.Image;

import java.net.InetAddress;

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
    public static final String CAMERA_1 = "192.168.1.64";
    public static final String CAMERA_2 = "192.168.1.65";
    public static final String CAMERA_3 = "192.168.1.63";
    public static String CONTROLLER_IP = "192.168.1.5";
    public static ModbusTCPTransaction transaction;
    public static TCPMasterConnection connection;
    public static InetAddress address;
    public static Integer port = Modbus.DEFAULT_PORT;
    public static Integer truckPosition = -1;
    public static final Image greenLight = new Image("/images/green_light.png");
    public static final Image redLight = new Image("/images/red_light.png");

}
