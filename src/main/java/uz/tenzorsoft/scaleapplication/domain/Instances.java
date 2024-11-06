package uz.tenzorsoft.scaleapplication.domain;

import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.UserEntity;

public class Instances<T> {
    public static Boolean isConnected = false;
    public static UserEntity currentUser = null;
    public static TruckEntity currentTruckEntity = null;

    public static boolean gate1Connection = false;
    public static boolean gate2Connection = false;
    public static boolean camera1Connection = false;
    public static boolean camera2Connection = false;
    public static boolean camera3Connection = false;
    public static boolean sensor1Connection = false;
    public static boolean sensor2Connection = false;
    public static boolean sensor3Connection = false;



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
