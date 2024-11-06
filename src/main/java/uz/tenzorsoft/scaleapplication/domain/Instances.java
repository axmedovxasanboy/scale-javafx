package uz.tenzorsoft.scaleapplication.domain;

import uz.tenzorsoft.scaleapplication.domain.entity.UserEntity;

public class Instances {
    public static Boolean isConnected = false;
    public static UserEntity currentUser = null;

    public static void reinitializeAll() {
        isConnected = false;
    }

    public static void reinitialize(Object obj){
        obj = null;
    }

}
