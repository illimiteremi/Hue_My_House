package fr.free.couturier_remi_hd.huemyhouse.hueBridge;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import fr.free.couturier_remi_hd.huemyhouse.R;

public class MeethueConnexion extends ActionBarActivity {

    WebView               hueWebWiew;
    Bundle                hueExtras;
    HueBridge             hueBridge = null;

    static  String        TAG               = "[HueMyHouse][MeetHue]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meethue_connexion);

        // Récuperation de l'identifiant hue
        hueExtras = getIntent().getExtras();
        hueBridge.hueIp = hueExtras.getString("hueIp");

        final HueManager hueManager = new HueManager(getApplicationContext());
        hueBridge                   = hueManager.getHueBridgeByNetwork(hueBridge);
        hueWebWiew                  = (WebView) findViewById(R.id.meethueview);

        WebSettings webSettings = hueWebWiew.getSettings();
        webSettings.setJavaScriptEnabled(true);
        hueWebWiew.loadUrl(hueBridge.getMeetHueToken());

        hueWebWiew.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("phhueapp://sdk/login/")) {
                   String hueToken = url.replace("phhueapp://sdk/login/","");
                    Log.d(TAG, "token = " +  hueToken);
                    hueBridge.meetHueToken = hueToken;
                    Boolean addResult = hueManager.updateHueBridge(hueBridge);
                    Log.d(TAG, "Mise à jour du pont hue = " + addResult);
                    finish();
                }
            }
        });

    }

}
