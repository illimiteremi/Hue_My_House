package fr.free.couturier_remi_hd.huemyhouse.hueGraph;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by XZAQ496 on 26/11/2015.
 */
public class ActionStyle {
    /**
     * Modification de l'etat du bouton
     *
     * @param button
     * @param state
     */
    public static void setButtonEnable(Button button, Boolean state) {
        if (state) {
            // set enabled
            button.setAlpha(1f);
            button.setEnabled(true);
        } else {
            // set disabled
            button.setAlpha(.5f);
            button.setEnabled(false);
        }
    }

    /**
     * Modification de l'etat d'un TextView
     *
     * @param textView
     * @param state
     */
    public static void setViewEnable(TextView textView, Boolean state) {
        if (state) {
            // set enabled
            textView.setAlpha(1f);
        } else {
            // set disabled
            textView.setAlpha(.5f);
        }
    }

    /**
     * Modification de l'etat d'un imageview
     *
     * @param imageView
     * @param state
     */
    public static void setImageViewEnable(ImageView imageView, Boolean state) {
        if (state) {
            // set enabled
            imageView.setAlpha(1f);
        } else {
            // set disabled
            imageView.setAlpha(.5f);
        }
    }

    /**
     * Modification de l'etat d'un ToggleButton
     *
     * @param toggleLightButton
     * @param state
     */
    public static void setToggleButtonEnable(ToggleButton toggleLightButton, Boolean state) {
        if (state) {
            // set enabled
            toggleLightButton.setEnabled(true);
            toggleLightButton.setAlpha(1f);
        } else {
            // set disabled
            toggleLightButton.setEnabled(false);
            toggleLightButton.setAlpha(.5f);
        }
    }


}
