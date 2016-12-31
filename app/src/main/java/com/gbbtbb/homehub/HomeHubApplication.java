package com.gbbtbb.homehub;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeHubApplication extends Application
{
    public static final String TAG = "HomeHubApplication";

    String loggingPath="";

    public void onCreate ()
    {
        loggingPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();
        Log.i(TAG, "App crashes will be logged into " + loggingPath);

        // All crash log code adapted from here: http://stackoverflow.com/questions/19897628/need-to-handle-uncaught-exception-and-send-log-file
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        Log.i(TAG, "xxxxxxxxxxxxxxxxxxxxxx Uncaught Exception Stack Trace xxxxxxxxxxxxxxxxxxxxxxxxxxx");
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        dumpLogcatToFile();
        System.exit(1); // kill off the crashed app
    }

    private void dumpLogcatToFile()
    {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo (this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
        }
        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;


        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentdateAndTime = simpleDateFormat.format(date);
        String fullName = loggingPath + "/crashlogfile"+currentdateAndTime+".log";

        Log.i(TAG, "logging crash to file " + fullName);

        // Extract logcat to file.
        File file = new File (fullName);
        InputStreamReader reader = null;
        FileWriter writer = null;
        try
        {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time HomeHubApp:v dalvikvm:v System.err:v *:s" :
                    "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader (process.getInputStream());

            // write output stream
            writer = new FileWriter (file);
            writer.write ("Android version: " +  Build.VERSION.SDK_INT + "\n");
            writer.write ("Device: " + model + "\n");
            writer.write ("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

            char[] buffer = new char[10000];
            do
            {
                int n = reader.read (buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write (buffer, 0, n);
            } while (true);

            reader.close();
            writer.close();
        }
        catch (IOException e)
        {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                }
        }
    }
}