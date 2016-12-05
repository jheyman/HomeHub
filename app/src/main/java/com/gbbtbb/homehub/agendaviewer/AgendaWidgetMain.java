package com.gbbtbb.homehub.agendaviewer;

import android.Manifest;
import android.app.Activity;
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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gbbtbb.homehub.Globals;
import com.gbbtbb.homehub.R;
import com.gbbtbb.homehub.Utilities;
import com.gbbtbb.homehub.todolist.TodoListRowItem;
import com.gbbtbb.homehub.todolist.TodoListViewAdapter;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AgendaWidgetMain extends Fragment {

    public static final String TAG = "AgendaWidgetMain";

    public static final String REFRESH_ACTION ="com.gbbtbb.agendaviewerwidget.REFRESH_ACTION";
    public static final String AGENDAREFRESHEDDONE_ACTION ="com.gbbtbb.agendaviewerwidget.agendaREFRESHEDDONE_ACTION";

    private Typeface weatherFont;

    private Context ctx;
    public Handler handler = new Handler();;
    private static int REFRESH_DELAY = 30000;

    Runnable refreshView = new Runnable()
    {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, REFRESH_DELAY);
        }
    };

    private class ViewHolder {
        TextView date;
        TextView time;
        TextView humidity;
        TextView temperature;
        TextView weatherId;
    }

    private void refresh(){
        setLoadingInProgress(true);

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
        Log.i("AgendaWidgetMain", "onDestroyView ");
        handler.removeCallbacksAndMessages(null);
        getActivity().unregisterReceiver(agendaViewBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i("AgendaWidgetMain", "onActivityCreated");

        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");

        IntentFilter filter = new IntentFilter();
        filter.addAction(AGENDAREFRESHEDDONE_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(agendaViewBroadcastReceiver, filter);
        ctx = getActivity();

        final HorizontalScrollView hsv = (HorizontalScrollView)getView().findViewById(R.id.hsv);

        // Register a callback for when the fragment and its views have been layed out on the screen, and it is now safe to get their dimensions.
        ViewGroup parentLayout = ((ViewGroup) getView().getParent());
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {

                //dimensions could be measured here

                // remove observer now
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    hsv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    hsv.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                // It is now also safe to refresh the agenda itself, with correct dimensions
                handler.post(refreshView);
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
    }

    private void setLoadingInProgress(boolean state) {

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.agendaviewer_loadingProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);
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

                LinearLayout days_layout = (LinearLayout)getView().findViewById(R.id.innerLay);
                days_layout.removeAllViews();

                View dayView = null;
                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                dayView = mInflater.inflate(R.layout.agenda_item, null);

                for (AgendaItem e: Globals.agendaItems) {

                    View weatherTimeSlotView = null;
                    ViewHolder holder = null;

                    // Build unitary weather time slot block
                    weatherTimeSlotView = mInflater.inflate(R.layout.agenda_weatheritem, null);
                    holder = new ViewHolder();
                    holder.date = (TextView) weatherTimeSlotView.findViewById(R.id.agendaitem_date);
                    holder.time = (TextView) weatherTimeSlotView.findViewById(R.id.agendaitem_time);
                    holder.humidity = (TextView) weatherTimeSlotView.findViewById(R.id.agendaitem_humidity);
                    holder.temperature = (TextView) weatherTimeSlotView.findViewById(R.id.agendaitem_temperature);
                    holder.weatherId = (TextView) weatherTimeSlotView.findViewById(R.id.agendaitem_weatherId);
                    holder.weatherId.setTypeface(weatherFont);

                    // Fill its data
                    Date date = new Date(e.getDatetime());
                    SimpleDateFormat simpleDateFormat_time = new SimpleDateFormat("H");
                    String timestring = simpleDateFormat_time.format(date) +"h";
                    SimpleDateFormat simpleDateFormat_date = new SimpleDateFormat("EEEE dd/MM");
                    String datestring = simpleDateFormat_date.format(date);

                    String humidity = String.format("%d %%", e.getWeatherHumidity());
                    String temperature = String.format("%.0f °C",e.getWeatherTemperature());

                    holder.date.setText(datestring);
                    holder.time.setText(timestring);
                    holder.humidity.setText(humidity);
                    holder.temperature.setText(temperature);
                    holder.weatherId.setText(getWeatherIconText(e.getWeatherId()));

                    // and insert it in weather slots layout for that day
                    LinearLayout weatherlayout = (LinearLayout)dayView.findViewById(R.id.agenda_item_weatheritemslayout);
                    weatherlayout.addView(weatherTimeSlotView);
                }
                days_layout.addView(dayView);
            }
        }
    };

    private String getWeatherIconText(int actualId){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
                icon = getActivity().getString(R.string.weather_sunny);
        } else {
            switch(id) {
                case 2 : icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }
}
