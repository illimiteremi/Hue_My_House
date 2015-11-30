package fr.free.couturier_remi_hd.huemyhouse.hueBridge;

/**
 * Created by rcouturi on 22/11/2015.
 */
public class HueLight {

    public static String LIGHT_ON = "{\"on\":true}";
    public static String LIGHT_OFF = "{\"on\":false}";
    public static String ALERT_MODE = "{\"alert\":\"select\"}";
    static String TAG = "[HueMyHouse][HueLight]";
    public String lightId;
    public String hueId;
    public String hueLightId;
    public String lightModel;
    public String lightType;
    public String lightName;

}
