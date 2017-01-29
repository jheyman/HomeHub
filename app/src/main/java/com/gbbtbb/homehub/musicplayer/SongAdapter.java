package com.gbbtbb.homehub.musicplayer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gbbtbb.homehub.R;

import java.util.List;

public class SongAdapter extends ArrayAdapter<SongItem> {

    private class ViewHolder {
        TextView number;
        TextView title;
        TextView duration;
    }

    Context context;

    public SongAdapter(Context context, int resourceId, List<SongItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    // create a new View for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        SongItem song = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.song_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.song_title);
            holder.duration = (TextView) convertView.findViewById(R.id.song_duration);
            holder.number = (TextView) convertView.findViewById(R.id.song_number);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.number.setText(Integer.toString(position+1));
        holder.title.setText(song.getTitle());
        holder.duration.setText(song.getDuration());

        return convertView;
    }
}

