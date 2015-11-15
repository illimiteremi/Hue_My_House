package fr.free.couturier_remi_hd.huemyhouse.hueBridge;

import android.util.Log;

/**
 * Created by rcouturi on 11/11/2015.
 */
public class HueBridge {

    public  String        hueId;
    public  String        hueIp;
    public  String        hueMacAdress;
    public  String        hueWifi;
    public  String        hueUserName;
    public  String        meetHueToken;

    static  String        TAG               = "[HueMyHouse][HueBridge]";
    static  String        aapId             = "hueapp";


    public HueBridge(String hueId, String hueIp, String hueUserName, String hueMacAdress, String hueWifi, String meetHueToken) {
        this.hueId        = hueId;
        this.hueIp        = hueIp;
        this.hueWifi      = hueWifi;
        this.hueMacAdress = hueMacAdress;
        this.hueUserName  = hueUserName;
        this.meetHueToken = meetHueToken;
    }

    /**
     * Url pour recuperer le token "meethue"
     * @return
     */
    public String getMeetHueToken() {
        String url;
        url =  "http://www.meethue.com/en-FR/api/gettoken?devicename=" + android.os.Build.MODEL + "&appid=" + this.aapId + "&deviceid=" + this.hueId;
        Log.d(TAG,"Récuperation de l'url 'meethue' pour token : " + url);
        return url;
    }
}
