package fr.free.couturier_remi_hd.huemyhouse.hueNotification;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HueLightManager;
import fr.free.couturier_remi_hd.huemyhouse.hueBridge.HuePHSDKListener;

public class GetNotification extends AccessibilityService {

    HueLightManager hueLightManager;
    private String TAG = "[HueMyHouse][GetNotification]";
    private String[] gmail = {"com.google.android.gm", "255", "0", "0"};

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {

            final String packagename = String.valueOf(event.getPackageName());
            Log.d(TAG, "Notification de  " + packagename.toString());
            if (HuePHSDKListener.onBridgeConnected) {
                hueLightManager = new HueLightManager(getApplicationContext());
                hueLightManager.alertSelect(255, 0, 0, 5000);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");

    }


}
