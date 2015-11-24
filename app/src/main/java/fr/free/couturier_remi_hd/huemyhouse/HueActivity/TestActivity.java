package fr.free.couturier_remi_hd.huemyhouse.HueActivity;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import fr.free.couturier_remi_hd.huemyhouse.R;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HueBridge;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HueBridgeManager;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.MeethueConnexion;

public class TestActivity extends ActionBarActivity {

    static  String        TAG                  = "[HueMyHouse][TestActivity]";

    PHHueSDK              phHueSDK;
    String                hueBrideIP           = "";
    boolean               isOnConnectionResume = false;

    Button                buttonAlarmMode;
    Button                buttonEffectMode;
    Button                buttonMyhue;
    Button                MyHueTestButton;

    HueBridge             hueBridge;

    public PHSDKListener listener = new PHSDKListener() {

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
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
            Log.d(TAG, "onAuthenticationRequired");
            phHueSDK.startPushlinkAuthentication(phAccessPoint);
        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list) {
        }

        @Override
        public void onError(int i, String s) {
            Log.d(TAG, "On Error " + i + " = " + s);
        }

        @Override
        public void onConnectionResumed(PHBridge phBridge) {
            Log.d(TAG, "onConnectionResumed");
            // Recuperation de l'adresse IP du pont
            PHBridgeResourcesCache cache      = phBridge.getResourceCache();
            PHBridgeConfiguration bridge      = cache.getBridgeConfiguration();
            hueBrideIP                        = bridge.getIpAddress();
            HueBridgeManager hueBridgeManager = new HueBridgeManager(getApplicationContext());
            hueBridge                         = new HueBridge("", hueBrideIP, "", "", "");
            hueBridge                         = hueBridgeManager.getHueBridgeByNetwork(hueBridge);
            isOnConnectionResume              = true;
        }

        @Override
        public void onConnectionLost(PHAccessPoint phAccessPoint) {
            Log.d(TAG, "onConnectionLost");
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list) {
        }
    };

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        buttonAlarmMode  = (Button) findViewById(R.id.buttonAlert);
        buttonEffectMode = (Button) findViewById(R.id.buttonEffect);
        buttonMyhue      = (Button) findViewById(R.id.buttonMyHue);
        MyHueTestButton  = (Button) findViewById(R.id.MyHueTestButton);

        phHueSDK = PHHueSDK.create();
        phHueSDK.getNotificationManager().registerSDKListener(listener);

        buttonEffectMode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                effectMode();
            }
        });

        buttonAlarmMode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alertMode(10000);
            }
        });

        buttonMyhue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Vérification de la présence de l' adresse IP du pont
                if (!hueBrideIP.isEmpty()) {
                    HueBridgeManager hueBridgeManager = new HueBridgeManager(getApplicationContext());
                    HueBridge hueBridge = new HueBridge("", hueBrideIP, "", "", "");
                    hueBridge = hueBridgeManager.getHueBridgeByNetwork(hueBridge);
                    hueBridgeManager.getMeetHueToken(getApplicationContext(), hueBridge);
                } else {
                    // Message box d'erreur
                    AlertDialog noBridgeDialogBox = new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Problème de connexion à votre compte Hue")
                            .setMessage("Nous ne parvenons pas à identifer votre pont hue")
                            .setIcon(R.drawable.ic_action_warning)
                            .setPositiveButton("Annuler", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create();
                    noBridgeDialogBox.show();
                }
            }
        });

        MyHueTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meethueTest();
            }
        });

        // Attente de connexion au pont Hue
        while (!isOnConnectionResume) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Si token de connexion alors bouton Visible
        if (!hueBridge.meetHueToken.isEmpty()) {
            MyHueTestButton.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Declanche un clignotement pendant xx seconde(s)
     * @param alarmTime
     */
    public void alertMode(final int alarmTime) {

        new Thread() {
            public void run() {
                PHBridge bridge = phHueSDK.getSelectedBridge();
                PHLightState lightState = new PHLightState();

                lightState.setEffectMode(PHLight.PHLightEffectMode.EFFECT_NONE);
                bridge.setLightStateForDefaultGroup(lightState);

                // Start blinking for up to xx seconds
                float xy[] = PHUtilities.calculateXYFromRGB(255, 0, 0,"");             // RED
                lightState.setX(xy[0]);
                lightState.setY(xy[1]);
                lightState.setAlertMode(PHLight.PHLightAlertMode.ALERT_LSELECT);
                bridge.setLightStateForDefaultGroup(lightState);

                try {
                    Thread.sleep(alarmTime);
                } catch (InterruptedException e) {}

                // Stop blinking
                xy = PHUtilities.calculateXYFromRGB(255, 255, 255, "LCT001");       // WHITE
                lightState.setX(xy[0]);
                lightState.setY(xy[1]);
                lightState.setAlertMode(PHLight.PHLightAlertMode.ALERT_NONE);
                bridge.setLightStateForDefaultGroup(lightState);
           }
        }.start();
    }

    /**
     *
     */
    public void effectMode() {
        new Thread() {
            public void run() {
                PHBridge bridge = phHueSDK.getSelectedBridge();
                PHLightState lightState = new PHLightState();
                lightState.setOn(true);
                // Start effect mode
                lightState.setEffectMode(PHLight.PHLightEffectMode.EFFECT_COLORLOOP);
                bridge.setLightStateForDefaultGroup(lightState);
            }
        }.start();
    }

    public void meethueTest() {

        // String urlGet  = "https://www.meethue.com/api/getbridge?token=" + hueBridge.meetHueToken+"&bridgeid=" + hueBridge;
        String urlPost = "https://www.meethue.com/api/sendmessage?token=" + hueBridge.meetHueToken;
        Log.d(TAG, urlPost);

        final String huecommand = "{ bridgeId: \"" + hueBridge.hueId +"\", clipCommand: { url: \"/api/" + hueBridge.hueUserName + "/groups/0/action\", method: \"PUT\", body: {\"alert\":\"select\"}}}";
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
