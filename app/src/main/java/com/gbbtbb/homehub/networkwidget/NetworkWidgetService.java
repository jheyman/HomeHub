package com.gbbtbb.homehub.networkwidget;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbbtbb.homehub.Globals;
import com.gbbtbb.homehub.R;

import java.util.Iterator;

public class NetworkWidgetService extends IntentService {
    public static final String TAG = "NetworkWidgetService";

    public NetworkWidgetService() {
        super(NetworkWidgetService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final String action = intent.getAction();

        if (NetworkWidgetMain.REFRESH_ACTION.equals(action)) {

            if (Globals.graphLatestValues != null) {

                String[] deviceList = this.getResources().getStringArray(R.array.NetworkDeviceList);
                for (String d : deviceList) {

                    Float val = Globals.graphLatestValues.get(d);

                    if (val != null) {
                        int arrayId = this.getResources().getIdentifier(d, "array", this.getPackageName());
                        String[] temp = this.getResources().getStringArray(arrayId);
                        int level = val > 0 ? 1 : 0;
                        updateImageView(temp[0], level);
                    }
                }
            }
        }
    }

    private void updateImageView(String imgViewId, int level) {
        int imageViewId = this.getResources().getIdentifier(imgViewId, "id", this.getPackageName());

        int iconOnId = this.getResources().getIdentifier("rj45_green", "drawable", this.getPackageName());
        int iconOffId = this.getResources().getIdentifier("rj45_red", "drawable", this.getPackageName());

        int imgId = level == 0 ? iconOffId : iconOnId;

        // Notify main class of view to refresh
        Intent updateImageViewIntent = new Intent();
        updateImageViewIntent.setAction(NetworkWidgetMain.UPDATE_IMAGEVIEW_ACTION);
        updateImageViewIntent.addCategory(Intent.CATEGORY_DEFAULT);
        updateImageViewIntent.putExtra(NetworkWidgetMain.UPDATE_IMAGEVIEW_EXTRA_VIEWID, imageViewId);
        updateImageViewIntent.putExtra(NetworkWidgetMain.UPDATE_IMAGEVIEW_EXTRA_IMGID, imgId);
        sendBroadcast(updateImageViewIntent);
    }
}