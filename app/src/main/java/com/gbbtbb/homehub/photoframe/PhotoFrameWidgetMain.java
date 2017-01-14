package com.gbbtbb.homehub.photoframe;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gbbtbb.homehub.Globals;
import com.gbbtbb.homehub.R;

import java.io.File;

public class PhotoFrameWidgetMain extends Fragment {

    public static final String TAG = "PhotoFrameWidgetMain";
    public static String GETLOWRESIMAGE_ACTION = "com.gbbtbb.PhotoFrameWidgetProvider.GETLOWRESIMAGE_ACTION";
    public static String LOWRESLOADINGDONE_ACTION = "com.gbbtbb.PhotoFrameWidgetProvider.LOWRESLOADINGDONE_ACTION";
    public static String FULLRESSAVE_ACTION = "com.gbbtbb.PhotoFrameWidgetProvider.FULLRESSAVE_ACTION";
    public static String FULLRESSAVINGDONE_ACTION = "com.gbbtbb.PhotoFrameWidgetProvider.FULLRESSAVINGDONE_ACTION";

    private static String imageName = "";
    private static int imageHeight ;
    private static int imageWidth ;
    private static int imageOrientation ;

    String imageDumpPath;

    public Handler handler = new Handler();
    private Context ctx;
    private static int REFRESH_DELAY = 60000;

    Runnable refreshView = new Runnable()
    {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, REFRESH_DELAY);
        }
    };

    private void refresh() {
        Intent intent = new Intent(ctx.getApplicationContext(), PhotoFrameWidgetService.class);
        intent.setAction(GETLOWRESIMAGE_ACTION);
        ctx.startService(intent);
    }

    private void sendAsEmail() {
        // Show progress bar, this saving the image to disk may take a while
        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.photoframe_loadingProgress);
        pb.setVisibility(View.VISIBLE);

        // Build the intent to save full res image to disk
        Intent saveImgIntent = new Intent(ctx.getApplicationContext(), PhotoFrameWidgetService.class);
        saveImgIntent.setAction(FULLRESSAVE_ACTION);
        saveImgIntent.putExtra("savepath", imageDumpPath);
        saveImgIntent.putExtra("imagepath", imageName);
        saveImgIntent.putExtra("height", imageHeight);
        saveImgIntent.putExtra("width", imageWidth);
        saveImgIntent.putExtra("orientation", imageOrientation);
        ctx.startService(saveImgIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.photoframewidget_layout, container, false);
    }

    @Override
    public void onDestroyView()
    {
        Log.i(TAG, "onDestroyView" );
        handler.removeCallbacksAndMessages(null);
        getActivity().unregisterReceiver(photoFrameViewBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(FULLRESSAVINGDONE_ACTION);
        filter.addAction(LOWRESLOADINGDONE_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(photoFrameViewBroadcastReceiver, filter);
        ctx = getActivity();

        imageDumpPath = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();

        ImageView iv = (ImageView)getView().findViewById(R.id.photoframe_imageView);

        //  register a click event on the image itself
        iv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ProgressBar pb = (ProgressBar)getView().findViewById(R.id.photoframe_loadingProgress);
                pb.setVisibility(View.VISIBLE);
                refresh();
            }
        });

        ImageView emailIV = (ImageView)getView().findViewById(R.id.photoframe_emailicon);

        //  register a click event on the image itself
        emailIV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendAsEmail();
            }
        });

        // Initial call to the service
        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.photoframe_loadingProgress);
        pb.setVisibility(View.VISIBLE);
        Intent intent = new Intent(ctx.getApplicationContext(), PhotoFrameWidgetService.class);
        intent.setAction(GETLOWRESIMAGE_ACTION);
        ctx.startService(intent);

        // Start background handler that will call refresh regularly
        handler.postDelayed(refreshView, REFRESH_DELAY);
    }

    private final BroadcastReceiver photoFrameViewBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            ProgressBar pb = (ProgressBar)getView().findViewById(R.id.photoframe_loadingProgress);

            //Log.i(TAG, "onReceive " + action);
            if (action.equals(FULLRESSAVINGDONE_ACTION)) {

                // Hide progress bar now
                pb.setVisibility(View.GONE);

                // Then send it as an attachment in an email
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                emailIntent.setType("text/html");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Photo");
                emailIntent.putExtra(Intent.EXTRA_TEXT, imageName);

                File f = new File(imageDumpPath, "temp.png");

                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                ctx.startActivity(emailIntent);
            } else if (action.equals(LOWRESLOADINGDONE_ACTION)) {

                imageName = intent.getStringExtra("imagepath");
                imageWidth = intent.getIntExtra("width", 0);
                imageHeight = intent.getIntExtra("heigth", 0);
                imageOrientation = intent.getIntExtra("orientation", 0);

                Log.i(TAG, "imagePath is " + imageName);
                Log.i(TAG, "imageHeight is " + Integer.toString(imageHeight));
                Log.i(TAG, "imageWidth is " + Integer.toString(imageWidth));
                Log.i(TAG, "imageOrientation is " + Integer.toString(imageOrientation));
                // image is about to be displayed: hide progress bar now.
                pb.setVisibility(View.GONE);

                // Update image title/path
                TextView tv = (TextView)getView().findViewById(R.id.photoframe_textView);
                tv.setText(imageName);

                //Bitmap bitmap = (Bitmap) intent.getParcelableExtra("BitmapImage");
                ImageView iv = (ImageView)getView().findViewById(R.id.photoframe_imageView);
                iv.setImageBitmap(Globals.photoFrameBitmap);

                pb.setVisibility(View.GONE);
            }
        }
    };
}
