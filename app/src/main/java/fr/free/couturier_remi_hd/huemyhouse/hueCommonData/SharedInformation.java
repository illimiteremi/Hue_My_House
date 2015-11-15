package fr.free.couturier_remi_hd.huemyhouse.hueCommonData;

import android.provider.BaseColumns;

/**
 * Created by rcouturi on 15/11/2015.
 */
public class SharedInformation {

    public static final class hueBridge implements BaseColumns {

        public static final String HUE_ID         = "ID";
        public static final String HUE_BRIDGE_ID  = "BRIDGE_ID";
        public static final String HUE_IP         = "IP";
        public static final String HUE_MAC_ADRESS = "MAC_ADRESS";
        public static final String HUE_WIFI_NAME  = "WIFI_NAME";
        public static final String HUE_USERNAME   = "USERNAME";
        public static final String HUE_TOKEN      = "TOKEN";

        private hueBridge() {}
    }



}
