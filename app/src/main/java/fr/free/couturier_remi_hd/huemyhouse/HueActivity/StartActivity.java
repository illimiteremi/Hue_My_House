package fr.free.couturier_remi_hd.huemyhouse.HueActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    Boolean              onBridgeSearch     = false;                // Etat de recherche
    Boolean              onAuthentification = false;                // Etat d'authentification
    int                  onErrorCode        = 0;                    // Code erreur du PHSDKListener

    TextView             messageTextView;
    ImageView            plugBridgeImageView;
    Button               rechercherButton;

    private PHSDKListener listener = new PHSDKListener() {

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
        public void onError(int errorCode, String errorMessage) {
            Log.d(TAG, "On Error " + errorCode + " = " + errorMessage);
            onErrorCode = errorCode;
            switch (errorCode){
                case 101:                                       // link button not pressed
                    break;
                case 1157:                                      // No bridge found
                    onBridgeSearch = false;
                    break;
                case 46:                                        // bridge not responding
                    onAuthentification = false;
                    break;
                default:                                        // Autres codes
            }
        }

        @Override
        public void onConnectionResumed(PHBridge phBridge) {

        }

        @Override
        public void onConnectionLost(PHAccessPoint phAccessPoint) {
            Log.d(TAG, "onConnectionLost");
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Initialisation de la TextView "Message"
        messageTextView = (TextView) findViewById(R.id.messageTextView);
        messageTextView.setText("1. Branchez le pont.\n" +
                "2. Connectez-le à votre routeur Wi-Fi à l'aide du câble LAN prévu à cet effet\n" +
                "3. Appuyez sur le bouton \"Rechercher\" ci-dessous");
        messageTextView.setVisibility(View.INVISIBLE);

        // Initialisation de l'imageView
        plugBridgeImageView = (ImageView) findViewById(R.id.plugBridgeImageView);
        plugBridgeImageView.setVisibility(View.INVISIBLE);

        // Initialisation du Boutton "Rechercher"
        rechercherButton = (Button) findViewById(R.id.rechercherButton);
        rechercherButton.setVisibility(View.INVISIBLE);
        rechercherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onErrorCode == 46) {
                    // Si On Error 46 = bridge not responding
                    checkHueBridge();
                } else {
                    // Lancement nouvelle recherche de pont hue
                    recherchePontHue();
                }

            }
        });

        // Initialisation du PHSDKListener
        phHueSDK = PHHueSDK.create();
        phHueSDK.getNotificationManager().registerSDKListener(listener);

        // Verification si un pont Hue est déjà enregistré
        if (!checkHueBridge()) {
            recherchePontHue();                 // Recherche d'un pont Hue
        }
    }

    /**
     * Verification de la présence d'un pont Hue déjà enregistré
     * @return Boolean de resultat
     */
    private boolean checkHueBridge() {

        class onAuthentication extends AsyncTask<Void, Integer, Void> {

            private ProgressDialog                  progress;

            @Override
            protected void onPreExecute() {
                onAuthentification = true;
                progress = new ProgressDialog(StartActivity.this);
                progress.setTitle("Demande d'authentification");
                progress.setMessage("Veuillez appuyer sur le bouton central de votre pont Hue.");
                progress.setCancelable(false);
                progress.setIcon(R.drawable.ic_action_bridge_pressed);
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

        class waitHueConnection extends AsyncTask<Void, Integer, Void> {

            private ProgressDialog                  progress;

            @Override
            protected void onPreExecute() {
                onAuthentification = true;
                progress = new ProgressDialog(StartActivity.this);
                progress.setTitle("Connexion en cours");
                progress.setMessage("Veuillez patientez...");
                progress.setCancelable(false);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
                progress.dismiss();
                if (onErrorCode == 46) {
                    Log.d(TAG,"Le pont ne répond pas");
                    AlertDialog diaBox = noBridgeAskOption();
                    diaBox.show();
                } else {
                    Log.d(TAG,"Authenfication réalisée");
                }
            }
        }

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
                    new waitHueConnection().execute();
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
     * Recherche de ponts Hue sur le reseau
     */
    private void recherchePontHue() {

        class findHueBridge extends AsyncTask<Void, Integer, Void> {

            private ProgressDialog                  progress;

            @Override
            protected void onPreExecute() {
                onBridgeSearch = true;
                progress = new ProgressDialog(StartActivity.this);
                progress.setTitle("Recherche de ponts Hue");
                progress.setMessage("Recheche en cours...");
                progress.setIcon(R.drawable.ic_hue_bridge);
                progress.setCancelable(false);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
                // Tentative de connexion si pont trouvé
                if (onErrorCode == 1157) {
                    AlertDialog diaBox = noBridgeAskOption();
                    diaBox.show();
                } else {
                    checkHueBridge();
                }

            }
        }

        // Start a bridge search
        PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);
        new findHueBridge().execute();
    }

    /**
     * Boite de dialogue "Pont non trouve"
     * @return AlertDialog
     */
    private AlertDialog noBridgeAskOption() {
                AlertDialog noBridgeDialogBox = new AlertDialog.Builder(this)
                .setTitle("Aucun pont connu détecté")
                .setMessage("Nous ne parvenons pas à trouver le dernier pont connu sur ce réseau Wifi.")
                .setIcon(R.drawable.ic_action_warning)
                .setPositiveButton("Continuer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        messageTextView.setVisibility(View.VISIBLE);
                        rechercherButton.setVisibility(View.VISIBLE);
                        plugBridgeImageView.setVisibility(View.VISIBLE);
                    }
                })
                .create();
        return noBridgeDialogBox;
    }
}
