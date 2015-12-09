package fr.free.couturier_remi_hd.huemyhouse.hueNotification;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HueLightManager;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HuePHSDKListener;

public class NotificationReceiver extends BroadcastReceiver {

    HueLightManager hueLightManager;
    private String TAG = "[HueMyHouse][NotificationReceiver]";

    private PHBridge bridge;
    private PHLightState lightState;

    public NotificationReceiver() {
        bridge = HuePHSDKListener.phHueSDK.getSelectedBridge();
        lightState = new PHLightState();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d(TAG, intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            Log.d(TAG, "ACTION_NEW_OUTGOING_CALL");
        } else {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            switch (tm.getCallState()) {

                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "CALL_STATE_RINGING");
                    lightState.setAlertMode(PHLight.PHLightAlertMode.ALERT_LSELECT);
                    bridge.setLightStateForDefaultGroup(lightState);
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "CALL_STATE_OFFHOOK");
                    lightState.setEffectMode(PHLight.PHLightEffectMode.EFFECT_NONE);
                    bridge.setLightStateForDefaultGroup(lightState);
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "CALL_STATE_IDLE");
                    lightState.setEffectMode(PHLight.PHLightEffectMode.EFFECT_NONE);
                    bridge.setLightStateForDefaultGroup(lightState);
                    break;
            }
        }

    }
}
