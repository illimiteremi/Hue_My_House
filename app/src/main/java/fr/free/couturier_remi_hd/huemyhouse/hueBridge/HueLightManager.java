package fr.free.couturier_remi_hd.huemyhouse.hueBridge;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.free.couturier_remi_hd.huemyhouse.hueCommonData.AndroidProvider;
import fr.free.couturier_remi_hd.huemyhouse.hueCommonData.SharedInformation;

/**
 * Created by rcouturi on 22/11/2015.
 */
public class HueLightManager {
    static  String TAG = "[HueMyHouse][HueLightManager]";

    Context lightContext;
    private Uri uriLight;

    public HueLightManager(Context context) {
        lightContext = context;
        uriLight     = AndroidProvider.CONTENT_URI_LIGHT;
    }

    /**
     * Permet de récuperer toute les ampoules présente
     * @return Liste d'objet HueLight
     */
    public ArrayList<HueLight> getAllInstanceLights() {
        PHHueSDK phHueSDK               = PHHueSDK.getInstance();
        PHBridgeResourcesCache cache    = phHueSDK.getSelectedBridge().getResourceCache();
        List<PHLight> allLights         = (ArrayList) cache.getAllLights();
        ArrayList<HueLight> allHueLight = new ArrayList<HueLight>();

        for(PHLight light : allLights) {
            HueLight hueLight   = new HueLight();
            hueLight.lightType  = light.getLightType().toString();
            hueLight.lightId    = light.getUniqueId();
            hueLight.lightModel = light.getModelNumber();
            hueLight.lightName  = light.getName();
            allHueLight.add(hueLight);
        }
        return allHueLight;
    }

    /**
     * Ajout d'une ampoule dans la base de données
     * @param hueLight
     * @return Boolean
     */
    public boolean addHueLight(HueLight hueLight) {
        // Verification de la présence des données
        if (hueLight.hueId.isEmpty()) return false;                        // Valeur obligatoire
        if (hueLight.hueLightId.isEmpty()) return false;                   // Valeur obligatoire

        try {
            ContentValues newLight = new ContentValues();
            newLight.put(SharedInformation.hueLight.LIGHT_ID, 0);
            newLight.put(SharedInformation.hueLight.HUE_ID, hueLight.lightId);
            newLight.put(SharedInformation.hueLight.HUE_LIGHT_ID, hueLight.hueLightId);
            newLight.put(SharedInformation.hueLight.LIGHT_MODEL, hueLight.lightModel);
            newLight.put(SharedInformation.hueLight.LIGHT_TYPE, hueLight.lightType);
            newLight.put(SharedInformation.hueLight.LIGHT_NAME, hueLight.lightName);
            lightContext.getContentResolver().insert(uriLight, newLight);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }
}