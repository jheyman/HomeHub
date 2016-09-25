package com.gbbtbb.homehub;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements OnItemClickListener {

    public static final String[] titles = new String[] { "un",
            "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix", "onze", "douze", "treize", "quatorze", "quinze" };

    public static final Integer image = R.drawable.paperpad_one_row;

    List<ShoppingListRowItem> rowItems;

    ZWaveWidgetMain zwave;

    public Handler handler = new Handler();
    private Context ctx;
    private static int REFRESH_DELAY = 20000;

    Runnable refreshWidget = new Runnable()
    {
        @Override
        public void run() {

            Toast toast = Toast.makeText(getApplicationContext(), "Run! ", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();

            zwave.update(ctx);

            handler.postDelayed(this, REFRESH_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.content_main);

        //TODO: replace with actual shoppinglist items
        rowItems = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            ShoppingListRowItem item = new ShoppingListRowItem(image, titles[i]);
            rowItems.add(item);
        }

        ListView listView = (ListView) findViewById(R.id.shopping_list);
        ShoppingListViewAdapter adapter = new ShoppingListViewAdapter(this, R.layout.shoppinglist_item, rowItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        zwave = new ZWaveWidgetMain(this, this);

        // Start background handler that will call refresh regularly
        ctx= this;
        handler.postDelayed(refreshWidget, REFRESH_DELAY);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Item " + (position + 1) + ": " + rowItems.get(position),
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
