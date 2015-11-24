package fr.free.couturier_remi_hd.huemyhouse.hueBridge;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueParsingError;

import java.util.List;

import fr.free.couturier_remi_hd.huemyhouse.HueActivity.TestActivity;
import fr.free.couturier_remi_hd.huemyhouse.R;

public class MeethueConnexion extends ActionBarActivity {

    WebView               hueWebWiew;
    PHHueSDK              phHueSDK;
    HueBridge             hueBridge;
    HueBridgeManager      hueBridgeManager;

    static  String        TAG                  = "[HueMyHouse][MeetHue]";
    static  String        hueBrideIP           = "";
    boolean               isOnConnectionResume = false;

    public PHSDKListener listener = new PHSDKListener() {

        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
        }

        @Override
        public void onBridgeConnected(PHBridge phBridge, String userName) {
            Log.d(TAG, "Le pont Hue est connecté");
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
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
            PHBridgeResourcesCache cache = phBridge.getResourceCache();
            PHBridgeConfiguration bridge = cache.getBridgeConfiguration();
            hueBrideIP                   = bridge.getIpAddress();
            hueBridgeManager             = new HueBridgeManager(getApplicationContext());
            hueBridge                    = new HueBridge("", hueBrideIP, "", "", "");
            hueBridge                    = hueBridgeManager.getHueBridgeByNetwork(hueBridge);
            isOnConnectionResume         = true;
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
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meethue_connexion);

        phHueSDK = PHHueSDK.create();
        phHueSDK.getNotificationManager().registerSDKListener(listener);

        // Récuperation de l'identifiant hue
        hueWebWiew              = (WebView) findViewById(R.id.meethueview);

        // Attente de connexion au pont Hue
        while (!isOnConnectionResume) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        WebSettings webSettings = hueWebWiew.getSettings();
        webSettings.setJavaScriptEnabled(true);
        hueWebWiew.loadUrl(hueBridge.getMeetHueToken());

        hueWebWiew.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("phhueapp://sdk/login/")) {
                    String hueToken = url.replace("phhueapp://sdk/login/", "");
                    Log.d(TAG, "token = " + hueToken);
                    hueBridge.meetHueToken = hueToken;
                    Boolean addResult = hueBridgeManager.updateHueBridge(hueBridge);
                    Log.d(TAG, "Mise à jour du pont hue = " + addResult);
                    // fermeture de l'activity
                    finish();

                    // Ouverture de l'activitée de Test
                    Intent i = new Intent(getApplicationContext(), TestActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            }
        });

    }

}
