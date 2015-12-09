package fr.free.couturier_remi_hd.huemyhouse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import fr.free.couturier_remi_hd.huemyhouse.hueActivity.BridgeActivity;
import fr.free.couturier_remi_hd.huemyhouse.hueNotification.HueService;

public class MainHueActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisation du PHSDKListener
        startService(new Intent(getBaseContext(), HueService.class));

        // Start BridgeActivity
        Intent tokenIntent = new Intent(getApplicationContext(), BridgeActivity.class);
        tokenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(tokenIntent);
    }

}
