package com.gbbtbb.homehub.agendaviewer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
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

public class AgendaWidgetService extends IntentService {

    public static final String TAG = "AgendaViewerWidgetSvc";

    private Context mContext;
    private Cursor mDataCursor;

    private String mDataRefreshDateTime;

    public static final Uri CONTENT_URI_DATAPOINTS = Uri.parse("content://com.gbbtbb.agendaviewerwidget.provider.datapoints");

    public static final String DATAREFRESH_DATETIME = "current_datetime";
    public static final String DATAITEMS = "items";


    
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

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);

            Utilities.fillCanvas(canvas, mContext.getResources().getColor(R.color.graphviewer_background_color));

            drawTimestampMarkers(canvas, width, height);

            int graphHeight = (int) (0.4f * height);
            int graphOffset = graphHeight;

            drawGraph(canvas, "waterMeter", width, graphHeight, graphOffset);

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

    public void parseCursor() {
        Log.i(TAG, "---------------PARSING DATA ---------------");


        Log.i(TAG, "---------------DONE PARSING DATA---------------");
    }


    private void drawGraph(Canvas canvas, String dataId, int width, int height, int offset_y) {


    }

    public void getFreshData() {

        Log.i(TAG, "getFreshData called");

        //mDataCursor = query(CONTENT_URI_DATAPOINTS, null, null,args, null);
        parseCursor();
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