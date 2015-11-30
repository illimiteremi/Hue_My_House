package fr.free.couturier_remi_hd.huemyhouse.hueBridge;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import fr.free.couturier_remi_hd.huemyhouse.R;
import fr.free.couturier_remi_hd.huemyhouse.hueActivity.TestActivity;

public class MeethueConnexion extends ActionBarActivity {

    static  String        TAG                  = "[HueMyHouse][MeetHue]";
    WebView hueWebWiew;

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
