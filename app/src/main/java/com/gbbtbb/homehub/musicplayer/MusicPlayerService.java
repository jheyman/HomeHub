package com.gbbtbb.homehub.musicplayer;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.gbbtbb.homehub.Globals;

import java.util.ArrayList;


public class MusicPlayerService extends IntentService{

    public static final String TAG = "MusicPlayerService";

    public MusicPlayerService() {
        super(MusicPlayerService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();

        Log.i(TAG, "onHandleIntent action= " + action);

        if (action.equals(MusicPlayerMain.REFRESH_ACTION)) {

            Globals.musicAlbumItems = getItems();

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.REFRESH_ACTION_DONE);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
    }

    public ArrayList<AlbumItem> getItems() {

        ArrayList<AlbumItem> list = new ArrayList<AlbumItem>();

        for(int i=0;i<50;i++){
            list.add(new AlbumItem("Album #"+Integer.toString(i)));
        }

        return list;
    }
}
