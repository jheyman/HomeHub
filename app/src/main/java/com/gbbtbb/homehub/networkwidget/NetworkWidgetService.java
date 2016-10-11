package com.gbbtbb.homehub.networkwidget;

import android.app.IntentService;
import android.content.Intent;

public class NetworkWidgetService extends IntentService {

    public NetworkWidgetService() {
        super(NetworkWidgetService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final String action = intent.getAction();


        if (NetworkWidgetMain.REFRESH_ACTION.equals(action)) {


        }
    }



}