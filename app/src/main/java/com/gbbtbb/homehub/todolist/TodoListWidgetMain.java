package com.gbbtbb.homehub.todolist;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.gbbtbb.homehub.Globals;
import com.gbbtbb.homehub.R;

import java.util.ArrayList;
import java.util.List;

public class TodoListWidgetMain extends Fragment implements AdapterView.OnItemClickListener {

    public static final String TAG = "TodoListWidgetMain";

    public static String CLEANLIST_ACTION = "com.gbbtbb.todolistwidget.CLEANLIST";
    public static String CLEANLISTDONE_ACTION = "com.gbbtbb.todolistwidget.CLEANLISTDONE";

    public static String ADDITEM_ACTION = "com.gbbtbb.todolistwidget.ADDITEM";
    public static String ADDITEMDONE_ACTION = "com.gbbtbb.todolistwidget.ADDITEMDONE";

    public static String RELOAD_ACTION = "com.gbbtbb.todolistwidget.RELOAD_LIST";
    public static String RELOADDONE_ACTION = "com.gbbtbb.todolistwidget.RELOAD_LIST_DONE";

    public static String DELETEITEM_ACTION = "com.gbbtbb.todolistwidget.DELETEITEM";
    public static String DELETEITEMDONE_ACTION = "com.gbbtbb.todolistwidget.DELETEITEMDONE";

    public static String EXTRA_ITEM_ID = "com.gbbtbb.todolistwidget.itemid";
    public static String EXTRA_ITEM_PRIO = "com.gbbtbb.todolistwidget.itemprio";
    public static String EXTRA_ITEM_CREATIONDATE = "com.gbbtbb.todolistwidget.itemcreationdate";

    public static String EXTRA_DELETEDITEM_POSITION = "com.gbbtbb.todolistwidget.deleteditemposition";

    public static String ACTION_CANCELLED = "com.gbbtbb.todolistwidget.ACTION_CANCELLED";

    List<TodoListRowItem> rowItems;
    TodoListViewAdapter adapter;
    public static final int NB_DUMMY_ITEMS = 25;

    private Context ctx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.todolist_layout, container, false);
    }

    @Override
    public void onDestroyView()
    {
        getActivity().unregisterReceiver(TodoListBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ADDITEMDONE_ACTION);
        filter.addAction(DELETEITEMDONE_ACTION);
        filter.addAction(RELOADDONE_ACTION);
        filter.addAction(CLEANLISTDONE_ACTION);
        filter.addAction(ACTION_CANCELLED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(TodoListBroadcastReceiver, filter);
        ctx = getActivity();

        ImageView addItemIcon = (ImageView)getView().findViewById(R.id.todowidget_addItem);

        //  register a click event on the add item button
        addItemIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setLoadingInProgress(true);
                Intent intent = new Intent(ctx.getApplicationContext(), com.gbbtbb.homehub.todolist.AddItemMenuActivity.class);
                startActivity(intent);
            }
        });

        ImageView cleanIcon = (ImageView)getView().findViewById(R.id.todowidget_cleanList);

        //  register a click event on the clean list button
        cleanIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setLoadingInProgress(true);
                Intent intent = new Intent(ctx.getApplicationContext(), CleanListMenuActivity.class);
                startActivity(intent);
            }
        });

        ImageView reloadIcon = (ImageView)getView().findViewById(R.id.todowidget_reloadList);

        //  register a click event on the reload button
        reloadIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reloadList();
            }
        });

        // Initialize list
        rowItems = new ArrayList<>();

        ListView listView = (ListView) getView().findViewById(R.id.todowidget_todo_list);
        adapter = new TodoListViewAdapter(ctx, R.layout.todolist_item, rowItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        // Initialize list
        reloadList();
    }

    private void reloadList() {
        setLoadingInProgress(true);
        Intent intent = new Intent(ctx.getApplicationContext(), TodoListWidgetService.class);
        intent.setAction(RELOAD_ACTION);
        ctx.startService(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        setLoadingInProgress(true);
        final String item = rowItems.get(position).getItemName();
        Intent intent = new Intent(ctx.getApplicationContext(), DeleteItemMenuActivity.class);
        Bundle b = new Bundle();
        b.putString(EXTRA_ITEM_ID, item);
        b.putInt(EXTRA_DELETEDITEM_POSITION, position);
        intent.putExtras(b);
        intent.setClass(ctx, DeleteItemMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ctx.startActivity(intent);
    }

    private final BroadcastReceiver TodoListBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if (action.equals(ADDITEMDONE_ACTION)) {
                setLoadingInProgress(false);

                String new_item = intent.getStringExtra(EXTRA_ITEM_ID);
                int priority = intent.getIntExtra(EXTRA_ITEM_PRIO, 0);
                String creation_date = intent.getStringExtra(EXTRA_ITEM_CREATIONDATE);

                adapter.insert(new TodoListRowItem(new_item, priority, creation_date), adapter.getCount() - NB_DUMMY_ITEMS);
                Log.i(TAG, "TodoListBroadcastReceiver added item { " + new_item +
                        "} with priority " + Integer.toString(priority) +
                        " and creation date " + creation_date);
            }
            else if (action.equals(CLEANLISTDONE_ACTION)) {

                setLoadingInProgress(false);
                adapter.clear();
                reloadList();
            }
            else if (action.equals(DELETEITEMDONE_ACTION)) {

                setLoadingInProgress(false);
                int position = intent.getIntExtra(EXTRA_DELETEDITEM_POSITION, 0);
                rowItems.remove(position);
                adapter.notifyDataSetChanged();
            }
            else if (action.equals(RELOADDONE_ACTION)) {

                Log.i(TAG, "processing RELOAD_ACTION_DONE...");
                setLoadingInProgress(false);

                adapter.clear();
                for (TodoListRowItem e: Globals.todoListItems) {

                    adapter.add(e);
                    if (! "".equals(e.getItemName())) {
                        Log.i(TAG, "Adding todo item " + e);
                    }
                }
            }
            else if (action.equals(ACTION_CANCELLED)) {

                // just stop the progress bar then
                setLoadingInProgress(false);
            }
        }
    };

    private void setLoadingInProgress(boolean state) {
        //Log.i(TodoListWidgetMain.TAG, "setLoadingInProgress " + Boolean.toString(state));

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.todowidget_loadingProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);

        ImageView iv = (ImageView)getView().findViewById(R.id.todowidget_reloadList);
        iv.setVisibility(state ? View.GONE: View.VISIBLE);
    }
}
