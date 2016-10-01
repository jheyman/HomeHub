package com.gbbtbb.homehub;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListWidgetMain extends Fragment implements AdapterView.OnItemClickListener {

    public static final String TAG = "ShoppingList";

    public static String CLICK_ACTION = "com.gbbtbb.shoppinglistwidget.CLICK";

    public static String CLEANLIST_ACTION = "com.gbbtbb.shoppinglistwidget.CLEANLIST";
    public static String CLEANLISTDONE_ACTION = "com.gbbtbb.shoppinglistwidget.CLEANLISTDONE";

    public static String ADDITEM_ACTION = "com.gbbtbb.shoppinglistwidget.ADDITEM";
    public static String ADDITEMDONE_ACTION = "com.gbbtbb.shoppinglistwidget.ADDITEMDONE";


    public static String RELOAD_ACTION = "com.gbbtbb.shoppinglistwidget.RELOAD_LIST";
    public static String RELOADDONE_ACTION = "com.gbbtbb.shoppinglistwidget.RELOAD_LIST_DONE";

    public static String DELETEITEM_ACTION = "com.gbbtbb.shoppinglistwidget.DELETEITEM";
    public static String DELETEITEMDONE_ACTION = "com.gbbtbb.shoppinglistwidget.DELETEITEMDONE";

    public static String EXTRA_ITEM_ID = "com.gbbtbb.shoppinglistwidget.item";

    List<ShoppingListRowItem> rowItems;





    public static final String[] titles = new String[] { "ONE!",
            "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix", "onze", "douze", "treize", "quatorze", "quinze" };

    public static final Integer image = R.drawable.paperpad_one_row;

    ShoppingListViewAdapter adapter;


    private Context ctx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.shoppinglist_layout, container, false);
    }

    @Override
    public void onDestroyView()
    {
        getActivity().unregisterReceiver(photoFrameViewBroadcastReceiver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ADDITEMDONE_ACTION);
        filter.addAction(DELETEITEMDONE_ACTION);
        filter.addAction(RELOADDONE_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(photoFrameViewBroadcastReceiver, filter);
        ctx = getActivity();


        ImageView addItemIcon = (ImageView)getView().findViewById(R.id.addItem);

        //  register a click event on the add item button
        addItemIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ctx.getApplicationContext(), ShoppingListWidgetService.class);
                intent.setAction(ADDITEM_ACTION);
                ctx.startService(intent);
            }
        });

        ImageView cleanIcon = (ImageView)getView().findViewById(R.id.cleanList);

        //  register a click event on the clean list button
        cleanIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ctx.getApplicationContext(), ShoppingListWidgetService.class);
                intent.setAction(CLEANLIST_ACTION);
                ctx.startService(intent);
            }
        });

        ImageView reloadIcon = (ImageView)getView().findViewById(R.id.reloadList);

        //  register a click event on the reload button
        reloadIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                adapter.clear();

                Intent intent = new Intent(ctx.getApplicationContext(), ShoppingListWidgetService.class);
                intent.setAction(RELOAD_ACTION);
                ctx.startService(intent);
            }
        });


        //TODO: replace with actual shoppinglist items
        rowItems = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            ShoppingListRowItem item = new ShoppingListRowItem(image, titles[i]);
            rowItems.add(item);
        }

        ListView listView = (ListView) getView().findViewById(R.id.shopping_list);
        adapter = new ShoppingListViewAdapter(ctx, R.layout.shoppinglist_item, rowItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Toast toast = Toast.makeText(ctx, "Item " + (position + 1) + ": " + rowItems.get(position),
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
/*
        final String item = intent.getStringExtra(EXTRA_ITEM_ID);

        Bundle b = new Bundle();
        b.putString(EXTRA_ITEM_ID, item);
        intent.putExtras(b);
        intent.setClass(ctx, DeleteItemMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ctx.startActivity(intent);
              */
    }

    private final BroadcastReceiver photoFrameViewBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if (action.equals(ADDITEMDONE_ACTION)) {

/*
                final Context context = ctx;
                final Intent i = intent;
                sWorkerQueue.removeMessages(0);
                sWorkerQueue.post(new Runnable() {
                    @Override
                    public void run() {
                        final ContentResolver r = context.getContentResolver();

                        // We disable the data changed observer temporarily since each of the updates
                        // will trigger an onChange() in our data observer.
                        r.unregisterContentObserver(sDataObserver);

                        final ContentValues values = new ContentValues();

                        String newItemName = i.getExtras().getString(ADDITEM_ACTION);

                        setLoadingInProgress(context, true);

                        if (newItemName != null)
                        {
                            values.put(ShoppingListDataProvider.Columns.ITEM, newItemName);
                            r.insert(ShoppingListDataProvider.CONTENT_URI, values);
                        }
                        else
                            Log.i(ShoppingListWidgetMain.TAG, "onReceive/addItem got null newItemName");

                        r.registerContentObserver(ShoppingListDataProvider.CONTENT_URI, true, sDataObserver);

                        final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                        final ComponentName cn = new ComponentName(context, ShoppingListWidgetMain.class);
                        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.shopping_list);
                    }
                });
*/

            }
            else if (action.equals(CLEANLISTDONE_ACTION)) {

                        setLoadingInProgress(false);
                        // TODO: delete listview

            } else if (action.equals(CLICK_ACTION)) {
/*
                final String item = intent.getStringExtra(EXTRA_ITEM_ID);

                Bundle b = new Bundle();
                b.putString(EXTRA_ITEM_ID, item);
                intent.putExtras(b);
                intent.setClass(ctx, DeleteItemMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                ctx.startActivity(intent);
                */

            } else if (action.equals(DELETEITEMDONE_ACTION)) {

                        setLoadingInProgress(false);
            }

            else if (action.equals(RELOADDONE_ACTION)) {

                Log.i(ShoppingListWidgetMain.TAG, "processing RELOAD_ACTION_DONE...");
                setLoadingInProgress(false);

                for (String e: Globals.shoppingListItems) {

                    adapter.add(new ShoppingListRowItem(image, e));
                    Log.i(ShoppingListWidgetMain.TAG, "Adding shopping item " + e);
                }
            }
        }
    };



    private void setLoadingInProgress(boolean state) {

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.loadingProgress);
        pb.setVisibility(state ? View.INVISIBLE: View.VISIBLE);

        ImageView iv = (ImageView)getView().findViewById(R.id.reloadList);
        pb.setVisibility(state ? View.VISIBLE: View.INVISIBLE);
    }


    public Bitmap drawTextOnList(Context ctx, String textToDisplay, int background)
    {

        Bitmap tmpBitmap = BitmapFactory.decodeResource(ctx.getResources(), background);
        Bitmap mutableBitmap = tmpBitmap.copy(tmpBitmap.getConfig(), true);

        Canvas myCanvas = new Canvas(mutableBitmap);
        Typeface myfont = Typeface.createFromAsset(ctx.getAssets(),"fonts/passing_notes.ttf");

        TextPaint textPaint = new TextPaint();
        textPaint.setTypeface(myfont);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(30);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setAntiAlias(true);
        textPaint.setSubpixelText(true);

        StaticLayout sl = new StaticLayout(textToDisplay, textPaint, ((int)90*myCanvas.getWidth())/100, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);

        myCanvas.save();
        myCanvas.translate(55, 15);
        sl.draw(myCanvas);
        myCanvas.restore();

        return mutableBitmap;
    }


}
