package fr.free.couturier_remi_hd.huemyhouse.HueActivity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import fr.free.couturier_remi_hd.huemyhouse.R;

public class TestActivity extends ActionBarActivity {

    static  String        TAG               = "[HueMyHouse][Listener]";

    PHHueSDK              phHueSDK;

    Button                buttonAlarmMode;
    Button                buttonEffectMode;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        buttonAlarmMode  = (Button) findViewById(R.id.buttonAlert);
        buttonEffectMode = (Button) findViewById(R.id.buttonEffect);

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
