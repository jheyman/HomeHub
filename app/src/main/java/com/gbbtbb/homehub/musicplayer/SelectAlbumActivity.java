package com.gbbtbb.homehub.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.gbbtbb.homehub.Globals;
import com.gbbtbb.homehub.R;

import java.util.ArrayList;
import java.util.List;

public class SelectAlbumActivity extends Activity {
    public static final String TAG = "SelectAlbumActivity";
    List<AlbumItem> albumItems;
    AlbumAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.musicplayer_selectalbum_layout);

		albumItems = new ArrayList<>();

		GridView gridview = (GridView) findViewById(R.id.gridview);

        adapter = new AlbumAdapter(this, R.layout.album_item, albumItems);
        gridview.setAdapter(adapter);

		adapter.clear();

		for (AlbumItem ai:  Globals.musicAlbumItems) {
			adapter.add(ai);
		}

		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
									int position, long id) {

                // Retrieve clicked object
                AlbumItem ai = albumItems.get(position);
                Log.i(TAG, "Selected: " + ai.getAlbumTitle());

                Globals.selectedAlbum = ai;

                // Trig service to load album
				Intent intent = new Intent(getApplicationContext(), MusicPlayerService.class);
				intent.setAction(MusicPlayerMain.LOADALBUM_ACTION);
                intent.putExtra(MusicPlayerMain.EXTRA_LOADALBUM_NAME, ai.getAlbumTitle());
				startService(intent);

                SelectAlbumActivity.this.finish();
			}
		});
	}
}
