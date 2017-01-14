package com.gbbtbb.homehub.shoppinglist;

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
import java.util.ArrayList;


public class ShoppingListWidgetService extends IntentService {

    public static final String TAG = "ShoppingListWidgetSvc";

    public ShoppingListWidgetService() {
        super(ShoppingListWidgetService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();

        Log.i(TAG, "onHandleIntent action= " + action);

        if (action.equals(ShoppingListWidgetMain.RELOAD_ACTION)) {

            Globals.shoppingListItems = getItems();

            Intent doneIntent = new Intent();
            doneIntent.setAction(ShoppingListWidgetMain.RELOADDONE_ACTION);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(ShoppingListWidgetMain.ADDITEM_ACTION)) {

            String item = intent.getStringExtra(ShoppingListWidgetMain.EXTRA_ITEM_ID);
            addItem(item);

            Intent doneIntent = new Intent();
            doneIntent.setAction(ShoppingListWidgetMain.ADDITEMDONE_ACTION);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            doneIntent.putExtra(ShoppingListWidgetMain.EXTRA_ITEM_ID, item);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(ShoppingListWidgetMain.DELETEITEM_ACTION)) {

            String item = intent.getStringExtra(ShoppingListWidgetMain.EXTRA_ITEM_ID);
            int position = intent.getIntExtra(ShoppingListWidgetMain.EXTRA_DELETEDITEM_POSITION, 0);
            deleteItem(item);

            Intent doneIntent = new Intent();
            doneIntent.setAction(ShoppingListWidgetMain.DELETEITEMDONE_ACTION);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            doneIntent.putExtra(ShoppingListWidgetMain.EXTRA_ITEM_ID, item);
            doneIntent.putExtra(ShoppingListWidgetMain.EXTRA_DELETEDITEM_POSITION, position);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(ShoppingListWidgetMain.CLEANLIST_ACTION)) {

            deleteItem("*");
            Intent doneIntent = new Intent();
            doneIntent.setAction(ShoppingListWidgetMain.CLEANLISTDONE_ACTION);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
    }

    public ArrayList<String> getItems() {

        ArrayList<String> list = new ArrayList<String>();

        String result = httpRequest("http://192.168.0.13:8081/shoppinglist.php");

        // Parse the received JSON data
        try {
            JSONObject jdata = new JSONObject(result);
            JSONArray jArray = jdata.getJSONArray("items");

            for(int i=0;i<jArray.length();i++){
                JSONObject jobj = jArray.getJSONObject(i);
                String item = jobj.getString("item");
                list.add(item);
            }

        } catch(JSONException e){
            Log.e(ShoppingListWidgetMain.TAG, "Error parsing data "+e.toString());
        }

        // Add a few empty items so that the list looks good even with no item present
        for(int i=0;i<ShoppingListWidgetMain.NB_DUMMY_ITEMS;i++){
            list.add("");
        }

        return list;
    }

    public void addItem(String newItemName) {

        String query = "";
        String charset = "UTF-8";

        try {
            query = String.format("http://192.168.0.13:8081/shoppinglist_insert.php?newitem=%s",
                    URLEncoder.encode(newItemName, charset));
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getImage: Error encoding URL params: " + e.toString());
        }
        
        httpRequest(query);

        Log.i(TAG, "ShoppingListDataProvider: requested to add new item: " + newItemName);
    }

    public void deleteItem(String selection) {
        Log.i(TAG, "ShoppingListDataProvider: request for deleting " + selection);

        String query = "";
        String charset = "UTF-8";

        try {
            query = String.format("http://192.168.0.13:8081/shoppinglist_delete.php?whereClause=%s",
                    URLEncoder.encode(selection, charset));
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getImage: Error encoding URL params: " + e.toString());
        }

        httpRequest(query);
    }

    private String httpRequest(String url) {
        String result = "";

        Log.i(TAG, "Performing HTTP request " + url);

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
            Log.e(TAG, "httpRequest: Error in http connection "+e.toString());
        }

        String data ;
        if (result.length() <= 128)
            data = result;
        else
            data = "[long data....]";

        Log.i(TAG, "httpRequest completed, received "+ result.length() + " bytes: " + data);

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
