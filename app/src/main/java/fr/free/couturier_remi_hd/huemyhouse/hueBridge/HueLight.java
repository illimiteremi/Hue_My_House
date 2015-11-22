package fr.free.couturier_remi_hd.huemyhouse.hueBridge;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridgeResourcesCache;

import java.util.List;

import fr.free.couturier_remi_hd.huemyhouse.hueCommonData.AndroidProvider;
import fr.free.couturier_remi_hd.huemyhouse.hueCommonData.SharedInformation;

/**
 * Created by rcouturi on 22/11/2015.
 */
public class HueLight {

    static  String TAG = "[HueMyHouse][HueLight]";

    public String lightId;
    public String hueId;
    public String hueLightId;
    public String lightModel;
    public String lightType;
    public String lightName;

    public HueLight() {

    }

}
