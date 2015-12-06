package fr.free.couturier_remi_hd.huemyhouse.hueActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import fr.free.couturier_remi_hd.huemyhouse.R;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HueLight;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HueLightManager;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HuePHSDKListener;
import fr.free.couturier_remi_hd.huemyhouse.hueGraph.ActionStyle;

public class TestActivity extends ActionBarActivity {

    static  String        TAG                  = "[HueMyHouse][TestActivity]";

    Button                buttonAlarmMode;
    Button                buttonEffectMode;
    Button                buttonMyhue;
    Button buttonMyhueTest;
    Button buttonColorPicker;

    HueLightManager hueLightManager;

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume...");
        setButtonState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        hueLightManager = new HueLightManager(getApplicationContext());

        buttonAlarmMode = (Button) findViewById(R.id.buttonAlert);
        buttonEffectMode = (Button) findViewById(R.id.buttonEffect);
        buttonMyhue = (Button) findViewById(R.id.buttonMyHue);
        buttonMyhueTest = (Button) findViewById(R.id.buttonMyHueTest);
        buttonColorPicker = (Button) findViewById(R.id.buttonColorPicker);

        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        //startActivityForResult(intent, 0);

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

        buttonMyhue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Vérification de la présence de l' adresse IP du pont
                if (!HuePHSDKListener.onMeethueMode) {
                    HuePHSDKListener.hueBridgeManager.getMeetHueToken(getApplicationContext(), HuePHSDKListener.hueBridge);
                }
            }
        });

        buttonMyhueTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meethueTest();
            }
        });


        // Button "meethueButton"
        buttonColorPicker = (Button) findViewById(R.id.buttonColorPicker);
        ActionStyle.setButtonEnable(buttonColorPicker, true);
        buttonColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent tokenIntent = new Intent(getApplicationContext(), ColorPickerActivity.class);
                tokenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(tokenIntent);
            }
        });

        // Attente de connexion au pont Hue
        while ((HuePHSDKListener.onConnectionResume == false)  &&  (HuePHSDKListener.onMeethueMode == false)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Permet d'afficher les boutons correspondant à l'etat de connexion
     */
    private void setButtonState() {

        Log.d(TAG, "Maj des boutons...");

        ActionStyle.setButtonEnable(buttonAlarmMode, true);
        ActionStyle.setButtonEnable(buttonEffectMode, true);
        ActionStyle.setButtonEnable(buttonMyhue, true);
        ActionStyle.setButtonEnable(buttonMyhueTest, true);
        ActionStyle.setButtonEnable(buttonColorPicker, true);

        // Affichage suivant l'etat de connexion

        // Si pas de connection wifi
        if (!HuePHSDKListener.onBridgeConnected){
            ActionStyle.setButtonEnable(buttonAlarmMode, false);
            ActionStyle.setButtonEnable(buttonEffectMode, false);
            ActionStyle.setButtonEnable(buttonColorPicker, false);
        }

        // Si token de connexion meetHue présent
        if (HuePHSDKListener.onMeethueMode) {
            ActionStyle.setButtonEnable(buttonMyhueTest, true);
            ActionStyle.setButtonEnable(buttonMyhue, false);
        } else {
            ActionStyle.setButtonEnable(buttonMyhueTest, false);
            ActionStyle.setButtonEnable(buttonMyhue, true);
        }
    }

    /**
     * Declanche un clignotement pendant xx seconde(s)
     * @param alarmTime
     */
    public void alertMode(final int alarmTime) {

        new Thread() {
            public void run() {
                PHBridge bridge = HuePHSDKListener.phHueSDK.getSelectedBridge();
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
                PHBridge bridge = HuePHSDKListener.phHueSDK.getSelectedBridge();
                PHLightState lightState = new PHLightState();
                lightState.setOn(true);
                // Start effect mode
                lightState.setEffectMode(PHLight.PHLightEffectMode.EFFECT_COLORLOOP);
                bridge.setLightStateForDefaultGroup(lightState);
            }
        }.start();
    }

    /**
     * Test des lumieres via internet
     */
    public void meethueTest() {
        hueLightManager.meethueAction(HueLight.ALERT_MODE);
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
