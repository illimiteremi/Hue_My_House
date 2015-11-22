package fr.free.couturier_remi_hd.huemyhouse.hueBridge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import fr.free.couturier_remi_hd.huemyhouse.R;

import static android.widget.Toast.makeText;


public class HueBridgeAuthentification extends Activity {

    static String TAG               = "[HueMyHouse][Authentification]";

    ImageView     imageBridge;
    boolean       onAuthentification;
    String        hueIp;
    String        hueUserName;
    HueBridge     hueBridge;
    boolean       hueBrideLed = false;

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent       = this.getIntent();
        onAuthentification  = intent.getExtras().getBoolean("onAuthentification");
        hueBridge           = new HueBridge("", intent.getExtras().getString("hueIp"), "", "", "");
        imageBridge         = (ImageView) findViewById(R.id.imageBridge);

        if(onAuthentification == true) {
            setContentView(R.layout.hue_bridge_authentification);
            Log.d(TAG,"Authentification du pont Hue (" + hueBridge.hueIp + ") en cours...");
        } else {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        imageBridge = (ImageView) findViewById(R.id.imageBridge);
        onAuthentification = intent.getExtras().getBoolean("onAuthentification");

        // Fin de demande d'authentification
        if(onAuthentification == false) {
            imageBridge.setImageResource(R.drawable.bridge_filled);
            String hueUserName    = intent.getExtras().getString("userName");
            HueBridgeManager hueBridgeManager = new HueBridgeManager(getApplicationContext());
            hueBridge = hueBridgeManager.getHueBridgeByNetwork(hueBridge);
            // Mise à jour de l'identifiant retourné par le pont
            hueBridge.hueUserName = hueUserName;
            // Mise à jour de la bdd
            hueBridgeManager.updateHueBridge(hueBridge);

            Log.d(TAG, "UserName = " + hueBridge.hueUserName);
            Log.d(TAG, "Authentification sur le pont Hue (" + hueBridge.hueIp + ") effectuée.");
            finish();
        }
    }
}
