package com.gbbtbb.homehub.agendaviewer;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.util.Log;

import com.gbbtbb.homehub.Globals;
import com.gbbtbb.homehub.R;
import com.gbbtbb.homehub.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Format;
import java.util.ArrayList;
import java.util.Locale;

public class AgendaWidgetService extends IntentService {

    public static final String TAG = "AgendaViewerWidgetSvc";

    private Context mContext;
    private Cursor mDataCursor;

    public AgendaWidgetService() {
        super(com.gbbtbb.homehub.agendaviewer.AgendaWidgetService.class.getName());
    }

    public void init() {
        mContext = this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onHandleIntent " + action);

        if (AgendaWidgetMain.REFRESH_ACTION.equals(action)) {
            init();

            getFreshData();

            // Notify widget that data is available
            Intent doneIntent = new Intent();
            doneIntent.setAction(AgendaWidgetMain.AGENDAREFRESHEDDONE_ACTION);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
    }


    private static final String OPEN_WEATHER_MAP_API =
            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";


    public void getFreshData() {

        Log.i(TAG, "getFreshData called");


        try {

            String[] EVENT_PROJECTION = new String[]{
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.EVENT_LOCATION,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Events.ALL_DAY};

            ContentResolver resolver = getContentResolver();
            Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();

            // get the next 5 days worth of events
            long timestamp_start = Utilities.getCurrentTimeStamp();
            long timestamp_end = timestamp_start + 5*24*60*60*1000;

            ContentUris.appendId(eventsUriBuilder, timestamp_start);
            ContentUris.appendId(eventsUriBuilder, timestamp_end);

            Uri eventUri = eventsUriBuilder.build();

            //String selection = "((" + CalendarContract.Events.CALENDAR_ID + " = ?))";
            //String[] selectionArgs = new String[]{"1"};

            mDataCursor = resolver.query(eventUri, EVENT_PROJECTION, /*selection*/null, /*selectionArgs*/null, CalendarContract.Instances.BEGIN + " ASC");
        }
        catch (SecurityException e) {
            Log.i(TAG, "getFreshData SecurityException" + e.toString());
        }







        ArrayList<AgendaItem> list = new ArrayList<AgendaItem>();

        String charset = "UTF-8";
        String query = "";

        String appId = "2dee82fd5d7326c51ceb0d4b8a42e2b5";

        try {
            query = String.format("http://api.openweathermap.org/data/2.5/forecast?units=metric&q=Paris,FR&APPID=%s",
                    URLEncoder.encode(appId, charset));
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error encoding URL params: " + e.toString());
        }

        String result = httpRequest(query);

        // Parse the received JSON data
        if (!result.equals("")) {
            Log.i(TAG, "Parsing received JSON data");
            try {
                JSONObject jdata = new JSONObject(result);
                JSONArray series_values = (JSONArray) jdata.getJSONArray("list");

                for (int i = 0; i < series_values.length(); i++) {

                    JSONObject tmp = (JSONObject) series_values.get(i);
                    Long timestamp = tmp.getLong("dt");
                    timestamp *=1000;

                    JSONObject mainObj = tmp.getJSONObject("main");
                    int humidity = mainObj.getInt("humidity");
                    double temperature = mainObj.getDouble("temp");

                    JSONObject weatherObj = tmp.getJSONArray("weather").getJSONObject(0);;
                    int id = weatherObj.getInt("id");

                    list.add(new AgendaItem(timestamp, id, humidity, temperature));
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing data: " + e.toString());
            }
            Log.i(TAG, "JSON data parsing completed");
        }



        Globals.agendaItems = list;

   }

    private static String httpRequest(String url/*, ArrayList<NameValuePair> nameValuePairs*/) {
        String result = "";

        Log.i(TAG, "Performing HTTP request " + url);

        try {

            URL targetUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) targetUrl.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = readStream(in);
            }
            finally {
                urlConnection.disconnect();
            }
        } catch(Exception e) {
            Log.e(TAG, "httpRequest: Error in http connection "+e.toString());
        }

        String data ;
        if (result.length() <= 32)
            data = result;
        else
            data = "[long data....]";

        Log.i(TAG, "httpRequest completed, received "+ result.length() + " bytes: " + data);

        return result;
    }

    private static String readStream(InputStream is) {
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