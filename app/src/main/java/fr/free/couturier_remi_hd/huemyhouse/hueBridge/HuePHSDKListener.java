package fr.free.couturier_remi_hd.huemyhouse.hueBridge;

import android.content.Context;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueParsingError;

import java.util.List;

public class HuePHSDKListener {

    public static final int BRIDGE_NOT_RESPONDING = 46;               // Code erreur - bridge not responding
    public static final int BRIDGE_NOT_FOUND = 1157;             // Code erreur - No bridge found
    public static int              errorCode                = 0;               // Code erreur retourné par le pon
    public static PHHueSDK         phHueSDK;                                   // Instance du pont Hue
    public static PHBridge         phHueBridge;                                // Objet pont Hue connecté
    public static HueBridge        hueBridge;                                  // Objet pont BDD connecté
    public static HueBridgeManager hueBridgeManager;                           // Objet pont BDD manager

    public static boolean   onBridgeSearch           = false;            // Etat sur la recheche d'un pont
    public static boolean   onAuthentification       = false;            // Etat sur l'authentification du pont
    public static boolean   onConnectionResume       = false;            // Reconnexion au pont
    public static boolean   onMeethueMode            = false;            // Etat de syncro avec meethue
    public static boolean   onBridgeConnected        = false;            // Etat de connection au pont Hue en wifi
    static String TAG = "[HueMyHouse][PHSDKListener]";
    Context context;
    private PHSDKListener  listener = new PHSDKListener() {

        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
            phHueBridge = phBridge;
            if (list.contains(PHMessageType.LIGHTS_CACHE_UPDATED)) {
                Log.d(TAG, "Lights Cache Updated");
            }
        }

        @Override
        public void onBridgeConnected(PHBridge phBridge, String userName) {
            Log.d(TAG, "Le pont Hue est connecté");
            phHueBridge = phBridge;
            phHueSDK.setSelectedBridge(phHueBridge);
            phHueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
            Log.d(TAG, "UserName = " + userName);

            // Recuperation de l'adresse IP du pont
            PHBridgeResourcesCache cache = phBridge.getResourceCache();
            PHBridgeConfiguration bridge = cache.getBridgeConfiguration();
            hueBridge.hueIp = bridge.getIpAddress();

            // Enregistrement du username retourné par le pont dans la BDD
            hueBridge = hueBridgeManager.getHueBridgeByNetwork(hueBridge);
            hueBridge.hueUserName = userName;
            hueBridgeManager.updateHueBridge(hueBridge);
            Log.d(TAG, "Authentification sur le pont Hue (" + hueBridge.hueIp + ") effectuée.");
            onBridgeConnected  = true;
            onAuthentification = false;                         // fin d'authentification
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
            Log.d(TAG, "onAuthenticationRequired");
            phHueSDK.startPushlinkAuthentication(phAccessPoint);
            onAuthentification = true;                          // En cours d'authentification
        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list) {
            HueBridgeManager myManager = new HueBridgeManager(context);
            for (PHAccessPoint myAccesPoint : list) {                   // Liste des ponts trouvés
                String ip = myAccesPoint.getIpAddress();                // @IP
                String id = myAccesPoint.getBridgeId();                 // Identifiant
                String userName = "";                                   // UserName (vide par default)
                String macAddress = myAccesPoint.getMacAddress();       // @Mac
                String token = "";                                      // token d'authentification Hue
                Log.d(TAG, "Pont Hue trouvé => " + id + " / " + ip);

                HueBridge hueBridge = new HueBridge(id, ip, userName, macAddress, token);
                myManager.addHueBridge(hueBridge);                      // Enregistrement du pont hue
            }
            onBridgeSearch = false;                                     // Fin de la recherche
        }

        @Override
        public void onError(int codeNumber, String errorMessage) {
            errorCode = codeNumber;
            Log.d(TAG, "Error : " + codeNumber + " - " + errorMessage);
            // Vérification du token meetHue
            if(!hueBridge.meetHueToken.isEmpty()){
                onMeethueMode = true;
                Log.d(TAG,"Un token meetHue est enregistré");
            }

            switch (errorCode) {
                case 101:                                       // link button not pressed
                    break;
                case BRIDGE_NOT_FOUND:                          // No bridge found
                    onBridgeSearch = false;                     // Fin de recherche
                    break;
                case BRIDGE_NOT_RESPONDING:                     // bridge not responding
                    onAuthentification = false;                 // Fin d'authentification
                    break;
                default:                                        // Autres codes
            }
        }

        @Override
        public void onConnectionResumed(PHBridge phBridge) {
            Log.d(TAG, "onConnectionResumed");
            phHueBridge = phBridge;

            // Recuperation de l'adresse IP du pont
            PHBridgeResourcesCache cache = phBridge.getResourceCache();
            PHBridgeConfiguration bridge = cache.getBridgeConfiguration();
            hueBridge.hueIp = bridge.getIpAddress();

            // recharchement des données de l'objet pont
            hueBridge = hueBridgeManager.getHueBridgeByNetwork(hueBridge);
            onConnectionResume  = true;
            onBridgeConnected   = true;
        }

        @Override
        public void onConnectionLost(PHAccessPoint phAccessPoint) {
            Log.d(TAG, "onConnectionLost");
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list) {

        }
     };

    /**
     * Constructeur
     * @param context
     */
    public HuePHSDKListener(Context context) {

        this.context     = context;
        hueBridge        = new HueBridge("", "", "", "", "");              // initialisation d'un objet pont BDD
        hueBridgeManager = new HueBridgeManager(context);                  // Initialisation du manager de pont BDD
        phHueSDK         = PHHueSDK.create();

        // Register the PHSDKListener to receive callbacks from the bridge.
        phHueSDK.getNotificationManager().registerSDKListener(listener);

    }
}