package fr.free.couturier_remi_hd.huemyhouse.hueBridge;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import fr.free.couturier_remi_hd.huemyhouse.hueCommonData.AndroidProvider;
import fr.free.couturier_remi_hd.huemyhouse.hueCommonData.SharedInformation;

/**
 * Created by rcouturi on 22/11/2015.
 */
public class HueLightManager {
    static  String TAG = "[HueMyHouse][HueLightManager]";

    Context lightContext;
    PHBridge bridge;
    private Uri uriLight;

    public HueLightManager(Context context) {
        lightContext = context;
        uriLight     = AndroidProvider.CONTENT_URI_LIGHT;
        bridge = HuePHSDKListener.phHueSDK.getSelectedBridge();
    }

    /**
     * Permet de récuperer toute les ampoules présente sur l'instance
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
    public long addHueLight(HueLight hueLight) {

        if (hueLight.hueId.isEmpty()) {
            throw new IllegalArgumentException("Identifiant du pont Hue est manquant !");
        }

        ContentValues newLight = new ContentValues();
        newLight.put(SharedInformation.hueLight.LIGHT_ID, 0);
        newLight.put(SharedInformation.hueLight.HUE_ID, hueLight.lightId);
        newLight.put(SharedInformation.hueLight.HUE_LIGHT_ID, hueLight.hueLightId);
        newLight.put(SharedInformation.hueLight.LIGHT_MODEL, hueLight.lightModel);
        newLight.put(SharedInformation.hueLight.LIGHT_TYPE, hueLight.lightType);
        newLight.put(SharedInformation.hueLight.LIGHT_NAME, hueLight.lightName);

        try {
            Uri uri = lightContext.getContentResolver().insert(uriLight, newLight);
            String lastPathSegment = uri.getLastPathSegment();            // Récuperation de l'URI
            if (lastPathSegment != null) {
                long idBdd = Long.parseLong(lastPathSegment);
                Log.d(TAG, "Identifiant BDD créé ID = " + idBdd);
                return idBdd;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return -1;
    }

    /**
     * Recherche les ampoules par rapport à l'id d'un pont
     * @param hueBridge
     * @return Liste d'objet HueLight
     */
    public ArrayList<HueLight> getHueLightByHue(HueBridge hueBridge) {
        // Verification de la présence des données
        if (hueBridge.hueId.isEmpty()) {
            throw new IllegalArgumentException("Identifiant du pont Hue est manquant !");
        }

        ArrayList<HueLight> allHueLight = new ArrayList<HueLight>();
        String columns[] = new String[]{
                SharedInformation.hueLight.LIGHT_ID,
                SharedInformation.hueLight.HUE_ID,
                SharedInformation.hueLight.HUE_LIGHT_ID,
                SharedInformation.hueLight.LIGHT_MODEL,
                SharedInformation.hueLight.LIGHT_TYPE,
                SharedInformation.hueLight.LIGHT_NAME};

        Cursor cursor = lightContext.getContentResolver().query(uriLight, columns, SharedInformation.hueBridge.HUE_ID + "=\"" + hueBridge.hueId + "\"", null, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    HueLight hueLight   = new HueLight();
                    hueLight.lightId    = cursor.getString(cursor.getColumnIndex(SharedInformation.hueLight.LIGHT_ID));
                    hueLight.hueId      = cursor.getString(cursor.getColumnIndex(SharedInformation.hueLight.HUE_ID));
                    hueLight.hueLightId = cursor.getString(cursor.getColumnIndex(SharedInformation.hueLight.HUE_LIGHT_ID));
                    hueLight.lightModel = cursor.getString(cursor.getColumnIndex(SharedInformation.hueLight.LIGHT_MODEL));
                    hueLight.lightType  = cursor.getString(cursor.getColumnIndex(SharedInformation.hueLight.LIGHT_TYPE));
                    hueLight.lightName  = cursor.getString(cursor.getColumnIndex(SharedInformation.hueLight.LIGHT_NAME));
                    allHueLight.add(hueLight);                                                                              // Ajout de l'ampoule à la liste
                    Log.d(TAG, "Ampoule Hue " + hueLight.hueId + " (" + hueLight.lightType + ") sur le pont : " + hueBridge.hueId);
                } while (cursor.moveToNext());
                return allHueLight;
            }
        }  catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    /**
     * Retire une ampoule de la BDD
     * @param hueLight
     * @return Boolean du resultat
     */
    public boolean removeHueLight(HueLight hueLight) {

        // Verification de la présence des données
        if (hueLight.lightId.isEmpty()) {
            throw new IllegalArgumentException("Identifiant de l'ampoule Hue est manquant !");
        }

        try {
            lightContext.getContentResolver().delete(uriLight, SharedInformation.hueLight.LIGHT_ID + "=\"" + hueLight.lightId + "\"", null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Mise à jour d'une ampoule en BDD
     * @param  hueLight
     * @return Boolean du resultat
     */
    public Boolean updateHueLight(HueLight hueLight){
        // Verification de la présence des données
        if (hueLight.lightId.isEmpty()) {
            throw new IllegalArgumentException("Identifiant de l'ampoule Hue est manquant !");
        }
        if (hueLight.hueId.isEmpty()) {
            throw new IllegalArgumentException("Identifiant du pont Hue est manquant !");
        }

        try {
            ContentValues updateLight = new ContentValues();
            updateLight.put(SharedInformation.hueLight.LIGHT_ID, hueLight.lightId);
            updateLight.put(SharedInformation.hueLight.HUE_ID, hueLight.hueId);
            updateLight.put(SharedInformation.hueLight.HUE_LIGHT_ID, hueLight.hueLightId);
            updateLight.put(SharedInformation.hueLight.LIGHT_MODEL, hueLight.lightModel);
            updateLight.put(SharedInformation.hueLight.LIGHT_TYPE, hueLight.lightType);
            updateLight.put(SharedInformation.hueLight.LIGHT_NAME, hueLight.lightName);
            lightContext.getContentResolver().update(uriLight, updateLight, SharedInformation.hueLight.LIGHT_ID + "=\"" + hueLight.lightId + "\"", null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Action distante à réalisé par le pont
     *
     * @param hueAction
     */
    public void meethueAction(String hueAction) {

        String urlPost = "https://www.meethue.com/api/sendmessage?token=" + HuePHSDKListener.hueBridge.meetHueToken;

        final String huecommand = "{ bridgeId: \"" + HuePHSDKListener.hueBridge.hueId + "\", clipCommand: { url: \"/api/" + HuePHSDKListener.hueBridge.hueUserName + "/groups/0/action\", method: \"PUT\", body: " + hueAction + "}}";
        Log.d(TAG, huecommand);

        final HttpPost httpPost = new HttpPost(urlPost);
        try {
            // REPONSE HTTP
            final Thread thread = new Thread() {
                public void run() {
                    try {
                        // AJOUT DU HEADER
                        httpPost.addHeader("content-type", "application/x-www-form-urlencoded");

                        // AJOUT DES DONNEES JSON
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        nameValuePairs.add(new BasicNameValuePair("clipmessage", huecommand));
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                        // INITIALISATION DU CLIENT HTTP + AJOUT DU COOKIE STCA
                        DefaultHttpClient httpClient = new DefaultHttpClient();

                        // EXECUTION DE LA REQUETE HTTP - POST
                        HttpResponse response = httpClient.execute(httpPost);
                        if (response != null) {
                            HttpEntity ent = response.getEntity();
                            InputStream inputStream = ent.getContent();
                            if (inputStream != null) {
                                // json is UTF-8 by default
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                                String line = null;
                                while ((line = reader.readLine()) != null) {
                                    Log.d(TAG, line);
                                }
                                inputStream.close();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            };
            thread.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Change la couleur de toutes les ampoules d'un pont Hue
     *
     * @param red
     * @param green
     * @param blue
     * @return result
     */
    public boolean changeAllLightsColor(int red, int green, int blue) {
        try {
            PHLightState lightState = new PHLightState();
            float xy[] = PHUtilities.calculateXYFromRGB(red, green, blue, null);             // RED / GREEN / BLUE
            lightState.setX(xy[0]);
            lightState.setY(xy[1]);
            bridge.setLightStateForDefaultGroup(lightState);
        } catch (Exception e) {
            Log.e(TAG, "Error = " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Permet d'allumer / eteindre toute les lumieres
     *
     * @param state
     * @return
     */
    public boolean setAllLightsOnOff(boolean state) {
        try {
            PHLightState lightState = new PHLightState();
            lightState.setOn(state);
            bridge.setLightStateForDefaultGroup(lightState);
        } catch (Exception e) {
            Log.e(TAG, "Error = " + e.getMessage());
            return false;
        }
        return true;
    }
}
