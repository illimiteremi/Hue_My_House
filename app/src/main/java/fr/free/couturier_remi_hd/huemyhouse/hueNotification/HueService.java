package fr.free.couturier_remi_hd.huemyhouse.hueNotification;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HueBridge;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HuePHSDKListener;

public class HueService extends Service {

    static String TAG = "[HueMyHouse][HueService]";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Initialisation du PHSDKListener
        Log.d(TAG, "Initialisation du PHSDKListener");
        new HuePHSDKListener(getApplicationContext());

        // Connection à un pont Hue
        ArrayList<HueBridge> allBridge = HuePHSDKListener.hueBridgeManager.getAllHueBridge();
        // Toast.makeText(this, "Service Hue My House Started", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
