package fr.free.couturier_remi_hd.huemyhouse.hueActivity;

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

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;

import java.util.ArrayList;

import fr.free.couturier_remi_hd.huemyhouse.R;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HueBridge;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HuePHSDKListener;
import fr.free.couturier_remi_hd.huemyhouse.hueGraph.ActionStyle;

public class BridgeActivity extends ActionBarActivity {

    static String TAG = "[HueMyHouse][StartActivity]";

    TextView messageTextView;
    ImageView plugBridgeImageView;
    Button rechercherButton;
    Button meethueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // TextView "Message"
        messageTextView = (TextView) findViewById(R.id.messageTextView);
        messageTextView.setText("1. Branchez le pont.\n" +
                "2. Connectez-le à votre routeur Wi-Fi à l'aide du câble LAN prévu à cet effet\n" +
                "3. Appuyez sur le bouton \"Rechercher\" ci-dessous");
        ActionStyle.setViewEnable(messageTextView, false);

        // ImageView "Branchement du pont Hue"
        plugBridgeImageView = (ImageView) findViewById(R.id.plugBridgeImageView);
        ActionStyle.setImageViewEnable(plugBridgeImageView, false);


        // Button "meethueButton"
        meethueButton = (Button) findViewById(R.id.meethueButton);
        ActionStyle.setButtonEnable(meethueButton, false);
        meethueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent tokenIntent = new Intent(getApplicationContext(), TestActivity.class);
                tokenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(tokenIntent);
            }
        });

        // Button "Rechercher"
        rechercherButton = (Button) findViewById(R.id.rechercherButton);
        rechercherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HuePHSDKListener.errorCode == HuePHSDKListener.BRIDGE_NOT_RESPONDING) {
                    // Si On Error 46 = bridge not responding
                    checkHueBridge();
                } else {
                    // Lancement nouvelle recherche de pont hue
                    recherchePontHue();
                }
            }
        });

        // Verification si un pont Hue est déjà enregistré
        if (!checkHueBridge()) {
            recherchePontHue();                 // Recherche d'un pont Hue
        }

    }

    /**
     * Verification de la présence d'un pont Hue déjà enregistré
     *
     * @return Boolean de resultat
     */
    private boolean checkHueBridge() {

        /**
         * Boite de dialogue : Attente d'authenfication...
         */
        class onAuthentication extends AsyncTask<Void, Integer, Void> {

            private ProgressDialog progress;

            @Override
            protected void onPreExecute() {
                HuePHSDKListener.onAuthentification = true;
                progress = new ProgressDialog(BridgeActivity.this);
                progress.setTitle("Demande d'authentification");
                progress.setMessage("Veuillez appuyer sur le bouton central de votre pont Hue.");
                progress.setCancelable(false);
                progress.setIcon(R.drawable.ic_action_bridge_pressed);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setButton(DialogInterface.BUTTON_POSITIVE, "Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HuePHSDKListener.onAuthentification = false;
                    }
                });
                progress.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                // Attente d'authenfication
                while (HuePHSDKListener.onAuthentification) {
                    try {
                        Thread.sleep(500);
                        Log.d(TAG, "Attente d'authenfication...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Log.d(TAG, "Authenfication réalisée");
                progress.dismiss();
                finish();
                Intent tokenIntent = new Intent(getApplicationContext(), TestActivity.class);
                tokenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(tokenIntent);
            }
        }

        /**
         * Boite de dialogue : Connexion en cours...
         */
        class waitHueConnection extends AsyncTask<Void, Integer, Void> {

            private ProgressDialog progress;

            @Override
            protected void onPreExecute() {
                HuePHSDKListener.onAuthentification = true;
                progress = new ProgressDialog(BridgeActivity.this);
                progress.setTitle("Connexion en cours");
                progress.setMessage("Veuillez patientez...");
                progress.setCancelable(false);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                // Attente de connexion
                while (HuePHSDKListener.onAuthentification) {
                    try {
                        Thread.sleep(1000);
                        Log.d(TAG, "Attente de connexion...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progress.dismiss();
                // Verification du token meethue pour connexion 4G
                if (!HuePHSDKListener.hueBridge.meetHueToken.isEmpty()) {
                    HuePHSDKListener.onMeethueMode = true;
                }

                // Verification du code erreur du pont
                if (HuePHSDKListener.errorCode == HuePHSDKListener.BRIDGE_NOT_RESPONDING) {
                    Log.d(TAG, "Le pont ne répond pas");
                    AlertDialog diaBox = noBridgeAskOption();
                    diaBox.show();
                } else {
                    Log.d(TAG, "Authenfication réalisée");
                    finish();
                    Intent tokenIntent = new Intent(getApplicationContext(), TestActivity.class);
                    tokenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(tokenIntent);
                }
            }
        }

        // Connection à un pont Hue
        ArrayList<HueBridge> allBridge = HuePHSDKListener.hueBridgeManager.getAllHueBridge();

        if (allBridge.size() != 0) {
            for (HueBridge listHueBridge : allBridge) {
                // Connnection au premier pont hue trouvé
                HuePHSDKListener.hueBridge = HuePHSDKListener.hueBridgeManager.getHueBridgeByNetwork(listHueBridge);
                PHAccessPoint lastAccessPoint = new PHAccessPoint();
                lastAccessPoint.setIpAddress(HuePHSDKListener.hueBridge.hueIp);

                // Verification du "userName" pour identification au pont
                if (!HuePHSDKListener.hueBridge.hueUserName.isEmpty()) {
                    // User enregistré (donc connection)
                    lastAccessPoint.setUsername(HuePHSDKListener.hueBridge.hueUserName);
                    HuePHSDKListener.phHueSDK.connect(lastAccessPoint);
                    new waitHueConnection().execute();
                } else {
                    // User non enregistré (donc demande d'authentification)
                    HuePHSDKListener.phHueSDK.connect(lastAccessPoint);
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

            private ProgressDialog progress;

            @Override
            protected void onPreExecute() {
                HuePHSDKListener.onBridgeSearch = true;
                progress = new ProgressDialog(BridgeActivity.this);
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
                while (HuePHSDKListener.onBridgeSearch) {
                    try {
                        Thread.sleep(500);
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
                if (HuePHSDKListener.errorCode == HuePHSDKListener.BRIDGE_NOT_FOUND) {
                    // Boite de dialogue "Aucun pont trouvé"
                    AlertDialog diaBox = noBridgeAskOption();
                    diaBox.show();
                } else {
                    // Tentatice de connexion
                    checkHueBridge();
                }
            }
        }

        // Debut de la recherche d'un pont Hue
        PHBridgeSearchManager sm = (PHBridgeSearchManager) HuePHSDKListener.phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);
        new findHueBridge().execute();
    }

    /**
     * Boite de dialogue "Pont non trouve"
     *
     * @return AlertDialog
     */
    private AlertDialog noBridgeAskOption() {
        AlertDialog noBridgeDialogBox = new AlertDialog.Builder(this)
                .setTitle("Aucun pont détecté")
                .setMessage("Nous ne parvenons pas à trouver le dernier pont connu sur ce réseau Wifi.")
                .setIcon(R.drawable.ic_action_warning)
                .setPositiveButton("Continuer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActionStyle.setViewEnable(messageTextView, true);
                        ActionStyle.setButtonEnable(rechercherButton, true);
                        ActionStyle.setImageViewEnable(plugBridgeImageView, true);
                        // Seulement si un token est enregistré
                        if (HuePHSDKListener.onMeethueMode) {
                            ActionStyle.setButtonEnable(meethueButton, true);
                        }
                    }
                })
                .create();
        return noBridgeDialogBox;
    }
}
