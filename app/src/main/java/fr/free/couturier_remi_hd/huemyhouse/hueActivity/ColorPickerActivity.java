package fr.free.couturier_remi_hd.huemyhouse.hueActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import fr.free.couturier_remi_hd.huemyhouse.R;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HueLightManager;

public class ColorPickerActivity extends Activity {

    static String TAG = "[HueMyHouse][ColorPicker]";

    int width;
    int height;
    ImageView imageView, imageView2;
    ImageView ampouleView;
    Bitmap bitmap;
    RelativeLayout relativeLayout;
    RelativeLayout.LayoutParams params;
    HueLightManager hueLightManager;
    Button lightButton;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Redimensionnement de l'image "final_palette"
        width = imageView.getWidth();
        height = imageView.getHeight();
        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, width, height, true));

        // Création du curseur Ampoule
        ampouleView = new ImageView(this);
        ampouleView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ampoule));
        params = new RelativeLayout.LayoutParams(70, 70);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        hueLightManager = new HueLightManager(getApplicationContext());
        relativeLayout = (RelativeLayout) findViewById(R.id.color_picker);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView = (ImageView) findViewById(R.id.imageView);
        lightButton = (Button) findViewById(R.id.lightButton);

        lightButton.setOnClickListener(new View.OnClickListener() {

            Boolean setOn = false;

            @Override
            public void onClick(View v) {
                if (setOn) {
                    hueLightManager.setAllLightsOnOff(true);
                    setOn = false;
                } else {
                    hueLightManager.setAllLightsOnOff(false);
                    setOn = true;
                }
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                try {
                    bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    int x = (int) event.getX() - 35;
                    int y = (int) event.getY() - 35;
                    int pixel = bitmap.getPixel(x, y);
                    int red = Color.red(pixel);
                    int green = Color.green(pixel);
                    int blue = Color.blue(pixel);
                    int color = Color.rgb(red, green, blue);
                    imageView2.setBackgroundColor(color);

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            setColors(red, green, blue);
                            break;
                        default:
                            setCursor(x, y);
                }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur : " + e.getMessage());
                    return false;
                }
                return true;
            }
        });

    }

    /**
     * Déplacement du curseur des couleurs
     *
     * @param x
     * @param y
     */
    private void setCursor(int x, int y) {
        Log.d(TAG, "setCursor : X = " + x + " Y = " + y);
        relativeLayout.removeView(ampouleView);
        params.leftMargin = x;
        params.topMargin = y;
        relativeLayout.addView(ampouleView, params);
    }

    /**
     * Mise à jour de la couleur des ampoules
     *
     * @param red
     * @param green
     * @param blue
     * @return resultat de mise à jour
     */
    private boolean setColors(int red, int green, int blue) {
        Log.d(TAG, "setColors : RGB = " + red + "/" + green + "/" + blue);
        // Mise à jour des ampoules sur le pont Hue
        hueLightManager.changeAllLightsColor(red, green, blue);
        return true;
    }
}
