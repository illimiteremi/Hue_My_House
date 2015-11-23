package fr.free.couturier_remi_hd.huemyhouse.HueActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueParsingError;

import java.util.ArrayList;
import java.util.List;

import fr.free.couturier_remi_hd.huemyhouse.R;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HueBridge;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HueBridgeManager;

public class StartActivity extends ActionBarActivity {

    static  String       TAG                = "[HueMyHouse][StartActivity]";
    PHHueSDK             phHueSDK;

    Boolean              onBridgeSearch     = false;
    Boolean              onAuthentification = false;

    private PHSDKListener listener = new PHSDKListener() {

        /**
         * Envoi un message au MainActivity
         * @param listenerMessage
         */
        private void sendMessage(String listenerMessage) {
            //Intent i = new Intent(MainActivity.this,MainActivity.class);
            //i.putExtra("listenerMessage", listenerMessage);
            //startActivity(i);
        }

        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
            if (list.contains(PHMessageType.LIGHTS_CACHE_UPDATED)) {
                Log.d(TAG, "Lights Cache Updated");
            }
        }

        @Override
        public void onBridgeConnected(PHBridge phBridge, String userName) {
            Log.d(TAG, "Le pont Hue est connecté");
            phHueSDK.setSelectedBridge(phBridge);
            phHueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
            Log.d(TAG, "UserName = " + userName);

            // Recuperation de l'adresse IP du pont
            PHBridgeResourcesCache cache  = phBridge.getResourceCache();
            PHBridgeConfiguration bridge  = cache.getBridgeConfiguration();
            String bridgeIpAddress        = bridge.getIpAddress();

            // Enregistrement du username dans la bdd
            HueBridgeManager hueBridgeManager = new HueBridgeManager(getApplicationContext());
            HueBridge hueBridge   = new HueBridge("", bridgeIpAddress, "", "", "");
            hueBridge             = hueBridgeManager.getHueBridgeByNetwork(hueBridge);
            hueBridge.hueUserName = userName;
            Boolean isUpdate      = hueBridgeManager.updateHueBridge(hueBridge);
            Log.d(TAG, "Mise à jour du pont = " + isUpdate);
            Log.d(TAG, "Authentification sur le pont Hue (" + hueBridge.hueIp + ") effectuée.");
            onAuthentification = false;

            // Nouvelle Activite
            Intent i = new Intent (getApplicationContext(), TestActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);


        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
            Log.d(TAG, "onAuthenticationRequired");
            phHueSDK.startPushlinkAuthentication(phAccessPoint);
            onAuthentification = true;
        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list) {
            HueBridgeManager myManager = new HueBridgeManager(getApplicationContext());
            for (PHAccessPoint myAccesPoint : list) {
                String ip           = myAccesPoint.getIpAddress();
                String id           = myAccesPoint.getBridgeId();
                String userName     = "";
                String macAddress   = myAccesPoint.getMacAddress();
                String token        = "";
                Log.d(TAG, "Pont Hue trouvé => " + id + " / " + ip);

                // Enregistrement du pont hue
                HueBridge hueBridge = new HueBridge(id, ip, userName, macAddress, token);
                myManager.addHueBridge(hueBridge);
            }
            onBridgeSearch = false;
        }

        @Override
        public void onError(int i, String s) {
            Log.d(TAG, "On Error " + i + " = " + s);
            switch (i){
                case 101:                                       // link button not pressed
                    break;
                default:                                        // Autres codes
                    sendMessage("On Error " + i + " = " + s);
            }
        }

        @Override
        public void onConnectionResumed(PHBridge phBridge) {

        }

        @Override
        public void onConnectionLost(PHAccessPoint phAccessPoint) {
            Log.d(TAG, "onConnectionLost");
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list) {
        }
    };

    private class findHueBridge extends AsyncTask<Void, Integer, Void> {

        private ProgressDialog                  progress;

        @Override
        protected void onPreExecute() {
            onBridgeSearch = true;
            progress = new ProgressDialog(StartActivity.this);
            progress.setTitle("Recherche des ponts Hue");
            progress.setMessage("Recheche en cours...");
            progress.setCancelable(true);
            progress.setIcon(R.drawable.hue_bridge);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setButton(DialogInterface.BUTTON_POSITIVE, "Annuler", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onBridgeSearch = false;
                }
            });
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Vérification de la recherche
            while (onBridgeSearch) {
                try {
                    Thread.sleep(1000);
                    Log.d(TAG, "Recherche des ponts en cours...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "Recheche terminée");
            progress.dismiss();
            // Tentative de connexion
            checkHueBridge(phHueSDK);
        }
    }

    private class onAuthentication extends AsyncTask<Void, Integer, Void> {

        private ProgressDialog                  progress;

        @Override
        protected void onPreExecute() {
            onAuthentification = true;
            progress = new ProgressDialog(StartActivity.this);
            progress.setTitle("Demande d'authentification");
            progress.setMessage("Authentification en cours...");
            progress.setCancelable(true);
            progress.setIcon(R.drawable.bridge_outline_push);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setButton(DialogInterface.BUTTON_POSITIVE, "Annuler", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onAuthentification = false;
                }
            });
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Attente d'authenfication
            while (onAuthentification) {
                try {
                    Thread.sleep(1000);
                    Log.d(TAG, "Attente d'authenfication...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG,"Authenfication réalisée");
            progress.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Initialisation du PHSDKListener
        phHueSDK = PHHueSDK.create();
        phHueSDK.getNotificationManager().registerSDKListener(listener);

        // Verification si un pont Hue est déjà enregistré
        if (!checkHueBridge(phHueSDK)) {
            // Recherche d'un pont Hue
            recherchePontHue(phHueSDK);
        }
    }

    /**
     *
     * @param phHueSDK
     * @return
     */
    private boolean checkHueBridge(PHHueSDK phHueSDK) {
        // Connection au pont
        HueBridgeManager hueBridgeManager = new HueBridgeManager(getApplicationContext());
        ArrayList<HueBridge> allBridge = hueBridgeManager.getAllHueBridge();
        if (allBridge.size() != 0) {
            for (HueBridge listHueBridge: allBridge) {
                // Connnection au premier pont hue
                HueBridge hueBridge = hueBridgeManager.getHueBridgeByNetwork(listHueBridge);
                PHAccessPoint lastAccessPoint  = new PHAccessPoint();
                lastAccessPoint.setIpAddress(hueBridge.hueIp);
                // Verification de "userName" pour identification au pont
                if (!hueBridge.hueUserName.isEmpty()) {
                    lastAccessPoint.setUsername(hueBridge.hueUserName);
                    phHueSDK.connect(lastAccessPoint);
                } else {
                    phHueSDK.connect(lastAccessPoint);
                    new onAuthentication().execute();
                }
            }
            return true;
        } else {
            return false;
        }

    }

    /**
     *
     * @param phHueSDK
     */
    private void recherchePontHue(PHHueSDK phHueSDK) {
        // Start a bridge search
        PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);
        new findHueBridge().execute();
    }

}
