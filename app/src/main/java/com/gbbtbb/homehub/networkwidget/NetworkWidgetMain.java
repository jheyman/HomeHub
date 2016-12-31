package com.gbbtbb.homehub.networkwidget;

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

public class NetworkWidgetMain extends Fragment {

    public static final String TAG = "NetworkWidgetMain";
    public static final String REFRESH_ACTION ="com.gbbtbb.networkwidget.REFRESH_ACTION";

    public static final String UPDATE_IMAGEVIEW_ACTION ="com.gbbtbb.networkwidget.UPDATE_IMAGEVIEW_ACTION";
    public static final String UPDATE_IMAGEVIEW_EXTRA_VIEWID ="com.gbbtbb.networkwidget.UPDATE_IMAGEVIEW_EXTRA_VIEWID";
    public static final String UPDATE_IMAGEVIEW_EXTRA_IMGID ="com.gbbtbb.networkwidget.UPDATE_IMAGEVIEW_EXTRA_LEVEL";

    public Handler handler = new Handler();
    private Context ctx;
    private static int REFRESH_DELAY = 10000;

    Runnable refreshView = new Runnable()
    {
        @Override
        public void run() {

            Intent intent = new Intent(ctx.getApplicationContext(), NetworkWidgetService.class);
            intent.setAction(REFRESH_ACTION);
            ctx.startService(intent);

            handler.postDelayed(this, REFRESH_DELAY);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.networkwidget_layout, container, false);
    }

    @Override
    public void onDestroyView()
    {
        Log.i(TAG, "onDestroyView" );
        handler.removeCallbacksAndMessages(null);
        getActivity().unregisterReceiver(NetworkViewBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_IMAGEVIEW_ACTION);

        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(NetworkViewBroadcastReceiver, filter);
        ctx = getActivity();

        // Parse the list of all declared devices, retrieve the associated imageView for each device, and register a click event on it
        String[] deviceList = ctx.getResources().getStringArray(R.array.NetworkDeviceList);
        for (String d : deviceList) {

            int arrayId = ctx.getResources().getIdentifier(d, "array", ctx.getPackageName());

            String[] temp = ctx.getResources().getStringArray(arrayId);

            int txtViewId = getResources().getIdentifier(temp[1], "id", ctx.getPackageName());
            TextView tv = (TextView)getActivity().findViewById(txtViewId);
            tv.setText(temp[2]);

            // Get resource identifier of ImageView for this device
            int imageViewId = getResources().getIdentifier(temp[0], "id", ctx.getPackageName());
            ImageView iv = (ImageView)getView().findViewById(imageViewId);
        }

        // Initial call to the service to get the first batch of data to refresh the UI
        Intent intent = new Intent(ctx.getApplicationContext(), NetworkWidgetService.class);
        intent.setAction(REFRESH_ACTION);
        ctx.startService(intent);

        // Start background handler that will call refresh regularly
        handler.postDelayed(refreshView, REFRESH_DELAY);
    }

    private final BroadcastReceiver NetworkViewBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            //Log.i(TAG, "onReceive " + action);
             if (UPDATE_IMAGEVIEW_ACTION.equals(action)) {
                int imageViewId = intent.getIntExtra(UPDATE_IMAGEVIEW_EXTRA_VIEWID, 0);
                int imgId = intent.getIntExtra(UPDATE_IMAGEVIEW_EXTRA_IMGID, 0);
                ImageView iv = (ImageView)getActivity().findViewById(imageViewId);
                iv.setImageResource(imgId);
             }
        }
    };

}
