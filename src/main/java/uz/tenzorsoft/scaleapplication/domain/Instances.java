package uz.tenzorsoft.scaleapplication.domain;

import lombok.Synchronized;
import org.hibernate.annotations.Synchronize;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.UserEntity;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;

public class Instances<T> {
    public static UserEntity currentUser = new UserEntity();
    public static TruckResponse currentTruck = new TruckResponse();

    public static String truckNumber = "";

    public static boolean isConnectedToInternet = false;
    public static boolean isConnected = false;
    public static boolean gate1Connection = false;
    public static boolean gate2Connection = false;
    public static boolean camera1Connection = false;
    public static boolean camera2Connection = false;
    public static boolean camera3Connection = false;
    public static boolean sensor1Connection = false;
    public static boolean sensor2Connection = false;
    public static boolean sensor3Connection = false;
    public static boolean isWaiting = false;
    public static boolean isScaleControlOn = false;
    public static short cargoConfirmationStatus = -1;



    public static void reinitializeAll() {
        isConnected = false;
    }

    public static Object reinitialize(Object obj) {
        obj = null;
        return obj;
    }

    public T reinitialize(T obj, T value) {
        obj = value;
        return obj;
    }

}
