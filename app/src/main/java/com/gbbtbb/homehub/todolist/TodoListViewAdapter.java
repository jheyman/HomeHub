package com.gbbtbb.homehub.todolist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbbtbb.homehub.R;
import com.gbbtbb.homehub.Utilities;

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
           // holder.priorityIcon = (ImageView) convertView.findViewById(R.id.todoitem_priorityicon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        //holder.imageView.setImageBitmap(drawTextOnList(context, rowItem.getTitle(), 17, rowItem.getImageId()));
  //      holder.imageView.setImageBitmap(drawTextOnList(context, rowItem.getTitle(), 17, holder.imageView));



        holder.item.setText(rowItem.getTitle());
        holder.creationDate.setText("xxxxxxx");
        //holder.priorityIcon.setImageResource(R.drawable.bulb_on);


        return convertView;
    }

    public Bitmap drawTextOnList(Context ctx, String textToDisplay, int size, ImageView img)
    {
        //Bitmap tmpBitmap = BitmapFactory.decodeResource(ctx.getResources(), background);
        Bitmap tmpBitmap = ((BitmapDrawable)img.getDrawable()).getBitmap();

        Bitmap mutableBitmap = tmpBitmap.copy(tmpBitmap.getConfig(), true);

        Canvas myCanvas = new Canvas(mutableBitmap);
        Typeface myfont = Typeface.createFromAsset(ctx.getAssets(),"fonts/passing_notes.ttf");

        TextPaint textPaint = new TextPaint();
        textPaint.setTypeface(myfont);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(size);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setAntiAlias(true);
        textPaint.setSubpixelText(true);

        float textHeight = Utilities.getTextHeight(textPaint, "0");
        myCanvas.drawText(textToDisplay, 0.2f*myCanvas.getWidth(), (0.5f) * myCanvas.getHeight() + 0.5f * textHeight, textPaint);

        return mutableBitmap;
    }
}
