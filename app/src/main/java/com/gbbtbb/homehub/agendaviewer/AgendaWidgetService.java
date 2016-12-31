package com.gbbtbb.homehub.agendaviewer;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.util.Log;

import com.gbbtbb.homehub.Globals;
import com.gbbtbb.homehub.Utilities;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Format;
import java.util.ArrayList;

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

        /////////////////////////////////////
        // Get Calendar Events
        /////////////////////////////////////
        ArrayList<AgendaItem> agenda_list = new ArrayList<AgendaItem>();
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

            Log.i(TAG, "getFreshData querying agenda items...");
            mDataCursor = resolver.query(eventUri, EVENT_PROJECTION, /*selection*/null, /*selectionArgs*/null, CalendarContract.Instances.BEGIN + " ASC");

            if (mDataCursor != null) {

                if (mDataCursor.getCount() == 0) {
                    Log.i(TAG, "No agenda items");
                }
                else {
                    while (mDataCursor.moveToNext()) {

                        String title = null;
                        Long start = 0L;

                        title = mDataCursor.getString(0);
                        start = mDataCursor.getLong(2);

                        Format df = DateFormat.getDateFormat(this);
                        Format tf = DateFormat.getTimeFormat(this);

                        Log.i(TAG, "getFreshData:" + title + " on " + df.format(start) + " at " + tf.format(start));

                        agenda_list.add(new AgendaItem(start, title));
                    }
                }
                mDataCursor.close();
            }
        }
        catch (SecurityException e) {
            Log.i(TAG, "getFreshData SecurityException" + e.toString());
        }

        Globals.agendaItems = agenda_list;


        /////////////////////////////////////
        // Get Weather forecast
        /////////////////////////////////////
        ArrayList<WeatherItem> list = new ArrayList<WeatherItem>();

        String charset = "UTF-8";
        String query = "";

        String appId = "2dee82fd5d7326c51ceb0d4b8a42e2b5";
        String result = "";

        Long timestamp_now = 0L;
        Long timestamp_sunrise = 0L;
        Long timestamp_sunset = 0L;

        // First call is only to retrieve the sunrise/sunset times for today (that will be used as an approximation for the 5 day forecast
        // This is silly, but OpenWeatherMap API does not include sunrise/sunset times in the 5-day forecast returned data....
        try {
            query = String.format("http://api.openweathermap.org/data/2.5/weather?q=Paris,FR&APPID=%s",
                    URLEncoder.encode(appId, charset));
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error encoding URL params: " + e.toString());
        }

        result = httpRequest(query);

        // Parse the received JSON data
        if (!result.equals("")) {
            Log.i(TAG, "Parsing received JSON data (1)");
            try {
                    JSONObject jdata = new JSONObject(result);

                    timestamp_now = jdata.getLong("dt");
                    timestamp_now *=1000;

                    JSONObject sys = (JSONObject)jdata.getJSONObject("sys");
                    timestamp_sunrise = sys.getLong("sunrise");
                    timestamp_sunrise *=1000;

                    timestamp_sunset = sys.getLong("sunset");
                    timestamp_sunset *=1000;

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing data: " + e.toString());
            }
            Log.i(TAG, "JSON data parsing completed");
        }

        // Now actually proceed to get the 5-day forecast from OpenWeatherMap
        try {
            query = String.format("http://api.openweathermap.org/data/2.5/forecast?units=metric&q=Paris,FR&APPID=%s",
                    URLEncoder.encode(appId, charset));
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error encoding URL params: " + e.toString());
        }

        result = httpRequest(query);

        // Parse the received JSON data
        if (!result.equals("")) {
            Log.i(TAG, "Parsing received JSON data (2)");
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

                    // recompute an approximation of sunrise/sunset for this particular day, by just using the sunrise/sunset for
                    // today and adding the proper number of full days between now and then
                    DateTime dt_now = new DateTime(timestamp_now);
                    DateTime dt_then = new DateTime(timestamp);
                    int days_between = Days.daysBetween(dt_now.toLocalDate(), dt_then.toLocalDate()).getDays();

                    DateTime sunrise = new DateTime(timestamp_sunrise);
                    sunrise = sunrise.plusDays(days_between);

                    DateTime sunset = new DateTime(timestamp_sunset);
                    sunset = sunset.plusDays(days_between);

                    // add the item to the day's layout
                    list.add(new WeatherItem(timestamp, id, humidity, temperature, sunrise.getMillis(), sunset.getMillis()));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing data: " + e.toString());
            }
            Log.i(TAG, "JSON data parsing completed");
        }

        // Store it in globals for later use by the UI thread
        Globals.weatherItems = list;
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