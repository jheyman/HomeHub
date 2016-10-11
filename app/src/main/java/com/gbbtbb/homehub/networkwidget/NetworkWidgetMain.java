package com.gbbtbb.homehub.networkwidget;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gbbtbb.homehub.R;

public class NetworkWidgetMain extends Fragment {

    public static final String REFRESH_ACTION ="com.gbbtbb.networkwidget.REFRESH_ACTION";

    static long latestRefreshUnixTime = 0;

    public Handler handler = new Handler();
    private Context ctx;
    private static int REFRESH_DELAY = 2000;

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
        getActivity().unregisterReceiver(NetworkViewBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        //filter.addAction(xx);
        //filter.addAction(xx);
        //filter.addAction(xx);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(NetworkViewBroadcastReceiver, filter);
        ctx = getActivity();

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

            //Log.i("NetworkWidgetMain", "onReceive " + action);
/*
            if (xxx.equals(action)) {

            }
 */
        }
    };

}
