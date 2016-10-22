package com.gbbtbb.homehub.todolist;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gbbtbb.homehub.R;

import java.util.List;

public class TodoListViewAdapter extends ArrayAdapter<com.gbbtbb.homehub.todolist.TodoListRowItem> {

    Context context;

    public TodoListViewAdapter(Context context, int resourceId,
                               List<com.gbbtbb.homehub.todolist.TodoListRowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    private class ViewHolder {
        TextView item;
        TextView creationDate;
        ImageView priorityIcon;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        TodoListRowItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.todolist_item, null);
            holder = new ViewHolder();
            holder.creationDate = (TextView) convertView.findViewById(R.id.todoitem_creationdate);
            holder.item = (TextView) convertView.findViewById(R.id.todo_item);
            holder.priorityIcon = (ImageView) convertView.findViewById(R.id.todoitem_priorityicon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.item.setText(rowItem.getItemName());
        holder.creationDate.setText(rowItem.getCreationDate());

        int priority = rowItem.getPriority();
        int icon = 0;
        switch(priority) {
            case 1:
                icon = R.drawable.priority1;
                break;
            case 2:
                icon = R.drawable.priority2;
                break;
            case 3:
                icon = R.drawable.priority3;
                break;
            default:
                break;
        }

        holder.priorityIcon.setImageResource(icon);

        LinearLayout l = (LinearLayout) convertView.findViewById(R.id.todo_item_backgroundlayout);

        if ("".equals(rowItem.getItemName())) {

            l.setBackgroundResource(R.drawable.todolist_border_emptyslot);
        } else {
            l.setBackgroundResource(R.drawable.todolist_border_fullslot);

        }

        return convertView;
    }

}
