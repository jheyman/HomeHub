package com.gbbtbb.homehub.agendaviewer;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.gbbtbb.homehub.Globals;
import com.gbbtbb.homehub.R;
import com.gbbtbb.homehub.Utilities;

public class AgendaWidgetMain extends Fragment {

    public static final String TAG = "AgendaWidgetMain";

    public static final String REFRESH_ACTION ="com.gbbtbb.agendaviewerwidget.REFRESH_ACTION";
    public static final String SETTINGSCHANGED_ACTION ="com.gbbtbb.agendaviewerwidget.SETTINGSCHANGED_ACTION";
    public static final String AGENDAREFRESHEDDONE_ACTION ="com.gbbtbb.agendaviewerwidget.agendaREFRESHEDDONE_ACTION";

    public static final int NB_VERTICAL_MARKERS = 15;
    public static int mHistoryLengthInHours;

    public static final String SETTING_BASE = "com.gbbtbb.agendaviewerwidget";

    public static int mAgendaWidth;
    public static int mAgendaHeight;
    public static int mHeaderHeight;
    public static int mHeaderWidth;
    public static int mFooterHeight;

    public static long timestamp_start;
    public static long timestamp_end;

    private String timeLastUpdated;

    private com.gbbtbb.homehub.agendaviewer.Settings mSettings;

    private Context ctx;
    public Handler handler = new Handler();
    private static int REFRESH_DELAY = 300000;

    Runnable refreshView = new Runnable()
    {
        @Override
        public void run() {

            refresh();

            handler.postDelayed(this, REFRESH_DELAY);
        }
    };

    private void refresh(){

        getPrefs(ctx);
        mSettings = com.gbbtbb.homehub.agendaviewer.Settings.get(ctx);
        Settings.AgendaSettings gs = mSettings.getAgendaSettings();
        mHistoryLengthInHours = gs.getHistoryLength();

        Log.i(AgendaWidgetMain.TAG, "mHistoryLengthInHours  " + Integer.toString(mHistoryLengthInHours) );

        timeLastUpdated = Utilities.getCurrentTime();
        timestamp_start = Utilities.getCurrentTimeStamp();
        timestamp_end = timestamp_start + (long)(mHistoryLengthInHours)*60*60*1000;

        final ImageView title = (ImageView)getView().findViewById(R.id.agendaviewer_textAgendaTitle);
        final ImageView footer = (ImageView)getView().findViewById(R.id.agendaviewer_footer);

        setLoadingInProgress(true);

        title.setImageBitmap(drawCommonHeader(ctx, mHeaderWidth, mHeaderHeight));
        footer.setImageBitmap(drawCommonFooter(ctx, mAgendaWidth, mFooterHeight));

        Intent intent = new Intent(ctx.getApplicationContext(), AgendaWidgetService.class);
        intent.setAction(REFRESH_ACTION);
        ctx.startService(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.agenda_layout, container, false);
    }

    @Override
    public void onDestroyView()
    {
        handler.removeCallbacksAndMessages(refreshView);
        getActivity().unregisterReceiver(agendaViewBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(AGENDAREFRESHEDDONE_ACTION);
        filter.addAction(SETTINGSCHANGED_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(agendaViewBroadcastReceiver, filter);
        ctx = getActivity();

        final ImageView title = (ImageView)getView().findViewById(R.id.agendaviewer_textAgendaTitle);
        final ImageView footer = (ImageView)getView().findViewById(R.id.agendaviewer_footer);
        final ImageView agenda = (ImageView)getView().findViewById(R.id.agendaviewer_AgendaBody);

        // Register a callback for when the fragment and its views have been layed out on the screen, and it is now safe to get their dimensions.
        ViewGroup parentLayout = ((ViewGroup) getView().getParent());
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                mAgendaWidth = agenda.getWidth();
                mAgendaHeight = agenda.getHeight();
                mHeaderHeight = title.getHeight();
                mHeaderWidth = title.getWidth();
                mFooterHeight = footer.getHeight();

                //Log.i(AgendaWidgetMain.TAG, "onGlobalLayout TITLE: " + Integer.toString(mHeaderWidth) + ", " + Integer.toString(mHeaderHeight));
                //Log.i(AgendaWidgetMain.TAG, "onGlobalLayout agenda: " + Integer.toString(mAgendaWidth) + ", " + Integer.toString(mAgendaHeight));
                //Log.i(AgendaWidgetMain.TAG, "onGlobalLayout FOOTER: " + Integer.toString(mAgendaWidth) + ", " + Integer.toString(mFooterHeight));

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    title.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    title.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                // It is now also safe to refresh the agenda itself, with correct dimensions
                refresh();
            }
        });

        ImageView reloadIcon = (ImageView)getView().findViewById(R.id.agendaviewer_reloadList);

        //  register a click event on the reload/refresh button
        reloadIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setLoadingInProgress(true);
                Intent reloadIntent = new Intent(ctx.getApplicationContext(), AgendaWidgetService.class);
                reloadIntent.setAction(REFRESH_ACTION);
                ctx.startService(reloadIntent);
            }
        });
        
        ImageView settingsIcon = (ImageView)getView().findViewById(R.id.agendaviewer_settings);

        //  register a click event on the settings button
        settingsIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ctx.getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });









        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CALENDAR)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_CALENDAR},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


        // Start background handler that will call refresh regularly
        handler.postDelayed(refreshView, REFRESH_DELAY);
    }

    private void setLoadingInProgress(boolean state) {

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.agendaviewer_loadingProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);

        ImageView iv = (ImageView)getView().findViewById(R.id.agendaviewer_reloadList);
        iv.setVisibility(state ? View.GONE: View.VISIBLE);
    }

    private void getPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SETTING_BASE, Context.MODE_PRIVATE);

    }

    private final BroadcastReceiver agendaViewBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            Log.i("AgendaWidgetMain", "onReceive " + action);

            if (AGENDAREFRESHEDDONE_ACTION.equals(action)) {

                setLoadingInProgress(false);

                ImageView iv = (ImageView)getView().findViewById(R.id.agendaviewer_AgendaBody);
                iv.setImageBitmap(Globals.agendaBitmap);
            }
            else if (SETTINGSCHANGED_ACTION.equals(action)) {
                refresh();
            }
        }
    };

    private Bitmap drawCommonHeader(Context ctx, int width, int height) {
        Log.i(com.gbbtbb.homehub.agendaviewer.AgendaWidgetMain.TAG, "drawCommonHeader");

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Typeface myfont = Typeface.createFromAsset(ctx.getAssets(), "fonts/passing_notes.ttf");

        TextPaint textPaint = new TextPaint();
        textPaint.setTypeface(myfont);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(ctx.getResources().getDimension(R.dimen.agendaviewer_header_text_size));
        textPaint.setColor(ctx.getResources().getColor(R.color.agendaviewer_text_color));

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setAntiAlias(true);
        textPaint.setSubpixelText(true);

        float textHeight = Utilities.getTextHeight(textPaint, "r");
        canvas.drawText(ctx.getResources().getString(R.string.agendaviewer_header_text) + ": " + mHistoryLengthInHours + " heures", 10.0f, 0.5f * (height + textHeight), textPaint);

        TextPaint textPaintComments = new TextPaint();
        textPaintComments.setStyle(Paint.Style.FILL);
        textPaintComments.setTextSize(ctx.getResources().getDimension(R.dimen.agendaviewer_header_comments_size));
        textPaintComments.setColor(ctx.getResources().getColor(R.color.agendaviewer_text_color));

        String commentText = "(Dernière MAJ: " + timeLastUpdated + ")";
        canvas.drawText(commentText , 0.5f*width, 0.5f * (height + textHeight), textPaintComments);

        return bmp;
    }

    private Bitmap drawCommonFooter(Context ctx, int width, int height) {

        Log.i(com.gbbtbb.homehub.agendaviewer.AgendaWidgetMain.TAG, "drawCommonFooter");

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Utilities.fillCanvas(canvas, ctx.getResources().getColor(R.color.agendaviewer_background_color));

        TextPaint textPaint = new TextPaint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(12);
        textPaint.setColor(ctx.getResources().getColor(R.color.agendaviewer_text_color));

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setAntiAlias(true);
        textPaint.setSubpixelText(true);

        String latestDatePrinted = "";

        for (int i=0; i<NB_VERTICAL_MARKERS; i++) {
            float x = (1.0f+i)*width/(NB_VERTICAL_MARKERS+1);

            // On bottom half of the footer, under each marker, print corresponding date and time
            //Rect bounds = new Rect();
            float textHeight = Utilities.getTextHeight(textPaint, "0");
            float textWidth;

            long timerange = timestamp_end - timestamp_start;

            // Compute and display time for this marker
            long timestamp = timestamp_start + (long)(x * timerange / width);
            String timetext = Utilities.getTimeFromTimeStamp(timestamp);

            textWidth = Utilities.getTextWidth(textPaint, timetext);
            canvas.drawText(timetext, x - 0.5f*textWidth, (0.25f)*height + 0.5f*textHeight, textPaint);

            // Compute and display date for this marker
            // But do not print the date on this marker if it is the same day as one of the previous markers
            String datetext = Utilities.getDateFromTimeStamp(timestamp);
            if (!datetext.equals(latestDatePrinted)) {
                textWidth = Utilities.getTextWidth(textPaint, datetext);
                canvas.drawText(datetext, x - 0.5f * textWidth, (0.75f) * height + 0.5f * textHeight, textPaint);
                latestDatePrinted = datetext;
            }
        }
        return bmp;
    }

}
