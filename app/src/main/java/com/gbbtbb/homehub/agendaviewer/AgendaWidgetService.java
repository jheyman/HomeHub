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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Format;
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

    public void cleanUp() {

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onHandleIntent " + action);

        if (AgendaWidgetMain.REFRESH_ACTION.equals(action)) {
            init();

            getFreshData();

            int width = AgendaWidgetMain.mAgendaWidth;
            int height = AgendaWidgetMain.mAgendaHeight;

            Log.i(TAG, "onHandleIntent: width=" + Integer.toString(width) + ", height=" + Integer.toString(height));

            /////////////////////////
            // Render agenda timeline
            /////////////////////////

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);

            Utilities.fillCanvas(canvas, mContext.getResources().getColor(R.color.graphviewer_background_color));

            drawTimestampMarkers(canvas, width, height);

            drawTimeline(canvas, width, height);

            Globals.agendaBitmap = bmp;
            //rv.setImageViewBitmap(R.id.GraphBody, bmp);

            // graph has been redrawn: hide progress bar now.
            //rv.setViewVisibility(R.id.loadingProgress, View.GONE);
            //rv.setViewVisibility(R.id.reloadList, View.VISIBLE);

            cleanUp();

            // Notify widget that graph has been refreshed
            Intent doneIntent = new Intent();
            doneIntent.setAction(AgendaWidgetMain.AGENDAREFRESHEDDONE_ACTION);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
    }

    private void drawTimestampMarkers(Canvas canvas, int width, int height) {

        Path path = new Path();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setARGB(127, 150, 150, 150);
        paint.setStrokeWidth(1.0f);
        paint.setPathEffect(new DashPathEffect(new float[]{2, 2}, 0));
        paint.setStyle(Paint.Style.STROKE);

        paint.setARGB(255, 100, 100, 100);
        for (int i=0; i< AgendaWidgetMain.NB_VERTICAL_MARKERS; i++) {
            float x = (1.0f+i)* width/ (AgendaWidgetMain.NB_VERTICAL_MARKERS+1);

            // Draw vertical marker at regular intervals
            path.moveTo(x, 0);
            path.lineTo(x, height);
            canvas.drawPath(path, paint);
        }
    }

    public void drawTimeline(Canvas canvas, int width, int height ) {
        Log.i(TAG, "---------------RENDERING DATA ---------------");

        if (mDataCursor != null) {

            while (mDataCursor.moveToNext()) {

                String title = null;
                Long start = 0L;

                title = mDataCursor.getString(0);
                start = mDataCursor.getLong(2);

                Format df = DateFormat.getDateFormat(this);
                Format tf = DateFormat.getTimeFormat(this);

                Log.i(TAG, "getFreshData:" + title + " on " + df.format(start) + " at " + tf.format(start));

                Log.i(TAG, "getFreshData: start time " + df.format(AgendaWidgetMain.timestamp_start) + " at " + tf.format(AgendaWidgetMain.timestamp_start));
                Log.i(TAG, "getFreshData: end time " + df.format(AgendaWidgetMain.timestamp_end) + " at " + tf.format(AgendaWidgetMain.timestamp_end));


                Log.i(TAG, "getFreshData: start time: " + Long.toString(AgendaWidgetMain.timestamp_start));
                Log.i(TAG, "getFreshData: end time: " + Long.toString(AgendaWidgetMain.timestamp_end));
                Log.i(TAG, "getFreshData: item  time: " + Long.toString(start));

                long timerange = AgendaWidgetMain.timestamp_end - AgendaWidgetMain.timestamp_start;
                float x = width * (start - AgendaWidgetMain.timestamp_start) / timerange;
                float y = height / 2;

                drawAgendaItem(canvas, title, width, height, x, y);
            }
        }

        Log.i(TAG, "---------------DONE RENDERING DATA---------------");
    }

    private void drawAgendaItem(Canvas canvas, String text, int width, int height, float offset_x, float offset_y) {

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTextSize(10);
            paint.setColor(mContext.getResources().getColor(R.color.agendaviewer_text_color));
            String msg= String.format(Locale.getDefault(),"%s", text);

            float textWidth = Utilities.getTextWidth(paint, msg);
            float textHeight = Utilities.getTextHeight(paint, "0");

            Paint boxpaint = new Paint();
            boxpaint.setAntiAlias(true);
            boxpaint.setColor(mContext.getResources().getColor(R.color.agendaviewer_text_background));
            boxpaint.setStyle(Paint.Style.STROKE);

            // Draw text brackground, and then text itself
            canvas.drawRect(offset_x, offset_y - (0.5f*textHeight) - 2, offset_x+textWidth + 10, offset_y + (0.5f* textHeight) + 2, boxpaint);
            canvas.drawText(msg, offset_x+5, offset_y + 0.5f*textHeight, paint);
    }

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

            ContentUris.appendId(eventsUriBuilder, AgendaWidgetMain.timestamp_start);
            ContentUris.appendId(eventsUriBuilder, AgendaWidgetMain.timestamp_end);

            Uri eventUri = eventsUriBuilder.build();

            //String selection = "((" + CalendarContract.Events.CALENDAR_ID + " = ?))";
            //String[] selectionArgs = new String[]{"1"};

            mDataCursor = resolver.query(eventUri, EVENT_PROJECTION, /*selection*/null, /*selectionArgs*/null, CalendarContract.Instances.BEGIN + " ASC");
        }
        catch (SecurityException e) {
            Log.i(TAG, "getFreshData SecurityException" + e.toString());
        }
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