package com.gbbtbb.homehub.musicplayer;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gbbtbb.homehub.Globals;
import com.gbbtbb.homehub.R;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerMain extends Fragment {

    public static final String TAG = "MusicPlayerMain";
    public static final String REFRESH_ACTION ="com.gbbtbb.musicplayerwidget.REFRESH_ACTION";
    public static final String REFRESH_ACTION_DONE ="com.gbbtbb.musicplayerwidget.REFRESH_ACTION_DONE";

    List<AlbumItem> rowItems;
    AlbumListViewAdapter adapter;

    private Context ctx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.musicplayerwidget_layout, container, false);
    }

    @Override
    public void onDestroyView()
    {
        Log.i(TAG, "onDestroyView" );
        getActivity().unregisterReceiver(NetworkViewBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(REFRESH_ACTION_DONE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(NetworkViewBroadcastReceiver, filter);
        ctx = getActivity();

        // Initialize list
        rowItems = new ArrayList<>();

        ListView listView = (ListView) getView().findViewById(R.id.musicplayer_folder_list);
        adapter = new AlbumListViewAdapter(ctx, R.layout.albumlist_item, rowItems);
        listView.setAdapter(adapter);

        Intent intent = new Intent(ctx.getApplicationContext(), MusicPlayerService.class);
        intent.setAction(REFRESH_ACTION);
        ctx.startService(intent);
    }

    private final BroadcastReceiver NetworkViewBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            Log.i(TAG, "onReceive " + action);

            if (action.equals(REFRESH_ACTION_DONE)) {

                Log.i(TAG, "processing REFRESH_ACTION_DONE...");

                ListView albumList = (ListView)getView().findViewById(R.id.musicplayer_folder_list);
                albumList.setVisibility(View.VISIBLE);

                TextView tv = (TextView) getView().findViewById(R.id.musicplayer_empty_view);
                tv.setVisibility(View.GONE);

                adapter.clear();
                for (AlbumItem e: Globals.musicAlbumItems) {

                    adapter.add(e);
                    if (! "".equals(e.getItemName())) {
                        Log.i(TAG, "Adding music item " + e);
                    }
                }
            }
        }
    };

}
