package com.gbbtbb.homehub.zwavewidget;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbbtbb.homehub.R;

public class ZWaveWidgetMain extends Fragment {

    public static final String TAG = "ZWaveWidgetMain";

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

    public Handler handler = new Handler();
    private Context ctx;
    private static int REFRESH_DELAY = 5000;

    Runnable refreshView = new Runnable()
    {
        @Override
        public void run() {
            Intent intent = new Intent(ctx.getApplicationContext(), ZWaveWidgetService.class);
            intent.putExtra(LATEST_REFRESH_EXTRA, latestRefreshUnixTime);
            intent.setAction(REFRESH_ACTION);
            ctx.startService(intent);

            handler.postDelayed(this, REFRESH_DELAY);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.zwavewidget_layout, container, false);
    }

    @Override
    public void onDestroyView()
    {
        Log.i(TAG, "onDestroyView" );
        handler.removeCallbacksAndMessages(null);
        getActivity().unregisterReceiver(zWaveViewBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_IMAGEVIEW_ACTION);
        filter.addAction(UPDATE_TEXTVIEW_ACTION);
        filter.addAction(STORE_REFRESH_TIME_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(zWaveViewBroadcastReceiver, filter);
        ctx = getActivity();
        Log.i(TAG, "onActivityCreated" );

        // Parse the list of all declared devices, retrieve the associated imageView for each device, and register a click event on it
        String[] deviceList = ctx.getResources().getStringArray(R.array.ZWaveDeviceList);
        for (String d : deviceList) {
            int arrayId = ctx.getResources().getIdentifier(d, "array", ctx.getPackageName());
            String[] temp = ctx.getResources().getStringArray(arrayId);

            // Get resource identifier of ImageView for this device
            int imageViewId = getResources().getIdentifier(temp[6], "id", ctx.getPackageName());
            ImageView iv = (ImageView)getView().findViewById(imageViewId);

            // Store deviceName in private tags of associated ImageView, for later use in click callback
            iv.setTag(d);

            // Register a click callback
            iv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent(ctx.getApplicationContext(), ZWaveWidgetService.class);
                    i.putExtra(DEVICE_NAME_EXTRA, (String)v.getTag());
                    i.setAction(TOGGLE_ACTION);
                    ctx.startService(i);
                }
            });
        }

        // Initial call to the service to get the first batch of data to refresh the UI
        Intent intent = new Intent(ctx.getApplicationContext(), ZWaveWidgetService.class);
        intent.putExtra(LATEST_REFRESH_EXTRA, latestRefreshUnixTime);
        intent.setAction(INITIALIZE_ACTION);
        ctx.startService(intent);

        // Start background handler that will call refresh regularly
        handler.postDelayed(refreshView, REFRESH_DELAY);
    }

    private final BroadcastReceiver zWaveViewBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            //Log.i(TAG, "onReceive " + action);

            if (STORE_REFRESH_TIME_ACTION.equals(action)) {

                latestRefreshUnixTime = intent.getLongExtra(STORE_REFRESH_TIME_EXTRA, 0);
                //Log.i(TAG, "Updating latestRefreshUnixTime to " + Long.toString(latestRefreshUnixTime));
            }
            else if (UPDATE_IMAGEVIEW_ACTION.equals(action)) {
                int imageViewId = intent.getIntExtra(UPDATE_IMAGEVIEW_EXTRA_VIEWID, 0);
                int imgId = intent.getIntExtra(UPDATE_IMAGEVIEW_EXTRA_IMGID, 0);
                ImageView iv = (ImageView)getActivity().findViewById(imageViewId);
                iv.setImageResource(imgId);
            }
            else if (UPDATE_TEXTVIEW_ACTION.equals(action)) {
                int txtViewId = intent.getIntExtra(UPDATE_TEXTVIEW_EXTRA_ID, 0);
                String txt = intent.getStringExtra(UPDATE_TEXTVIEW_EXTRA_TEXT);
                TextView tv = (TextView)getActivity().findViewById(txtViewId);
                tv.setText(txt);
            }
        }
    };

}
