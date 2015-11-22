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

    Context         lightContext;
    private Uri     uriLight;


    public HueLight(Context context) {
        lightContext = context;
        uriLight     = AndroidProvider.CONTENT_URI_LIGHT;
    }

    public List getAllLights() {
        PHHueSDK phHueSDK = PHHueSDK.getInstance();
        PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();
        // And now you can get any resource you want, for example:
        List myLights = cache.getAllLights();
        addHueLight();
        return myLights;
    }


    public boolean addHueLight() {
        ContentValues newLight = new ContentValues();
        newLight.put(SharedInformation.hueLight.LIGHT_ID, 0);
        newLight.put(SharedInformation.hueLight.HUD_ID, 1);
        newLight.put(SharedInformation.hueLight.HUE_LIGHT_ID, 1);
        newLight.put(SharedInformation.hueLight.LIGHT_MODEL, "toto");
        newLight.put(SharedInformation.hueLight.LIGHT_TYPE, "titi");
        newLight.put(SharedInformation.hueLight.LIGHT_NAME, "tata");
        lightContext.getContentResolver().insert(uriLight, newLight);

        return true;
    }


}
