package com.gbbtbb.homehub;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by etabli on 25/09/16.
 */
public class ZWaveWidgetMain extends BroadcastReceiver {

    public static final String CLICK_ACTION ="com.gbbtbb.zwavewidget.CLICK_ACTION";
    public static final String INITIALIZE_ACTION ="com.gbbtbb.zwavewidget.INITIALIZE_ACTION";
    public static final String REFRESH_ACTION ="com.gbbtbb.zwavewidget.REFRESH_ACTION";
    public static final String TOGGLE_ACTION ="com.gbbtbb.zwavewidget.TOGGLE_ACTION";

    public static final String STORE_REFRESH_TIME_ACTION ="com.gbbtbb.zwavewidget.STORE_REFRESH_TIME_ACTION";

    public static final String UPDATE_IMAGEVIEW_ACTION ="com.gbbtbb.zwavewidget.UPDATE_IMAGEVIEW_ACTION";
    public static final String UPDATE_IMAGEVIEW_EXTRA_VIEWID ="com.gbbtbb.zwavewidget.UPDATE_IMAGEVIEW_EXTRA_VIEWID";
    public static final String UPDATE_IMAGEVIEW_EXTRA_IMGID ="com.gbbtbb.zwavewidget.UPDATE_IMAGEVIEW_EXTRA_LEVEL";

    public static final String UPDATE_TEXTVIEW_ACTION ="com.gbbtbb.zwavewidget.UPDATE_TEXTVIEW_ACTION";
    public static final String UPDATE_TEXTVIEW_EXTRA_ID ="com.gbbtbb.zwavewidget.UPDATE_TEXTVIEW_EXTRA_ID";
    public static final String UPDATE_TEXTVIEW_EXTRA_TEXT ="com.gbbtbb.zwavewidget.UPDATE_TEXTVIEW_EXTRA_TEXT";


    public static final String STORE_REFRESH_TIME_EXTRA ="com.gbbtbb.zwavewidget.STORE_REFRESH_TIME_EXTRA";
    public static final String LATEST_REFRESH_EXTRA ="com.gbbtbb.zwavewidget.LATEST_REFRESH_EXTRA";
    public static final String DEVICE_NAME_EXTRA ="com.gbbtbb.zwavewidget.DEVICE_NAME_EXTRA";

    static long latestRefreshUnixTime = 0;


    public ZWaveWidgetMain() {

    }

    Activity activity;
    Context context;
    public ZWaveWidgetMain(Activity activity,Context context) {
        this.activity=activity;
        this.context=context;
    }

    public void update(Context context) {

        Log.i("ZWaveWidgetProvider", "update called...");

        // Parse the list of all declared devices, retrieve the associated imageView for each device, and register a click event on it
        String[] deviceList = context.getResources().getStringArray(R.array.deviceList);
        for (String d : deviceList) {
            int arrayId = context.getResources().getIdentifier(d, "array", context.getPackageName());
            String[] temp = context.getResources().getStringArray(arrayId);

            // Get resource identifier of ImageView for this device
            int imageViewId = context.getResources().getIdentifier(temp[6], "id", context.getPackageName());

            // Register a click intent on it
            //TODO
        }

        // TODO update widget

        // Initial call to the service to get the first batch of data to refresh the UI
        Intent intent = new Intent(context.getApplicationContext(), ZWaveWidgetService.class);
        intent.putExtra(LATEST_REFRESH_EXTRA, latestRefreshUnixTime);
        intent.setAction(INITIALIZE_ACTION);
        context.startService(intent);

        Log.i("ZWaveWidgetMain", "onUpdate: background service started");
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();

        Log.i("ZWaveWidgetMain", "onReceive " + action);

        if (CLICK_ACTION.equals(action)) {

            String deviceName = intent.getStringExtra(DEVICE_NAME_EXTRA);
            Log.i("ZWaveWidgetMain", "onReceive CLICK devName=" + deviceName);

            // Build the intent to call the service
            Intent i = new Intent(ctx.getApplicationContext(), ZWaveWidgetService.class);
            i.putExtra(DEVICE_NAME_EXTRA, deviceName);
            i.setAction(TOGGLE_ACTION);
            ctx.startService(i);
        }
        else if (STORE_REFRESH_TIME_ACTION.equals(action)) {

            latestRefreshUnixTime = intent.getLongExtra(STORE_REFRESH_TIME_EXTRA, 0);
            //Log.i("ZWaveWidgetProvider", "Updating latestRefreshUnixTime to " + Long.toString(latestRefreshUnixTime));
        }
        else if (UPDATE_IMAGEVIEW_ACTION.equals(action)) {
            int imageViewId = intent.getIntExtra(UPDATE_IMAGEVIEW_EXTRA_VIEWID, 0);
            int imgId = intent.getIntExtra(UPDATE_IMAGEVIEW_EXTRA_IMGID, 0);
            Log.i("ZWaveWidgetMain", "update ImageView: " + Integer.toString(imageViewId)+ ", " + Integer.toString(imgId));

            Log.i("ZWaveWidgetMain", "update ImageView: acitvity is" + activity);

        }
        else if (UPDATE_TEXTVIEW_ACTION.equals(action)) {
            int txtViewId = intent.getIntExtra(UPDATE_TEXTVIEW_EXTRA_ID, 0);
            String txt = intent.getStringExtra(UPDATE_TEXTVIEW_EXTRA_TEXT);
            Log.i("ZWaveWidgetMain", "update ImageView: " + Integer.toString(txtViewId)+ ", " + txt);

        }
    }
}
