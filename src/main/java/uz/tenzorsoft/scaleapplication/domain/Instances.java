package uz.tenzorsoft.scaleapplication.domain;

import uz.tenzorsoft.scaleapplication.domain.entity.UserEntity;

public class Instances<T> {
    public static Boolean isConnected = false;
    public static UserEntity currentUser = null;

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
