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

public class SelectSongActivity extends Activity {
    public static final String TAG = "SelectSongActivity";
    List<SongItem> songItems;
    SongAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.musicplayer_selectsong_layout);

		songItems = new ArrayList<>();

		GridView gridview = (GridView) findViewById(R.id.gridview);

        adapter = new SongAdapter(this, R.layout.song_item, songItems);
        gridview.setAdapter(adapter);

		adapter.clear();

		for (SongItem si:  Globals.musicAlbumSongItems) {
			adapter.add(si);
		}

		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
									int position, long id) {

                // Retrieve clicked object
                SongItem si = songItems.get(position);
                Log.i(TAG, "Selected song: " + si.getTitle());

                // Trig service to launch this song
				Intent intent = new Intent();
                intent.putExtra(MusicPlayerMain.EXTRA_SONG_INDEX, position);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
                setResult(Activity.RESULT_OK,intent);

                SelectSongActivity.this.finish();
			}
		});
	}


}
