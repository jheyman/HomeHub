package com.gbbtbb.homehub.musicplayer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbbtbb.homehub.R;

import java.util.List;

public class AlbumAdapter extends ArrayAdapter<AlbumItem> {

    private class ViewHolder {
        TextView albumTitle;
        TextView albumArtist;
        ImageView albumCover;
    }

    Context context;

    public AlbumAdapter(Context context, int resourceId, List<AlbumItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    // create a new View for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        AlbumItem album = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.album_item, null);
            holder = new ViewHolder();
            holder.albumTitle = (TextView) convertView.findViewById(R.id.album_title);
            holder.albumArtist = (TextView) convertView.findViewById(R.id.album_artist);
            holder.albumCover = (ImageView) convertView.findViewById(R.id.album_cover);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.albumTitle.setText(album.getAlbumTitle());
        holder.albumArtist.setText(album.getAlbumArtist());
        holder.albumCover.setImageBitmap(album.getAlbumCover());

        return convertView;
    }
}

