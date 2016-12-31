package com.gbbtbb.homehub.photoframe;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Debug;
import android.text.TextPaint;
import android.util.Log;

import com.gbbtbb.homehub.Globals;
import com.gbbtbb.homehub.R;
import com.gbbtbb.homehub.Utilities;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class PhotoFrameWidgetService extends IntentService {

    public static final String TAG = "PhotoFrameWidgetService";
    public PhotoFrameWidgetService() {
        super(PhotoFrameWidgetService.class.getName());
    }

    class ImageInfo
    {
        public String path;
        public int width;
        public int heigth;
        public int orientation;
    };

    final String BASE_DIR= "/mnt/photo";
    final int MIN_IMAGE_WIDTH = 128;
    final int MAX_IMAGE_WIDTH = 8192;
    final int TARGET_DISPLAYED_IMAGE_WIDTH = 800;
    final int TARGET_SAVED_IMAGE_WIDTH = 1600;

    @Override
    protected void onHandleIntent(Intent intent) {

        Bitmap b = null;

        final String action = intent.getAction();

        Log.i(TAG, "onHandleIntent action= " + action);

        if (action.equals(PhotoFrameWidgetMain.FULLRESSAVE_ACTION)) {

            String imagePath = intent.getStringExtra("imagepath");
            int width = intent.getIntExtra("width", 0);
            int heigth = intent.getIntExtra("height", 0);
            int orientation = intent.getIntExtra("orientation", 0);

            // get image data, with no rescaling
            b = getImage(imagePath, width, heigth, orientation, TARGET_SAVED_IMAGE_WIDTH);

            // Save image to temporary location
            String path = intent.getStringExtra("savepath");
            Log.i(TAG, "Saving path = " + path);

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(path+"/temp.png");
                b.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Notify widget that image has been saved to disk
            Intent doneIntent = new Intent();
            doneIntent.setAction(PhotoFrameWidgetMain.FULLRESSAVINGDONE_ACTION);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);

        } else if (action.equals(PhotoFrameWidgetMain.GETLOWRESIMAGE_ACTION)) {
            // Get a random image from remote server
            ImageInfo inf = getRandomImageInfo(BASE_DIR);

            String pathToRandomImage = inf.path;
            int width = inf.width;
            int heigth = inf.heigth;

            // Just filtering out suspiciously small or large images
            if ((width > MIN_IMAGE_WIDTH) && (width < MAX_IMAGE_WIDTH)) {

                b = getImage(pathToRandomImage, width, heigth, inf.orientation, TARGET_DISPLAYED_IMAGE_WIDTH);

                Log.i(TAG, "refreshing widget with image " + pathToRandomImage);
                buildUpdate(b, inf);
            }
            else {
                Log.i(TAG, String.format("image width (%d) is out of bounds [%d, %d]", width, MIN_IMAGE_WIDTH, MAX_IMAGE_WIDTH));
            }
        }
    }

    private void buildUpdate(Bitmap b, ImageInfo inf) {

        Bitmap bret;
        if (b != null) {
            bret = b;
        }
        else {
            Log.e(TAG, "NULL bitmap: skipping");
            bret = BitmapFactory.decodeResource(getResources(), R.drawable.photoframe_preview);
        }

        final Intent doneIntent = new Intent();
        doneIntent.setAction(PhotoFrameWidgetMain.LOWRESLOADINGDONE_ACTION);
        doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
        //doneIntent.putExtra("BitmapImage", bret);
        Globals.photoFrameBitmap = bret;
        doneIntent.putExtra("Text", inf.path.substring(BASE_DIR.length()));

        doneIntent.putExtra("imagepath", inf.path);
        doneIntent.putExtra("width", inf.width);
        doneIntent.putExtra("heigth", inf.heigth);
        doneIntent.putExtra("orientation", inf.orientation);
        sendBroadcast(doneIntent);
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
            Log.e(TAG,"httpRequest: Error in http connection "+e.toString());
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

    private ImageInfo getRandomImageInfo(String basePath) {

        String charset = "UTF-8";
        String query = "";
        ImageInfo inf = new ImageInfo();

        try {
            query = String.format("http://192.168.0.13:8081/getRandomImagePath.php?basepath=%s",
                    URLEncoder.encode(basePath, charset));
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getRandomImageInfo: error encoding URL params: " + e.toString());
        }

        String result = httpRequest(query);

        String[] parts = result.split(";");

        inf.path = parts[0];

        try{
            inf.width = Integer.parseInt(parts[1]);
            inf.heigth = Integer.parseInt(parts[2]);
        } catch (NumberFormatException n) {
            Log.e(TAG, "getRandomImageInfo: error parsing image width or height: " + n.toString());
            inf.width = 512;
            inf.heigth = 512;
        }

        if (parts.length > 3) {
            try{
                inf.orientation = Integer.parseInt(parts[3]);
            } catch (NumberFormatException n) {
                Log.e(TAG, "getRandomImageInfo: error parsing image orientation: " + n.toString());
                inf.orientation = 1;
            }
        } else {
            // EXIF orientation data is not available: set default value
            inf.orientation = 1;
        }

        return inf;
    }

    public static double getAvailableMemoryInMB(){

        final Runtime runtime = Runtime.getRuntime();
        final long usedMemInMB=(runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        final long maxHeapSizeInMB=runtime.maxMemory() / 1048576L;

        return (double)(maxHeapSizeInMB - usedMemInMB);
    }

    private Bitmap getImage(String path, int originalWidth, int originalHeight, int originalOrientation, int targetWidth) {

        Bitmap b=null;
        Bitmap temp;

        String charset = "UTF-8";
        String query = "";

        try {
            query = String.format("http://192.168.0.13:8081/getImage.php?path=%s",
                    URLEncoder.encode(path, charset));
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getImage: Error encoding URL params: " + e.toString());
        }

        double am = getAvailableMemoryInMB();

        try {
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            InputStream imageDataStream = new BufferedInputStream(conn.getInputStream());

            // decode the input image data stream into a bitmap, and resample as appropriate to fit a predefined size
            // this is useful to avoid decoding very large images into equally large bitmaps, which could cause out of
            // memory conditions on the device, and is not useful anyway since rendered image will be quite small.
            BitmapFactory.Options options = new BitmapFactory.Options();

            int inSampleSize = 1;

            if (originalWidth > targetWidth) {

                while ((originalWidth / inSampleSize) > targetWidth) {
                    inSampleSize *= 2;
                }
            }

            Log.i(TAG, "getImage: resampling factor=" + Integer.toString(inSampleSize));

            // Only proceed to load the resampled image if it is going to fit in available memory
            float resampledBitmapSizeInMB = ((float)(originalWidth/inSampleSize)*(originalHeight/inSampleSize)*4)/(1024*1024);
            if ( resampledBitmapSizeInMB < 0.9*am ) {

                Log.i(TAG, "Enough RAM available (" + Double.toString(0.9*am) + " MB) to load image (need " + Float.toString(resampledBitmapSizeInMB)+" MB)");

                options.inSampleSize = inSampleSize;
                temp = BitmapFactory.decodeResourceStream(null, null, imageDataStream, null, options);
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;

                imageDataStream.close();

                // Rotate image depending on the orientation setting gathered from the image EXIF data
                int rotationAngle = 0;
                switch (originalOrientation) {
                    case 1:
                        rotationAngle = 0;
                        break;
                    case 3:
                        rotationAngle = 180;
                        break;
                    case 6:
                        rotationAngle = 90;
                        break;
                    case 8:
                        rotationAngle = 270;
                        break;
                    default:
                        Log.e(TAG, "Orientation value " + Integer.toString(originalOrientation) + " unknown");
                        break;
                }

                // if needed rotate image depending on EXIF orientation data, to view it heads-up
                if (rotationAngle != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotationAngle);
                    b = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), matrix, true);
                } else {
                    b = temp;
                }
            }
            else {
                Log.e(TAG, "Too few RAM available (" + Double.toString(0.9*am) + " MB) to load image (needed " + Float.toString(resampledBitmapSizeInMB)+" MB)");

                String debugText = "Low RAM";
                b = Bitmap.createBitmap(320, 200, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(b);

                Utilities.fillCanvas(canvas, getResources().getColor(R.color.photoframe_background_color));

                TextPaint textPaint = new TextPaint();
                textPaint.setStyle(Paint.Style.FILL);
                textPaint.setTextSize(12);
                textPaint.setColor(getResources().getColor(R.color.photoframe_text_color));

                textPaint.setTextAlign(Paint.Align.LEFT);
                textPaint.setAntiAlias(true);
                textPaint.setSubpixelText(true);

                float textHeight = Utilities.getTextHeight(textPaint, "0");
                float textWidth;

                textWidth = Utilities.getTextWidth(textPaint, debugText);
                canvas.drawText(debugText, 160 - 0.5f*textWidth, 100 + 0.5f*textHeight, textPaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "getImage: exception reading image over network");
        }

        return b;
    }
}