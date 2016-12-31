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

public class AlbumListViewAdapter extends ArrayAdapter<AlbumItem> {

    Context context;

    public AlbumListViewAdapter(Context context, int resourceId,
                               List<AlbumItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    private class ViewHolder {
        TextView item;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        AlbumItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.albumlist_item, null);
            holder = new ViewHolder();
            holder.item = (TextView) convertView.findViewById(R.id.album_foldername);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.item.setText(rowItem.getItemName());

        return convertView;
    }
}
