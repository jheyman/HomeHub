package com.gbbtbb.homehub.musicplayer;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.gbbtbb.homehub.CustomTelnetClient;
import com.gbbtbb.homehub.Globals;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.gbbtbb.homehub.musicplayer.MusicPlayerMain.EXTRA_ALBUMLOAD_PROGRESS;
import static com.gbbtbb.homehub.musicplayer.MusicPlayerMain.EXTRA_LOADALBUM_NAME;
import static com.gbbtbb.homehub.musicplayer.MusicPlayerMain.EXTRA_SONGPLAYING_NAME;
import static com.gbbtbb.homehub.musicplayer.MusicPlayerMain.EXTRA_VOLUME_FEEDBACK;


public class MusicPlayerService extends IntentService{

    public static final String TAG = "MusicPlayerService";
    private static final int ALBUM_THUMBNAIL_SIZE=128;
    private static final String LMS_PLAYER_MACADDRESS = "b8:27:eb:d4:00:99";
    private static final String REMOTEAUDIOCONTROL_IPADDRESS = "192.168.0.20";
    private static final int REMOTEAUDIOCONTROL_PORT = 10000;
    private static final String LMS_IPADDRESS = "192.168.0.13";
    private static final int LMS_MAINPORT = 9000;
    private static final int LMS_CLIPORT = 9090;
    public MusicPlayerService() {
        super(MusicPlayerService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();

        Log.i(TAG, "onHandleIntent action= " + action);

        if (action.equals(MusicPlayerMain.ALBUM_LIST_REFRESH_ACTION)) {

            Globals.musicAlbumItems = new ArrayList<>();

            // get full list of albums from server
            // the "tags:jla" option specifies to return artwork_track_id (j) and album_name (l) and artist name (a)
            String command = "albums 0 999 tags:jla";
            String list = sendMusicServerCommand(command,"");

            if (list != "") {

                // The returned string is of the format:
                // <echoed command> id:XXX album:ZZZZZZZZZ [artwork_track_id:XXXX] artist:ZZZZ count:XXX
                // artwork_track_id param is only present for albums that have a valid cover image available

                Log.i(TAG, "albums list returned: " + list);

                // remove "count:<XXX>" from the end of the string
                int index = list.indexOf(" count:");
                list = list.substring(0, index);

                Log.i(TAG, "albums list returned(1): " + list);

                // replace id: & album: keywords by an arbitrary symbol, and split on this symbol
                list = list.replace(" id:", ";");
                list = list.replace(" album:", ";");
                list = list.replace(" artist:", ";");
                String tmp[] = list.split(";");

                // the resulting string list has album ID values and Album names (and potentially artwork_track_id) in sequence
                for (int i = 0; i < tmp.length; i += 3) {

                    String albumID = tmp[i];
                    Log.i(TAG, "AlbumID:" + albumID);

                    String albumName="";
                    String albumArtworkID="0";
                    String albumArtist="";
                    if (tmp[i+1].contains(" artwork_track_id:")) {
                        tmp[i+1] = tmp[i+1].replace(" artwork_track_id:", ";");
                        String tmp2[] = tmp[i+1].split(";");
                        albumName = tmp2[0];
                        albumArtworkID = tmp2[1];
                    }
                    else
                    {
                        albumName = tmp[i+1];
                    }
                    albumArtist = tmp[i+2];

                    Log.i(TAG, "AlbumName:" + albumName);
                    Log.i(TAG, "AlbumArtist:" + albumArtist);

                    // Get album cover image
                    String charset = "UTF-8";
                    String query = "";
                    Bitmap b = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);

                    try {
                        query = String.format("http://%s:%d/music/%s/cover_%dx%d.jpg?player=%s",
                                LMS_IPADDRESS,
                                LMS_MAINPORT,
                                albumArtworkID,
                                ALBUM_THUMBNAIL_SIZE,
                                ALBUM_THUMBNAIL_SIZE,
                                URLEncoder.encode(LMS_PLAYER_MACADDRESS, charset));
                    }
                    catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "MusicPlayerService: Error encoding URL params: " + e.toString());
                    }

                    try {
                        URL url = new URL(query);
                        Log.i(TAG, "MusicPlayerService: querying image via url: " + query);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.connect();
                        InputStream imageDataStream = new BufferedInputStream(conn.getInputStream());
                        b = BitmapFactory.decodeResourceStream(null, null, imageDataStream, null, null);
                        imageDataStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, "MusicPlayerService: exception reading image over network");
                    }

                    AlbumItem album = new AlbumItem(albumName, albumArtist, b);
                    Globals.musicAlbumItems.add(album);

                    Intent progressIntent = new Intent();
                    progressIntent.setAction(MusicPlayerMain.ALBUMS_LOADING_PROGRESS_EVENT);
                    int progress=  (100*i) / tmp.length;
                    progressIntent.putExtra(EXTRA_ALBUMLOAD_PROGRESS, progress);
                    progressIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    sendBroadcast(progressIntent);
                }
            }
            else
                Log.e(TAG, "returned album list is empty");

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.ALBUM_LIST_REFRESH_ACTION_DONE);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(MusicPlayerMain.GET_LMS_INFO_ACTION)) {

            String cmd2 = LMS_PLAYER_MACADDRESS + " mixer volume ?";
            String feedback = sendMusicServerCommand(cmd2, "");

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.GET_LMS_INFO_ACTION_DONE);
            doneIntent.putExtra(EXTRA_VOLUME_FEEDBACK, feedback);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(MusicPlayerMain.LOADALBUM_ACTION)) {

            String albumName = intent.getStringExtra(EXTRA_LOADALBUM_NAME);

            String command = LMS_PLAYER_MACADDRESS + " playlist clear";
            sendMusicServerCommand(command, "");

            command = LMS_PLAYER_MACADDRESS + " playlist addalbum * * ";
            sendMusicServerCommand(command, albumName);

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.LOADALBUM_ACTION_DONE);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);

            command = LMS_PLAYER_MACADDRESS + " title ?";
            String ret = sendMusicServerCommand(command, "");

            Log.i(TAG, "Active song: " + ret);

            Intent songPlayingIntent = new Intent();
            songPlayingIntent.setAction(MusicPlayerMain.SONGPLAYING_EVENT);
            songPlayingIntent.putExtra(EXTRA_SONGPLAYING_NAME, ret);
            songPlayingIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(songPlayingIntent);
        }
        else if (action.equals(MusicPlayerMain.POWERON_ACTION)) {

            sendRemoteAudioControlCommand("power_on");

            try{
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
            }

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.POWERON_ACTION_DONE);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(MusicPlayerMain.POWEROFF_ACTION)) {

            // stop any currently playing song
            String command = LMS_PLAYER_MACADDRESS + " stop";
            sendMusicServerCommand(command, "");

            sendRemoteAudioControlCommand("power_off");

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.POWEROFF_ACTION_DONE);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(MusicPlayerMain.PLAY_ACTION)) {

            String command = LMS_PLAYER_MACADDRESS + " play ";
            sendMusicServerCommand(command, "");

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.PLAY_ACTION_DONE);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(MusicPlayerMain.PAUSE_ACTION)) {

            String command = LMS_PLAYER_MACADDRESS + " pause 1";
            sendMusicServerCommand(command, ""); ;

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.PAUSE_ACTION_DONE);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(MusicPlayerMain.PREVIOUSSONG_ACTION)) {

            String command = LMS_PLAYER_MACADDRESS + " playlist index -1";
            sendMusicServerCommand(command, "");

            command = LMS_PLAYER_MACADDRESS + " title ?";
            String ret = sendMusicServerCommand(command, "");

            Log.i(TAG, "Active song after PREVIOUS action: " + ret);

            Intent songPlayingIntent = new Intent();
            songPlayingIntent.setAction(MusicPlayerMain.SONGPLAYING_EVENT);
            songPlayingIntent.putExtra(EXTRA_SONGPLAYING_NAME, ret);
            songPlayingIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(songPlayingIntent);

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.PREVIOUSSONG_ACTION_DONE);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(MusicPlayerMain.NEXTSONG_ACTION)) {

            String command = LMS_PLAYER_MACADDRESS + " playlist index +1";
            sendMusicServerCommand(command, "");

            command = LMS_PLAYER_MACADDRESS + " title ?";
            String ret = sendMusicServerCommand(command, "");

            Log.i(TAG, "Active song after NEXT action: " + ret);

            Intent songPlayingIntent = new Intent();
            songPlayingIntent.setAction(MusicPlayerMain.SONGPLAYING_EVENT);
            songPlayingIntent.putExtra(EXTRA_SONGPLAYING_NAME, ret);
            songPlayingIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(songPlayingIntent);

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.NEXTSONG_ACTION_DONE);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(MusicPlayerMain.VOLUMEUP_ACTION)) {

            String command = LMS_PLAYER_MACADDRESS + " mixer volume +5";
            sendMusicServerCommand(command, "");

            String cmd2 = LMS_PLAYER_MACADDRESS + " mixer volume ?";
            String feedback = sendMusicServerCommand(cmd2, "");

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.VOLUMEUP_ACTION_DONE);
            doneIntent.putExtra(EXTRA_VOLUME_FEEDBACK, feedback);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
        else if (action.equals(MusicPlayerMain.VOLUMEDOWN_ACTION)) {

            String command = LMS_PLAYER_MACADDRESS + " mixer volume -5";
            sendMusicServerCommand(command, "");

            String cmd2 = LMS_PLAYER_MACADDRESS + " mixer volume ?";
            String feedback = sendMusicServerCommand(cmd2, "");

            Intent doneIntent = new Intent();
            doneIntent.setAction(MusicPlayerMain.VOLUMEDOWN_ACTION_DONE);
            doneIntent.putExtra(EXTRA_VOLUME_FEEDBACK, feedback);
            doneIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(doneIntent);
        }
    }

    private String sendMusicServerCommand(String cmd, String params) {

        String responseFromServer="";
        String fullCmd="";
        String ret="";
        try {
            CustomTelnetClient ctc = new CustomTelnetClient();
            ctc.connect(LMS_IPADDRESS, LMS_CLIPORT);
            String[] responses= {"\n"};

            String encodedParams = "";
            try {
                // Note: LMS is old and does not support spaces encoded as "+": they should be percent-encoded
                encodedParams = URLEncoder.encode(params, "UTF-8").replaceAll("\\+", "%20");
            }
            catch (UnsupportedEncodingException e) {
                Log.e(TAG, "EXCEPTION encoding parameter");
            }

            fullCmd = cmd + encodedParams;

            Log.i(TAG, "sendMusicServerCommand: sending command " + fullCmd);
            responseFromServer = ctc.write(fullCmd, responses);
        } catch (java.io.IOException e) {
            Log.e(TAG, "sendMusicServerCommand exception: " + e.toString());
        }

        Log.i(TAG, "sendMusicServerCommand: received raw response: " + responseFromServer.toString());

        String afterDecode = "";
        try {
            afterDecode = URLDecoder.decode(responseFromServer.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "sendMusicServerCommaned EXCEPTION decoding response");

        }

        Log.i(TAG, "sendMusicServerCommand: decoded response: " + afterDecode);

        // for cases where data is returned in addition to the mirrored command,
        // remove command itself + one space from the beginning of the string to get
        // the actual interesting response data
        // Also, when the command ends with " ?" (some queries do), this " ?" will not be present
        // in the mirrored command in the response, so take it int account.
        int substringIndex = fullCmd.length() + 1 - (fullCmd.endsWith(" ?") ? 2:0);
        if (afterDecode.length() > substringIndex) {
            ret = afterDecode.substring(substringIndex).replace("\n","");

            Log.i(TAG, "sendMusicServerCommand: returning useful decoded part: [" + ret + "]");
        }
        else {
            ret = "";
            Log.i(TAG, "sendMusicServerCommand: no data returned");
        }

        return ret;
    }

    private String sendRemoteAudioControlCommand(String cmd) {
    String result="";
        try {
            Socket socket;
            PrintWriter out;
            BufferedReader in;

            socket = new Socket(REMOTEAUDIOCONTROL_IPADDRESS, REMOTEAUDIOCONTROL_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            //in = new BufferedReader(
            //        new InputStreamReader(socket.getInputStream()));

            out.print(cmd);
            //result = in.readLine();

            out.close();
            socket.close();
        } catch (java.io.IOException e) {
            Log.e(TAG, "sendRemoteAudioControlCommand exception: " + e.toString());
        }
        return result.toString();
    }
}
