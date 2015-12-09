package fr.free.couturier_remi_hd.huemyhouse.hueNotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fr.free.couturier_remi_hd.huemyhouse.hueActivity.BridgeActivity;

public class HueReceiver extends BroadcastReceiver {

    private String TAG = "[HueMyHouse][HueReceiver]";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        // Initialisation du PHSDKListener

        String action = intent.getAction();
        // Toast.makeText(context, action, Toast.LENGTH_LONG).show();
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d(TAG, "android.intent.action.BOOT_COMPLETED");
            // Execution du module d'ordonnanceur - ON BOOT
            context.startService(new Intent(context, HueService.class));
            // adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p fr.free.couturier_remi_hd.huemyhouse
            // Start BridgeActivity
            Intent tokenIntent = new Intent(context, BridgeActivity.class);
            tokenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(tokenIntent);

        }
    }
}
