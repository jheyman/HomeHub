package com.gbbtbb.homehub.shoppinglist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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

public class ShoppingListViewAdapter extends ArrayAdapter<ShoppingListRowItem> {

    Context context;

    public ShoppingListViewAdapter(Context context, int resourceId,
                                 List<ShoppingListRowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        ShoppingListRowItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.shoppinglist_item, null);
            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView.findViewById(R.id.shoppingitem_title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.shoppingitem_icon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.imageView.setImageBitmap(drawTextOnList(context, rowItem.getTitle(), 17, rowItem.getImageId()));

        return convertView;
    }

    public Bitmap drawTextOnList(Context ctx, String textToDisplay, int size, int background)
    {
        Bitmap tmpBitmap = BitmapFactory.decodeResource(ctx.getResources(), background);
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
