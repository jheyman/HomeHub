package com.gbbtbb.homehub.graphviewer;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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

public class GraphViewerWidgetMain extends Fragment {

    public static final String TAG = "GraphViewerWidgetMain";

    public static final String REFRESH_ACTION ="com.gbbtbb.graphviewerwidget.REFRESH_ACTION";
    public static final String SETTINGSCHANGED_ACTION ="com.gbbtbb.graphviewerwidget.SETTINGSCHANGED_ACTION";
    public static final String GRAPHREFRESHEDDONE_ACTION ="com.gbbtbb.graphviewerwidget.GRAPHREFRESHEDDONE_ACTION";

    public static final int NB_VERTICAL_MARKERS = 15;
    public static int mHistoryLengthInHours;

    public static final String SETTING_BASE = "com.gbbtbb.graphviewerviewerwidget";

    public static int mGraphWidth;
    public static int mGraphHeight;
    public static int mHeaderHeight;
    public static int mHeaderWidth;
    public static int mFooterHeight;

    public static long timestamp_start;
    public static long timestamp_end;

    private String timeLastUpdated;

    private Settings mSettings;

    private Context ctx;
    public Handler handler = new Handler();
    private static int REFRESH_DELAY = 60000;

    Runnable refreshView = new Runnable()
    {
        @Override
        public void run() {

            Log.i("GraphViewerWidgetMain", "refreshView CALLED, ctx=" + ctx.toString());
            refresh();

            handler.postDelayed(this, REFRESH_DELAY);
        }
    };

    private void refresh(){

        getPrefs(ctx);
        mSettings = Settings.get(ctx);
        Settings.GraphSettings gs = mSettings.getGraphSettings();
        mHistoryLengthInHours = gs.getHistoryLength();

        timeLastUpdated = Utilities.getCurrentTime();
        timestamp_end = Utilities.getCurrentTimeStamp();
        timestamp_start = timestamp_end - mHistoryLengthInHours*60*60*1000;

        final ImageView title = (ImageView)getView().findViewById(R.id.graphviewer_textGraphTitle);
        final ImageView footer = (ImageView)getView().findViewById(R.id.graphviewer_footer);

        setLoadingInProgress(true);

        title.setImageBitmap(drawCommonHeader(ctx, mHeaderWidth, mHeaderHeight));
        footer.setImageBitmap(drawCommonFooter(ctx, mGraphWidth, mFooterHeight));

        Intent intent = new Intent(ctx.getApplicationContext(), GraphViewerWidgetService.class);
        intent.setAction(REFRESH_ACTION);
        ctx.startService(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.graphviewer_layout, container, false);
    }

    @Override
    public void onDestroyView()
    {
        Log.i(GraphViewerWidgetMain.TAG, "onDestroyView" + this.toString());
        handler.removeCallbacksAndMessages(null);
        getActivity().unregisterReceiver(GraphViewBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(GraphViewerWidgetMain.TAG, "onPause" + this.toString());
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(GraphViewerWidgetMain.TAG, "onStop" + this.toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i(GraphViewerWidgetMain.TAG, "onActivityCreated" + this.toString());

        IntentFilter filter = new IntentFilter();
        filter.addAction(GRAPHREFRESHEDDONE_ACTION);
        filter.addAction(SETTINGSCHANGED_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(GraphViewBroadcastReceiver, filter);
        ctx = getActivity();

        final ImageView title = (ImageView)getView().findViewById(R.id.graphviewer_textGraphTitle);
        final ImageView footer = (ImageView)getView().findViewById(R.id.graphviewer_footer);
        final ImageView graph = (ImageView)getView().findViewById(R.id.graphviewer_GraphBody);

        // Register a callback for when the fragment and its views have been layed out on the screen, and it is now safe to get their dimensions.
        ViewGroup parentLayout = ((ViewGroup) getView().getParent());
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                mGraphWidth = graph.getWidth();
                mGraphHeight = graph.getHeight();
                mHeaderHeight = title.getHeight();
                mHeaderWidth = title.getWidth();
                mFooterHeight = footer.getHeight();

                //Log.i(GraphViewerWidgetMain.TAG, "onGlobalLayout TITLE: " + Integer.toString(mHeaderWidth) + ", " + Integer.toString(mHeaderHeight));
                //Log.i(GraphViewerWidgetMain.TAG, "onGlobalLayout GRAPH: " + Integer.toString(mGraphWidth) + ", " + Integer.toString(mGraphHeight));
                //Log.i(GraphViewerWidgetMain.TAG, "onGlobalLayout FOOTER: " + Integer.toString(mGraphWidth) + ", " + Integer.toString(mFooterHeight));

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    title.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    title.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                // It is now also safe to refresh the graph itself, with correct dimensions
                Log.i(GraphViewerWidgetMain.TAG, "initial REFRESH triggered");
                handler.post(refreshView);
                //refresh();
            }
        });

        ImageView reloadIcon = (ImageView)getView().findViewById(R.id.graphviewer_reloadList);

        //  register a click event on the reload/refresh button
        reloadIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setLoadingInProgress(true);
                Intent reloadIntent = new Intent(ctx.getApplicationContext(), GraphViewerWidgetService.class);
                reloadIntent.setAction(REFRESH_ACTION);
                ctx.startService(reloadIntent);
            }
        });
        
        ImageView settingsIcon = (ImageView)getView().findViewById(R.id.graphviewer_settings);

        //  register a click event on the settings button
        settingsIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ctx.getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Start background handler that will call refresh regularly
        //handler.postDelayed(refreshView, REFRESH_DELAY);
    }

    private void setLoadingInProgress(boolean state) {

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.graphviewer_loadingProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);

        ImageView iv = (ImageView)getView().findViewById(R.id.graphviewer_reloadList);
        iv.setVisibility(state ? View.GONE: View.VISIBLE);
    }

    private void getPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SETTING_BASE, Context.MODE_PRIVATE);

    }

    private final BroadcastReceiver GraphViewBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            Log.i("GraphViewerWidgetMain", "onReceive " + action);

            if (GRAPHREFRESHEDDONE_ACTION.equals(action)) {

                setLoadingInProgress(false);

                ImageView iv = (ImageView)getView().findViewById(R.id.graphviewer_GraphBody);
                iv.setImageBitmap(Globals.graphBitmap);
            }
            else if (SETTINGSCHANGED_ACTION.equals(action)) {
                refresh();
            }
        }
    };

    private Bitmap drawCommonHeader(Context ctx, int width, int height) {
        Log.i(GraphViewerWidgetMain.TAG, "drawCommonHeader");

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Typeface myfont = Typeface.createFromAsset(ctx.getAssets(), "fonts/passing_notes.ttf");

        TextPaint textPaint = new TextPaint();
        textPaint.setTypeface(myfont);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(ctx.getResources().getDimension(R.dimen.graphviewer_header_text_size));
        textPaint.setColor(ctx.getResources().getColor(R.color.graphviewer_text_color));

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setAntiAlias(true);
        textPaint.setSubpixelText(true);

        float textHeight = Utilities.getTextHeight(textPaint, "r");
        canvas.drawText(ctx.getResources().getString(R.string.graphviewer_header_text) + ": " + mHistoryLengthInHours + " heures", 10.0f, 0.5f * (height + textHeight), textPaint);

        TextPaint textPaintComments = new TextPaint();
        textPaintComments.setStyle(Paint.Style.FILL);
        textPaintComments.setTextSize(ctx.getResources().getDimension(R.dimen.graphviewer_header_comments_size));
        textPaintComments.setColor(ctx.getResources().getColor(R.color.graphviewer_text_color));

        String commentText = "(Dernière MAJ: " + timeLastUpdated + ")";
        canvas.drawText(commentText , 0.5f*width, 0.5f * (height + textHeight), textPaintComments);

        return bmp;
    }

    private Bitmap drawCommonFooter(Context ctx, int width, int height) {

        Log.i(GraphViewerWidgetMain.TAG, "drawCommonFooter");

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Utilities.fillCanvas(canvas, ctx.getResources().getColor(R.color.graphviewer_background_color));

        TextPaint textPaint = new TextPaint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(12);
        textPaint.setColor(ctx.getResources().getColor(R.color.graphviewer_text_color));

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
