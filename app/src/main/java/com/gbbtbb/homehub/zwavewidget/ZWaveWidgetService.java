package com.gbbtbb.homehub.zwavewidget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.gbbtbb.homehub.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ZWaveWidgetService extends IntentService {

    public static final String TAG = "ZWaveWidgetService";

    public ZWaveWidgetService() {
        super(ZWaveWidgetService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final String action = intent.getAction();

        /* XML config data structure for each device
        First the device, instance, and command class number used for Get and Set commands

        0 = devId_setget
        1 = instanceId_setget
        2 = commandclass_setget

        Then the device, instance, and command class number used to detect changed when parsing incremental data updates.
        In 99% of the cases, those are identical to 0,1,2
        But for at least one plug device, for some reason when the device is manually switched, the change is detected/notified with a different dev/instance/class.

        3 = devId_notified
        4 = instanceId_notified
        5 = commandclass_notified

        Then the android Id for the associated ImageView and TextView
        6 = imageViewId
        7 = textViewId

        Finally the base name for the image resource to be used to show the on/off state
        8 = icontype
        */

        if (ZWaveWidgetMain.INITIALIZE_ACTION.equals(action)) {

            Log.i(TAG, "onHandleIntent INITIALIZE_ACTION");

            // retrieve server-side timestamp, since this will be used in incremental updates
            getServerTime();

            String[] deviceList = getResources().getStringArray(R.array.ZWaveDeviceList);

            // now get data for every device
            for (String d : deviceList) {
                int arrayId = this.getResources().getIdentifier(d, "array", this.getPackageName());
                String[] temp = getResources().getStringArray(arrayId);
                forceRefreshDevice(temp[0], temp[1], temp[2], temp[6], temp[7], temp[8], d);
            }

        } else if (ZWaveWidgetMain.REFRESH_ACTION.equals(action)) {

            long lastRefreshTime = intent.getExtras().getLong(ZWaveWidgetMain.LATEST_REFRESH_EXTRA);
            //Log.i(TAG, "onHandleIntent REFRESH_ACTION date=" + Long.toString(lastRefreshTime));

            // Get all state changes since last update from the z-way server
            JSONObject jdata = getIncrementalUpdate(lastRefreshTime);

            // Parse the list of all declared devices, and figure out if a UI update is required
            if (jdata != null) {
                String[] deviceList = getResources().getStringArray(R.array.ZWaveDeviceList);
                for (String d : deviceList) {
                    int arrayId = this.getResources().getIdentifier(d, "array", this.getPackageName());
                    String[] temp = getResources().getStringArray(arrayId);
                    refreshDevice(jdata, temp[0], temp[1], temp[2], temp[3], temp[4], temp[5], temp[6], temp[7], temp[8], d);
                }
            }
            else
                Log.e(TAG, "NULL jdata after getIncrementalUpdate");

        }  else if (ZWaveWidgetMain.TOGGLE_ACTION.equals(action)) {

            String deviceName = intent.getExtras().getString(ZWaveWidgetMain.DEVICE_NAME_EXTRA);
            Log.i(TAG, "onHandleIntent TOGGLE_ACTION, toggle " + deviceName);
            int arrayId = this.getResources().getIdentifier(deviceName, "array", this.getPackageName());
            String[] temp = getResources().getStringArray(arrayId);
            toggleDevice(temp[0], temp[1], temp[2], temp[6], temp[7], temp[8], deviceName);
        }
    }

    private JSONObject getIncrementalUpdate(long timestamp){
        String query_Data = String.format(this.getResources().getString(R.string.zwaveserver_base) + "/ZWaveAPI/Data/%d", timestamp);
        String result = httpRequest(query_Data);

        String updateTime ="";
        JSONObject jdata = null;

        // Parse the received JSON data
        try {
            jdata = new JSONObject(result);
            updateTime = jdata.getString("updateTime");

            // Notify provider of data refresh time
            Intent storeTimeIntent = new Intent();
            storeTimeIntent.setAction(ZWaveWidgetMain.STORE_REFRESH_TIME_ACTION);
            storeTimeIntent.putExtra(ZWaveWidgetMain.STORE_REFRESH_TIME_EXTRA, Long.valueOf(updateTime));
            storeTimeIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(storeTimeIntent);

        } catch(JSONException e){
            Log.e(TAG, "getIncrementalUpdate: Error parsing data " + e.toString());
        }

        return jdata;
    }

    private void refreshDevice(JSONObject jdata, String devId_setget, String devInstanceId_setget, String devCommandClass_setget, String devId_notified, String devInstanceId_notified, String devCommandClass_notified, String imgViewId, String txtViewId, String icontype_base, String name) {

        // Build the string to look for in the JSON data
        String key_main = String.format("devices.%s.instances.%s.commandClasses.%s.data.level", devId_setget, devInstanceId_setget, devCommandClass_setget);
        String key_alt = String.format("devices.%s.instances.%s.commandClasses.%s.data.level", devId_notified, devInstanceId_notified, devCommandClass_notified);

        String level_string="";
        long updateTime;
        long invalidateTime;
        int level=0;
        JSONObject j_main = null;
        JSONObject j_alt = null;

        // Parse the JSON data and check if this particular device changed state
        // Some device state changes may be notified by two different ways, depending on how their change was triggered
        // so look for both the main key string and the alternate key string in the JSON data
        try {
            j_main = jdata.getJSONObject(key_main);
        } catch(JSONException e){
        }

        try {
            j_alt = jdata.getJSONObject(key_alt);
        } catch(JSONException e){
        }

        JSONObject j = (j_main !=null) ? j_main: (j_alt != null) ? j_alt: null;
        if (j != null) {
            try {
                level_string = j.getString("value");
                updateTime = Long.valueOf(j.getString("updateTime"));
                invalidateTime = Long.valueOf(j.getString("invalidateTime"));

                // This checks is important, to filter out stale data notification
                // i.e. notification of a device state gathered *just* before it was manually changed from here
                // If not filtered, the next refresh will have the good data, but the UI will flash to the wrong state, not nice.
                if (updateTime > invalidateTime) {

                    if ("true".equals(level_string))
                        level = 1;
                    else if ("false".equals(level_string))
                        level = 0;
                    else
                        level = Integer.valueOf(j.getString("value"));

                    Log.i(TAG, "found change for " + key_main + " or "+ key_alt +" , level=" + Integer.toString(level));

                    // Refresh icon
                    updateImageView(imgViewId, icontype_base, level);
                }
                else {
                    Log.i(TAG, "STALE update for " + key_main + ", discarding ");
                }
            } catch(JSONException e){
                Log.e(TAG, "refreshDevice: Error parsing data " + e.toString());
            }
        } else {
            //Log.i(TAG, "No change detected for " + key_main + "or "+ key_alt);
        }
    }

    private void toggleDevice(String devId, String devInstanceId, String devCommandClass, String imgViewId, String txtViewId, String icontype_base, String name) {

        // Get current state of this device
        String query_Data = String.format(this.getResources().getString(R.string.zwaveserver_base)+"/ZWaveAPI/Run/devices[%s].instances[%s].commandClasses[%s].data.level", devId, devInstanceId, devCommandClass);
        String result = httpRequest(query_Data);

        String level_string="";
        int level=0;

        // Parse the received JSON data
        if (!"".equals(result)) {
            try {
                JSONObject jdata = new JSONObject(result);
                level_string = jdata.getString("value");

                if ("true".equals(level_string))
                    level = 1;
                else if ("false".equals(level_string))
                    level = 0;
                else
                    level = Integer.valueOf(jdata.getString("value"));

                Log.i(TAG, "updateSwitch (" + name + "): level=" + Integer.toString(level));

            } catch (JSONException e) {
                Log.e(TAG, "toggleDevice: Error parsing data " + e.toString());
            }

            // toggle current state
            if (level == 0)
                level = 255; // 255 works even if max value is actually 1 or 99 or whatever.
            else
                level = 0;

            // Refresh icon
            updateImageView(imgViewId, icontype_base, level);

            // Set new level on device
            String query_SetLevel = String.format(this.getResources().getString(R.string.zwaveserver_base) + "/ZWaveAPI/Run/devices[%s].instances[%s].commandClasses[%s].Set(%d)", devId, devInstanceId, devCommandClass, level);
            httpRequest(query_SetLevel);
        }
    }

    private void forceRefreshDevice(String devId, String devInstanceId, String devCommandClass, String imgViewId, String txtViewId, String icontype_base, String name) {

        // Perform an explicit GET command on the device, to be sure that device status is fresh
        String query_Get = String.format(this.getResources().getString(R.string.zwaveserver_base)+"/ZWaveAPI/Run/devices[%s].instances[%s].commandClasses[%s].Get()", devId, devInstanceId, devCommandClass);
        httpRequest(query_Get);

        // Then get the actual data
        String query_Data = String.format(this.getResources().getString(R.string.zwaveserver_base) + "/ZWaveAPI/Run/devices[%s].instances[%s].commandClasses[%s].data.level", devId, devInstanceId, devCommandClass);
        String result = httpRequest(query_Data);

        String level_string="";
        int level=0;

        // Parse the received JSON data
        try {
            JSONObject jdata = new JSONObject(result);
            level_string = jdata.getString("value");

            if ("true".equals(level_string))
                level = 1;
            else if ("false".equals(level_string))
                level = 0;
            else
                level = Integer.valueOf(jdata.getString("value"));

            Log.i(TAG, "updateSwitch ("+name+"): level=" + Integer.toString(level));

        } catch(JSONException e){
            Log.e(TAG, "forceRefreshDevice: Error parsing data "+e.toString());
        }

        updateImageView(imgViewId, icontype_base, level);

        int textViewId = this.getResources().getIdentifier(txtViewId, "id", this.getPackageName());
        //rv.setTextViewText(textViewId, name);
        updateTextView(textViewId, name);
    }

    private void updateTextView(int txtViewId, String text) {

        // Notify main class of view to refresh
        Intent updateTextViewIntent = new Intent();
        updateTextViewIntent.setAction(ZWaveWidgetMain.UPDATE_TEXTVIEW_ACTION);
        updateTextViewIntent.addCategory(Intent.CATEGORY_DEFAULT);
        updateTextViewIntent.putExtra(ZWaveWidgetMain.UPDATE_TEXTVIEW_EXTRA_ID, txtViewId);
        updateTextViewIntent.putExtra(ZWaveWidgetMain.UPDATE_TEXTVIEW_EXTRA_TEXT, text);
        sendBroadcast(updateTextViewIntent);
    }

    private void updateImageView(String imgViewId, String icontype_base, int level) {
        int imageViewId = this.getResources().getIdentifier(imgViewId, "id", this.getPackageName());

        String iconOnIdString = String.format("%s_on", icontype_base);
        String iconOffIdString = String.format("%s_off", icontype_base);
        int iconOnId = this.getResources().getIdentifier(iconOnIdString, "drawable", this.getPackageName());
        int iconOffId = this.getResources().getIdentifier(iconOffIdString, "drawable", this.getPackageName());

        int imgId = level == 0 ? iconOffId : iconOnId;

        // Notify main class of view to refresh
        Intent updateImageViewIntent = new Intent();
        updateImageViewIntent.setAction(ZWaveWidgetMain.UPDATE_IMAGEVIEW_ACTION);
        updateImageViewIntent.addCategory(Intent.CATEGORY_DEFAULT);
        updateImageViewIntent.putExtra(ZWaveWidgetMain.UPDATE_IMAGEVIEW_EXTRA_VIEWID, imageViewId);
        updateImageViewIntent.putExtra(ZWaveWidgetMain.UPDATE_IMAGEVIEW_EXTRA_IMGID, imgId);
        sendBroadcast(updateImageViewIntent);
    }

    private void getServerTime() {

        // Horrible hack until I find a proper way to get the current timestamp at server side : retrieve data from one arbitrary device and read its update timestamp
        String[] deviceList = getResources().getStringArray(R.array.ZWaveDeviceList);
        int tempArrayId = this.getResources().getIdentifier(deviceList[0], "array", this.getPackageName());
        String[] val = getResources().getStringArray(tempArrayId);
        String devId = val[0];
        String devInstanceId = val[1];
        String devCommandClass = val[2];

        String query_Get = String.format(this.getResources().getString(R.string.zwaveserver_base) + "/ZWaveAPI/Run/devices[%s].instances[%s].commandClasses[%s].Get()", devId, devInstanceId, devCommandClass);
        String query_Data = String.format(this.getResources().getString(R.string.zwaveserver_base)+"/ZWaveAPI/Run/devices[%s].instances[%s].commandClasses[%s].data.level", devId, devInstanceId, devCommandClass);

        httpRequest(query_Get);
        String result = httpRequest(query_Data);

        String updateTime ="0";

        // Parse the received JSON data
        try {
            JSONObject jdata = new JSONObject(result);
            if (jdata != null) {
                updateTime = jdata.getString("updateTime");
            }

            // Notify provider of this timestamp
            final Intent storeTimeIntent = new Intent(this, ZWaveWidgetMain.class);
            storeTimeIntent.setAction(ZWaveWidgetMain.STORE_REFRESH_TIME_ACTION);
            storeTimeIntent.putExtra(ZWaveWidgetMain.STORE_REFRESH_TIME_EXTRA, Long.valueOf(updateTime));
            final PendingIntent donePendingIntent = PendingIntent.getBroadcast(this, 0, storeTimeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            try {
                donePendingIntent.send();
            }
            catch (PendingIntent.CanceledException ce) {
                Log.i(TAG, "getServerTime: Exception: " + ce.toString());
            }

        } catch(JSONException e){
            Log.e(TAG, "getServerTime: Error parsing data "+e.toString());
        }
    }

    private String httpRequest(String url) {
        String result = "";

      Log.i(TAG, "Performing HTTP request " + url);

        try {

            URL targetUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) targetUrl.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = readStream(in);
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "httpRequest: Error in http connection " + e.toString());
        }

        String data;
        if (result.length() <= 2048)
            data = result;
        else
            data = "[long data....]";

        //Log.i(TAG, "httpRequest completed, received " + result.length() + " bytes: " + result.replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", ""));

        return result;
    }

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

}