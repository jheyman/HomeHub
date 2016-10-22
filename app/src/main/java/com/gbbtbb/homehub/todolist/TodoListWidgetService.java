package com.gbbtbb.homehub.todolist;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.gbbtbb.homehub.Globals;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class TodoListWidgetService extends IntentService {

    public TodoListWidgetService() {
        super(com.gbbtbb.homehub.todolist.TodoListWidgetService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();

        Log.i("TodoListWidgetSvc", "onHandleIntent action= " + action);

        if (action.equals(com.gbbtbb.homehub.todolist.TodoListWidgetMain.RELOAD_ACTION)) {

            Globals.todoListItems = getItems();

            Intent doneIntent = new Intent();
            doneIntent.setAction(com.gbbtbb.homehub.todolist.TodoListWidgetMain.RELOADDONE_ACTION);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(com.gbbtbb.homehub.todolist.TodoListWidgetMain.ADDITEM_ACTION)) {

            String item = intent.getStringExtra(TodoListWidgetMain.EXTRA_ITEM_ID);
            int priority = intent.getIntExtra(TodoListWidgetMain.EXTRA_ITEM_PRIO, 0);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy à HH:mm");
            String creationDate = df.format(c.getTime());

            addItem(item, priority, creationDate);

            Intent doneIntent = new Intent();
            doneIntent.setAction(com.gbbtbb.homehub.todolist.TodoListWidgetMain.ADDITEMDONE_ACTION);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            doneIntent.putExtra(TodoListWidgetMain.EXTRA_ITEM_ID, item);
            doneIntent.putExtra(TodoListWidgetMain.EXTRA_ITEM_PRIO, priority);
            doneIntent.putExtra(TodoListWidgetMain.EXTRA_ITEM_CREATIONDATE, creationDate);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(com.gbbtbb.homehub.todolist.TodoListWidgetMain.DELETEITEM_ACTION)) {

            String item = intent.getStringExtra(com.gbbtbb.homehub.todolist.TodoListWidgetMain.EXTRA_ITEM_ID);
            int position = intent.getIntExtra(com.gbbtbb.homehub.todolist.TodoListWidgetMain.EXTRA_DELETEDITEM_POSITION, 0);
            deleteItem(item);

            Intent doneIntent = new Intent();
            doneIntent.setAction(com.gbbtbb.homehub.todolist.TodoListWidgetMain.DELETEITEMDONE_ACTION);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            doneIntent.putExtra(com.gbbtbb.homehub.todolist.TodoListWidgetMain.EXTRA_ITEM_ID, item);
            doneIntent.putExtra(com.gbbtbb.homehub.todolist.TodoListWidgetMain.EXTRA_DELETEDITEM_POSITION, position);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(com.gbbtbb.homehub.todolist.TodoListWidgetMain.CLEANLIST_ACTION)) {

            deleteItem("*");
            Intent doneIntent = new Intent();
            doneIntent.setAction(com.gbbtbb.homehub.todolist.TodoListWidgetMain.CLEANLISTDONE_ACTION);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
    }

    public ArrayList<TodoListRowItem> getItems() {

        ArrayList<TodoListRowItem> list = new ArrayList<TodoListRowItem>();

        String result = httpRequest("http://88.181.199.137:8081/todolist.php");

        // Parse the received JSON data
        try {
            JSONObject jdata = new JSONObject(result);
            JSONArray jArray = jdata.getJSONArray("items");

            for(int i=0;i<jArray.length();i++){
                JSONObject jobj = jArray.getJSONObject(i);
                String item = jobj.getString("item");
                int priority = jobj.getInt("priority");
                String creationDate = jobj.getString("creationdate");
                TodoListRowItem newElement = new TodoListRowItem(item, priority, creationDate);
                list.add(newElement);
            }

        } catch(JSONException e){
            Log.e(com.gbbtbb.homehub.todolist.TodoListWidgetMain.TAG, "Error parsing data "+e.toString());
        }

        // Add a few empty items so that the list looks good even with no item present
        for(int i=0;i< com.gbbtbb.homehub.todolist.TodoListWidgetMain.NB_DUMMY_ITEMS;i++){
            list.add(new TodoListRowItem("", 0, ""));
        }
/*
        try {
            Thread.sleep(4000);
        }
        catch (InterruptedException e) {

        }
*/
        return list;
    }

    public void addItem(String newItemName, int priority, String creationDate) {

        String query = "";
        String charset = "UTF-8";

        try {
            query = String.format("http://88.181.199.137:8081/todolist_insert.php?newitem=%s&priority=%d&creationdate=%s",
                    URLEncoder.encode(newItemName, charset), priority, URLEncoder.encode(creationDate, charset));
        }
        catch (UnsupportedEncodingException e) {
            Log.e("PhotoFrameWidgetService", "getImage: Error encoding URL params: " + e.toString());
        }

        httpRequest(query);

        Log.i(com.gbbtbb.homehub.todolist.TodoListWidgetMain.TAG, "TodoListDataProvider: requested to add new item: " + newItemName);
    }

    public void deleteItem(String selection) {
        Log.i(TodoListWidgetMain.TAG, "TodoListDataProvider: request for deleting " + selection);

        String query = "";
        String charset = "UTF-8";

        try {
            query = String.format("http://88.181.199.137:8081/todolist_delete.php?whereClause=%s",
                    URLEncoder.encode(selection, charset));
        }
        catch (UnsupportedEncodingException e) {
            Log.e("PhotoFrameWidgetService", "getImage: Error encoding URL params: " + e.toString());
        }

        httpRequest(query);
    }

    private String httpRequest(String url) {
        String result = "";

        Log.i("TodoListWidgetSvc", "Performing HTTP request " + url);

        try {

            URL targetUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) targetUrl.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = readStream(in);
            }
            finally {
                urlConnection.disconnect();
            }
        } catch(Exception e) {
            Log.e("TodoListWidgetSvc", "httpRequest: Error in http connection "+e.toString());
        }

        String data ;
        if (result.length() <= 128)
            data = result;
        else
            data = "[long data....]";

        Log.i("TodoListWidgetSvc", "httpRequest completed, received "+ result.length() + " bytes: " + data);

        return result;
    }

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }    

}
