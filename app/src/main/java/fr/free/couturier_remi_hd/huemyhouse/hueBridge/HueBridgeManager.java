package fr.free.couturier_remi_hd.huemyhouse.hueBridge;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;

import fr.free.couturier_remi_hd.huemyhouse.hueCommonData.AndroidProvider;
import fr.free.couturier_remi_hd.huemyhouse.hueCommonData.SharedInformation;

/**
 * Created by rcouturi on 08/11/2015.
 */
public class HueBridgeManager {

    Context hueContext;                   // Context

    static  String TAG = "[HueMyHouse][HueManager]";
    static  String nupnp = "https://www.meethue.com/api/nupnp";
    private Uri uriBridge;

    /**
     * Constructeur
     *
     * @param context
     */
    public HueBridgeManager(Context context) {
        hueContext = context;
        uriBridge = AndroidProvider.CONTENT_URI_BRIDGE;
    }

    /**
     * Ajout d'un pont Hue dans la base de données
     *
     * @param hueBridge
     * @return Identifiant bdd
     */
    public long addHueBridge(HueBridge hueBridge) {

        long idBdd = -1;                                                 // Init valeur de retour

        // Verification de la présence des données
        if (hueBridge.hueId.isEmpty()) return -1;                        // Valeur obligatoire
        if (hueBridge.hueIp.isEmpty()) return -1;                        // Valeur obligatoire

        ContentValues newBridge = new ContentValues();
        hueBridge.hueWifi = getWifiName();                              // Récuperation du SIDD du wifi
        newBridge.put(SharedInformation.hueBridge.HUE_ID, 0);
        newBridge.put(SharedInformation.hueBridge.HUE_BRIDGE_ID, hueBridge.hueId);
        newBridge.put(SharedInformation.hueBridge.HUE_IP, hueBridge.hueIp);
        newBridge.put(SharedInformation.hueBridge.HUE_MAC_ADRESS, hueBridge.hueMacAdress);
        newBridge.put(SharedInformation.hueBridge.HUE_WIFI_NAME, hueBridge.hueWifi);
        newBridge.put(SharedInformation.hueBridge.HUE_USERNAME, hueBridge.hueUserName);
        newBridge.put(SharedInformation.hueBridge.HUE_TOKEN, hueBridge.meetHueToken);
        Uri uri = hueContext.getContentResolver().insert(uriBridge, newBridge);
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment != null) {
            try {
                idBdd = Long.parseLong(lastPathSegment);
                Log.d(TAG, "Identifiant BDD créé ID = " + idBdd);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Number Format Exception : " + e);
                idBdd = -1;
            }
        }
        return idBdd;
    }

    /**
     * Récuperation de la liste des ponts Hue enregistrés
     *
     * @return : Liste d'objet HueBridge
     */
    public ArrayList<HueBridge> getAllHueBridge() {

        ArrayList<HueBridge> allHueBridge = new ArrayList<HueBridge>();

        String columns[] = new String[]{
                SharedInformation.hueBridge.HUE_ID,
                SharedInformation.hueBridge.HUE_BRIDGE_ID,
                SharedInformation.hueBridge.HUE_IP,
                SharedInformation.hueBridge.HUE_MAC_ADRESS,
                SharedInformation.hueBridge.HUE_WIFI_NAME,
                SharedInformation.hueBridge.HUE_USERNAME,
                SharedInformation.hueBridge.HUE_TOKEN};

        Cursor cursor = hueContext.getContentResolver().query(uriBridge, columns, null, null, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    HueBridge hueBridge =  new HueBridge("", "", "", "", "");           // initialisation d'un pont vierge
                    hueBridge.hueId = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_BRIDGE_ID));
                    hueBridge.hueIp = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_IP));
                    hueBridge.hueMacAdress = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_MAC_ADRESS));
                    hueBridge.hueWifi = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_WIFI_NAME));
                    hueBridge.hueUserName = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_USERNAME));
                    hueBridge.meetHueToken = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_TOKEN));
                    allHueBridge.add(hueBridge);
                } while (cursor.moveToNext());
            }
        } catch (Exception e){
            Log.e(TAG, e.toString());
            return null;
        }
        return allHueBridge;
    }

    /**
     * Mise à jour d'un pont Hue
     * @param hueBridge
     * @return
     */
    public Boolean updateHueBridge(HueBridge hueBridge){
        // Verification de la présence des données
        if (hueBridge.hueId.isEmpty()) return false;                        // Valeur obligatoire
        if (hueBridge.hueIp.isEmpty()) return false;                        // Valeur obligatoire

        try {
            ContentValues updateBridge = new ContentValues();
            updateBridge.put(SharedInformation.hueBridge.HUE_BRIDGE_ID, hueBridge.hueId);
            updateBridge.put(SharedInformation.hueBridge.HUE_IP, hueBridge.hueIp);
            updateBridge.put(SharedInformation.hueBridge.HUE_MAC_ADRESS, hueBridge.hueMacAdress);
            updateBridge.put(SharedInformation.hueBridge.HUE_WIFI_NAME, hueBridge.hueWifi);
            updateBridge.put(SharedInformation.hueBridge.HUE_USERNAME, hueBridge.hueUserName);
            updateBridge.put(SharedInformation.hueBridge.HUE_TOKEN, hueBridge.meetHueToken);
            hueContext.getContentResolver().update(uriBridge, updateBridge, SharedInformation.hueBridge.HUE_BRIDGE_ID + "=\"" + hueBridge.hueId + "\"", null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Suppression d'un pont Hue
     * @param hueBridge
     * @return
     */
    public Boolean removeHueBridge(HueBridge hueBridge){
        // Verification de la présence des données
        if (hueBridge.hueId.isEmpty()) return false;                        // Valeur obligatoire

        try {
            ContentValues removeBridge = new ContentValues();
            removeBridge.put(SharedInformation.hueBridge.HUE_BRIDGE_ID, hueBridge.hueId);
            hueContext.getContentResolver().delete(uriBridge, SharedInformation.hueBridge.HUE_BRIDGE_ID + "=\"" + hueBridge.hueId + "\"", null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Récuperation d'un pont Hue par son adress IP
     * @param hueBridge
     * @return
     */
    public HueBridge getHueBridgeByNetwork(HueBridge hueBridge) {
        // Verification de la présence des données
        if (hueBridge.hueIp.isEmpty()) return null;                        // Valeur obligatoire

        String columns[] = new String[]{
                SharedInformation.hueBridge.HUE_ID,
                SharedInformation.hueBridge.HUE_BRIDGE_ID,
                SharedInformation.hueBridge.HUE_IP,
                SharedInformation.hueBridge.HUE_MAC_ADRESS,
                SharedInformation.hueBridge.HUE_WIFI_NAME,
                SharedInformation.hueBridge.HUE_USERNAME,
                SharedInformation.hueBridge.HUE_TOKEN};

        Cursor cursor = hueContext.getContentResolver().query(uriBridge, columns, SharedInformation.hueBridge.HUE_IP + "=\"" + hueBridge.hueIp + "\"", null, null);

        if (cursor.moveToFirst()) {
            do {
                hueBridge.hueId = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_BRIDGE_ID));
                hueBridge.hueIp = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_IP));
                hueBridge.hueMacAdress = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_MAC_ADRESS));
                hueBridge.hueWifi = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_WIFI_NAME));
                hueBridge.hueUserName = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_USERNAME));
                hueBridge.meetHueToken = cursor.getString(cursor.getColumnIndex(SharedInformation.hueBridge.HUE_TOKEN));
            } while (cursor.moveToNext());
        }
        Log.d(TAG, "Pont Hue " + hueBridge.hueId + " (" + hueBridge.hueIp + ") sur le reseau : " + hueBridge.hueWifi);
        return hueBridge;
    }

    /**
     * Permet d'associer un pont hue avec un compte "Meethue"
     * @param hueBridge
     */
    public void getMeetHueToken (Context hueContex, HueBridge hueBridge) {
        // Connexion à Meethue si pas de TOKEN
        try {
            if (!hueBridge.hueId.isEmpty()){
                Intent tokenIntent = new Intent(hueContex, MeethueConnexion.class);
                tokenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                tokenIntent.putExtra("hueIp", hueBridge.hueIp);
                hueContex.startActivity(tokenIntent);
                Log.d(TAG, "Ouverture de l'url 'meethue' pour récuperation du Token");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retourne le SSID Wifi
     * @return
     */
    private String getWifiName() {
        WifiManager manager = (WifiManager) hueContext.getSystemService(hueContext.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.getSSID();
                }
            }
        }
        return null;
    }

}