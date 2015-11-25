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

    static  String        TAG                  = "[HueMyHouse][MeetHue]";

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meethue_connexion);

        // Récuperation de l'identifiant hue
        hueWebWiew = (WebView) findViewById(R.id.meethueview);

        // Attente de connexion au pont Hue
        while (!HuePHSDKListener.onConnectionResume) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        WebSettings webSettings = hueWebWiew.getSettings();
        webSettings.setJavaScriptEnabled(true);
        hueWebWiew.loadUrl(HuePHSDKListener.hueBridge.getMeetHueToken());

        hueWebWiew.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("phhueapp://sdk/login/")) {
                    String hueToken = url.replace("phhueapp://sdk/login/", "");
                    Log.d(TAG, "token = " + hueToken);
                    HuePHSDKListener.hueBridge.meetHueToken = hueToken;
                    Boolean addResult = HuePHSDKListener.hueBridgeManager.updateHueBridge(HuePHSDKListener.hueBridge);
                    Log.d(TAG, "Mise à jour du pont hue = " + addResult);

                    // Ouverture de l'activitée de Test
                    HuePHSDKListener.onMeethueMode = true;
                    Intent i = new Intent(getApplicationContext(), TestActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                }
            }
        });

    }

}
