package com.gbbtbb.homehub.agendaviewer;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntegerRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gbbtbb.homehub.CustomTextView;
import com.gbbtbb.homehub.Globals;
import com.gbbtbb.homehub.R;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AgendaWidgetMain extends Fragment {

    public static final String TAG = "AgendaWidgetMain";

    public static final String REFRESH_ACTION ="com.gbbtbb.agendaviewerwidget.REFRESH_ACTION";
    public static final String AGENDAREFRESHEDDONE_ACTION ="com.gbbtbb.agendaviewerwidget.agendaREFRESHEDDONE_ACTION";

    private Typeface weatherFont;

    private Context ctx;
    public Handler handler = new Handler();;
    private static int REFRESH_DELAY = 300000;

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

                // Show an explanation to the user *asynchronously* -- don't block
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

            LinearLayout MultiDaysLayout = (LinearLayout)getView().findViewById(R.id.innerLay);
            MultiDaysLayout.removeAllViews();

            Calendar now = Calendar.getInstance();

            long startOfDay = now.getTimeInMillis();

            now.set(Calendar.HOUR_OF_DAY, 24);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);

            long endOfDay = now.getTimeInMillis();

            for (int day=0; day < 5; day++) {

                View OneDayView = null;
                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                OneDayView = mInflater.inflate(R.layout.agenda_item, null);

                Date date = new Date(startOfDay);
                SimpleDateFormat simpleDateFormat_date = new SimpleDateFormat("EEEE dd MMM");
                String datestring = simpleDateFormat_date.format(date);

                ViewHolder holder = new ViewHolder();
                holder.date = (TextView) OneDayView.findViewById(R.id.agendaitem_date);
                holder.date.setText(datestring);

                for (WeatherItem e : Globals.weatherItems) {

                    long weatherItemTime = e.getDatetime();
                    Date weatherItemDate = new Date(e.getDatetime());

                    if (weatherItemTime >= startOfDay && weatherItemTime <= endOfDay) {

                        View weatherTimeSlotView = null;

                        // Build unitary weather time slot block
                        weatherTimeSlotView = mInflater.inflate(R.layout.agenda_weatheritem, null);

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(weatherItemDate);
                        int hours = cal.get(Calendar.HOUR_OF_DAY);

                        int selectedBackground = 0;
                        int textColor = 0;

                        if (hours <=6 || hours >= 22) {
                            selectedBackground = R.drawable.weather_item_night;
                            textColor = R.color.agendaviewer_nighttext_color;
                        } else if (hours > 6 && hours <=9) {
                            selectedBackground = R.drawable.weather_item_dawn;
                            textColor = R.color.agendaviewer_dawntext_color;
                        } else if (hours > 9 && hours <=18) {
                            selectedBackground = R.drawable.weather_item_day;
                            textColor = R.color.agendaviewer_daytext_color;
                        } else if (hours > 18) {
                            selectedBackground = R.drawable.weather_item_dusk;
                            textColor = R.color.agendaviewer_dusktext_color;
                        }

                        weatherTimeSlotView.setBackgroundResource(selectedBackground);

                        CustomTextView timeText = (CustomTextView) weatherTimeSlotView.findViewById(R.id.agendaitem_time);
                        timeText.setTextColor(getResources().getColor(textColor));

                        CustomTextView humText = (CustomTextView) weatherTimeSlotView.findViewById(R.id.agendaitem_humidity);
                        humText.setTextColor(getResources().getColor(textColor));

                        CustomTextView tempText = (CustomTextView) weatherTimeSlotView.findViewById(R.id.agendaitem_temperature);
                        tempText.setTextColor(getResources().getColor(textColor));

                        CustomTextView weatherIdText = (CustomTextView) weatherTimeSlotView.findViewById(R.id.agendaitem_weatherId);
                        weatherIdText.setTextColor(getResources().getColor(textColor));

                        holder.time = (TextView) weatherTimeSlotView.findViewById(R.id.agendaitem_time);
                        holder.humidity = (TextView) weatherTimeSlotView.findViewById(R.id.agendaitem_humidity);
                        holder.temperature = (TextView) weatherTimeSlotView.findViewById(R.id.agendaitem_temperature);
                        holder.weatherId = (TextView) weatherTimeSlotView.findViewById(R.id.agendaitem_weatherId);
                        holder.weatherId.setTypeface(weatherFont);

                        SimpleDateFormat simpleDateFormat_time = new SimpleDateFormat("H");
                        String timestring = simpleDateFormat_time.format(weatherItemDate) + "h";

                        String humidity = String.format("%d %%", e.getWeatherHumidity());
                        String temperature = String.format("%.0f °C", e.getWeatherTemperature());

                        holder.time.setText(timestring);
                        holder.humidity.setText(humidity);
                        holder.temperature.setText(temperature);

                        holder.weatherId.setText(getWeatherIconText(e.getWeatherId(), e.getDatetime(),e.getSunrise() , e.getSunset()));

                        // and insert it in weather slots layout for that day
                        LinearLayout dailyweatherlayout = (LinearLayout) OneDayView.findViewById(R.id.agenda_item_weatheritemslayout);
                        dailyweatherlayout.addView(weatherTimeSlotView);
                    }
                }

                // Now proceed to fill the agenda events view
                LinearLayout agendaItemsLayout = (LinearLayout) OneDayView.findViewById(R.id.agenda_item_agendaitemslayout);

                int nb_added = 0;
                for (AgendaItem e : Globals.agendaItems) {
                    long agendaItemTime = e.getDatetime();

                    if (agendaItemTime >= startOfDay && agendaItemTime <= endOfDay) {
                        nb_added++;
                        View v = null;
                        v = mInflater.inflate(R.layout.agenda_eventline, null);
                        CustomTextView ctv = (CustomTextView) v.findViewById(R.id.agendaitem_eventline);

                        Date agendadate = new Date(agendaItemTime);
                        SimpleDateFormat sdf = new SimpleDateFormat("H");
                        ctv.setText("à " + sdf.format(agendadate) + "h: " + e.getTitle());
                        agendaItemsLayout.addView(v);
                    }
                }

                // If there are no agenda events for that day, insert a dummy/empty event to show it
                if (nb_added == 0) {
                    View v = null;
                    v = mInflater.inflate(R.layout.agenda_eventline, null);

                    CustomTextView ctv = (CustomTextView) v.findViewById(R.id.agendaitem_eventline);
                    ctv.setText(R.string.agendaviewer_noevent_text);
                    agendaItemsLayout.addView(v);
                }

                // finally add this view to the overall horizontal scrollview showing all days
                MultiDaysLayout.addView(OneDayView);

                startOfDay = endOfDay;
                endOfDay += 24*60*60*1000;
            }
        }
        }
    };


    private String getWeatherIconText(int actualId, long datetime, long sunrise, long sunset){

        String icon="";
        //Format df = DateFormat.getDateFormat(ctx);
        //Format tf = DateFormat.getTimeFormat(ctx);
        //Log.i(TAG, "getWeatherIconText: datetime is " + df.format(datetime) + " at " + tf.format(datetime));
        //Log.i(TAG, "getWeatherIconText: sunrise is " + df.format(sunrise) + " at " + tf.format(sunrise));
        //Log.i(TAG, "getWeatherIconText: sunset is " + df.format(sunset) + " at " + tf.format(sunset));

        Log.i(TAG, "getWeatherIconText: actualID is " + Integer.toString(actualId));

        boolean isDaytime =  datetime>=sunrise && datetime<sunset;

        switch(actualId) {
            case 200 :
            case 201 :
            case 202 :
            case 210 :
            case 211 :
            case 212 :
            case 221 :
            case 230 :
            case 231 :
            case 232 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_thunder_day);
                } else {
                    icon = getActivity().getString(R.string.weather_thunder_night);
                }
                break;
            case 300 :
            case 301 :
            case 302 :
            case 310 :
            case 311 :
            case 312 :
            case 313 :
            case 314 :
            case 321 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_drizzle_day);
                } else {
                    icon = getActivity().getString(R.string.weather_drizzle_night);
                }
                break;
            case 500 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_lightrain_day);
                } else {
                    icon = getActivity().getString(R.string.weather_lightrain_night);
                }
                break;
            case 501 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_moderaterain_day);
                } else {
                    icon = getActivity().getString(R.string.weather_moderaterain_night);
                }
                break;
            case 502 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_heavyrain_day);
                } else {
                    icon = getActivity().getString(R.string.weather_heavyrain_night);
                }
                break;
            case 503 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_veryheavyrain_day);
                } else {
                    icon = getActivity().getString(R.string.weather_veryheavyrain_night);
                }
                break;
            case 504 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_extremerain_day);
                } else {
                    icon = getActivity().getString(R.string.weather_extremerain_night);
                }
                break;
            case 511 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_freezingrain_day);
                } else {
                    icon = getActivity().getString(R.string.weather_freezingrain_night);
                }
                break;
            case 520 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_lightshowerrain_day);
                } else {
                    icon = getActivity().getString(R.string.weather_lightshowerrain_night);
                }
                break;
            case 521 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_showerrain_day);
                } else {
                    icon = getActivity().getString(R.string.weather_showerrain_night);
                }
                break;
            case 522 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_heavyshower_day);
                } else {
                    icon = getActivity().getString(R.string.weather_heavyshower_night);
                }
                break;
            case 531 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_raggedshowerrain_day);
                } else {
                    icon = getActivity().getString(R.string.weather_raggedshowerrain_night);
                }
                break;
            case 600 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_snowy_day);
                } else {
                    icon = getActivity().getString(R.string.weather_snowy_night);
                }
                break;
            case 741 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_foggy_day);
                } else {
                    icon = getActivity().getString(R.string.weather_foggy_night);
                }
                break;
            case 800 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_sunny_day);
                } else {
                    icon = getActivity().getString(R.string.weather_clear_night);
                }
                break;
            case 801 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_fewclouds_day);
                } else {
                    icon = getActivity().getString(R.string.weather_fewclouds_night);
                }
                break;
            case 802 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_scatteredclouds_day);
                } else {
                    icon = getActivity().getString(R.string.weather_scatteredclouds_night);
                }
                break;
            case 803 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_brokenclouds_day);
                } else {
                    icon = getActivity().getString(R.string.weather_brokenclouds_night);
                }
                break;
            case 804 :
                if(isDaytime) {
                    icon = getActivity().getString(R.string.weather_overcastclouds_day);
                } else {
                    icon = getActivity().getString(R.string.weather_overcastclouds_night);
                }
                break;
        }

        return icon;
    }
}
