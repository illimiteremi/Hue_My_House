package fr.free.couturier_remi_hd.huemyhouse.hueActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLightState;

import fr.free.couturier_remi_hd.huemyhouse.R;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HuePHSDKListener;

public class ColorPickerActivity extends Activity {

    static String TAG = "[HueMyHouse][ColorPicker]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        final PHBridge bridge = HuePHSDKListener.phHueSDK.getSelectedBridge();
        final PHLightState lightState = new PHLightState();
        bridge.setLightStateForDefaultGroup(lightState);

        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        final ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        final TextView textView = (TextView) findViewById(R.id.textView);
        final Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        imageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x, y;
                int pixel;
                int redValue = 0;
                int blueValue = 0;
                int greenValue = 0;
                int color;

                try {
                    x = (int) event.getX();
                    y = (int) event.getY();
                    pixel = bitmap.getPixel(x, y);
                    redValue = Color.red(pixel);
                    blueValue = Color.blue(pixel);
                    greenValue = Color.green(pixel);
                    color = Color.rgb(redValue, greenValue, blueValue);
                    imageView2.setBackgroundColor(color);
                    textView.setText("RGB = " + redValue + " - " + blueValue + " - " + greenValue);

                } catch (Exception e) {
                    Log.d(TAG, "Error = " + e.getMessage());
                }

                float xy[] = PHUtilities.calculateXYFromRGB(redValue, greenValue, blueValue, "");             // Set RGB
                lightState.setX(xy[0]);
                lightState.setY(xy[1]);
                bridge.setLightStateForDefaultGroup(lightState);
                return true;
            }
        });

    }
}
