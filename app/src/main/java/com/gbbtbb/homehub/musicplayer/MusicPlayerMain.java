package com.gbbtbb.homehub.musicplayer;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

import java.util.Random;

public class MusicPlayerMain extends Fragment {

    public static final String TAG = "MusicPlayerMain";

    public static final String GET_LMS_INFO_ACTION ="com.gbbtbb.musicplayerwidget.GET_LMS_INFO_ACTION";
    public static final String GET_LMS_INFO_ACTION_DONE ="com.gbbtbb.musicplayerwidget.GET_LMS_INFO_ACTION_DONE";

    public static final String ALBUM_LIST_REFRESH_ACTION ="com.gbbtbb.musicplayerwidget.ALBUM_LIST_REFRESH_ACTION";
    public static final String ALBUMS_LOADING_PROGRESS_EVENT ="com.gbbtbb.musicplayerwidget.ALBUMS_LOADING_PROGRESS_EVENT";
    public static String EXTRA_ALBUMLOAD_PROGRESS = "com.gbbtbb.shoppinglistwidget.albumLoadProgress";
    public static final String ALBUM_LIST_REFRESH_ACTION_DONE ="com.gbbtbb.musicplayerwidget.ALBUM_LIST_REFRESH_ACTION_DONE";

    public static final String LOADALBUM_ACTION ="com.gbbtbb.musicplayerwidget.LOADALBUM_ACTION";
    public static final String LOADALBUM_ACTION_DONE ="com.gbbtbb.musicplayerwidget.LOADALBUM_ACTION_DONE";
    public static String EXTRA_LOADALBUM_NAME = "com.gbbtbb.shoppinglistwidget.loadAlbumName";

    public static final String PLAY_ACTION ="com.gbbtbb.musicplayerwidget.PLAY_ACTION";
    public static final String PLAY_ACTION_DONE ="com.gbbtbb.musicplayerwidget.PLAY_ACTION_DONE";

    public static final String POWERON_ACTION ="com.gbbtbb.musicplayerwidget.POWERON_ACTION";
    public static final String POWERON_ACTION_DONE ="com.gbbtbb.musicplayerwidget.POWERON_ACTION_DONE";

    public static final String POWEROFF_ACTION ="com.gbbtbb.musicplayerwidget.POWEROFF_ACTION";
    public static final String POWEROFF_ACTION_DONE ="com.gbbtbb.musicplayerwidget.POWERONOFF_ACTION_DONE";

    public static final String PAUSE_ACTION ="com.gbbtbb.musicplayerwidget.PAUSE_ACTION";
    public static final String PAUSE_ACTION_DONE ="com.gbbtbb.musicplayerwidget.PAUSE_ACTION_DONE";

    public static final String NEXTSONG_ACTION ="com.gbbtbb.musicplayerwidget.NEXTSONG_ACTION";
    public static final String NEXTSONG_ACTION_DONE ="com.gbbtbb.musicplayerwidget.NEXTSONG_ACTION_DONE";

    public static final String PREVIOUSSONG_ACTION ="com.gbbtbb.musicplayerwidget.PREVIOUSSONG_ACTION";
    public static final String PREVIOUSSONG_ACTION_DONE ="com.gbbtbb.musicplayerwidget.PREVIOUSSONG_ACTION_DONE";

    public static String EXTRA_VOLUME_FEEDBACK = "com.gbbtbb.shoppinglistwidget.volumeFeedback";

    public static final String VOLUMEUP_ACTION ="com.gbbtbb.musicplayerwidget.VOLUMEUP_ACTION";
    public static final String VOLUMEUP_ACTION_DONE ="com.gbbtbb.musicplayerwidget.VOLUMEUP_ACTION_DONE";

    public static final String VOLUMEDOWN_ACTION ="com.gbbtbb.musicplayerwidget.VOLUMEDOWN_ACTION";
    public static final String VOLUMEDOWN_ACTION_DONE ="com.gbbtbb.musicplayerwidget.VOLUMEDOWN_ACTION_DONE";

    public static final String SONGPLAYING_EVENT ="com.gbbtbb.musicplayerwidget.SONGPLAYING_EVENT";
    public static String EXTRA_SONGPLAYING_NAME = "com.gbbtbb.shoppinglistwidget.songPlayingName";

    private Context ctx;

    private boolean isPowerOn;

    public Handler handler = new Handler();
    private static int AUTOLOADALBUM_DELAY = 1800000;
    private static int AUTOLOADALBUM_INITIALDELAY = 120000;

    Runnable autoLoadAlbum = new Runnable()
    {
        @Override
        public void run() {
            Log.i(TAG, "autoLoadAlbum CALLED, ctx=" + ctx.toString());
            // Do not autoload albums if the player is currently being used
            if (!isPowerOn)
                loadRandomAlbum();
            handler.postDelayed(this, AUTOLOADALBUM_DELAY);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.musicplayerwidget_layout, container, false);
    }

    @Override
    public void onDestroyView()
    {
        Log.i(TAG, "onDestroyView" );
        getActivity().unregisterReceiver(NetworkViewBroadcastReceiver);
        super.onDestroyView();
    }

    private void setAlbumLoadingInProgress(boolean state) {
        //Log.i(TAG, "setAlbumLoadingInProgress " + Boolean.toString(state));

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.musicplayer_albumloadingProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);
    }
    private void setPowerOnInProgress(boolean state) {
        //Log.i(TAG, "setPowerOnInProgress " + Boolean.toString(state));

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.musicplayer_powerOnInProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);
    }
    private void setPowerOffInProgress(boolean state) {
        //Log.i(TAG, "setPowerOffInProgress " + Boolean.toString(state));

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.musicplayer_powerOffInProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);
    }
    private void setPlayInProgress(boolean state) {
        //Log.i(TAG, "setPlayInProgress " + Boolean.toString(state));

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.musicplayer_playInProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);
    }
    private void setPauseInProgress(boolean state) {
        //Log.i(TAG, "setPauseInProgress " + Boolean.toString(state));

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.musicplayer_pauseInProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);
    }
    private void setPreviousInProgress(boolean state) {
        //Log.i(TAG, "setPreviousInProgress " + Boolean.toString(state));

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.musicplayer_previousInProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);
    }
    private void setNextInProgress(boolean state) {
        //Log.i(TAG, "setNextInProgress " + Boolean.toString(state));

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.musicplayer_nextInProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);
    }
    private void setVolDownInProgress(boolean state) {
        //Log.i(TAG, "setVolDownInProgress " + Boolean.toString(state));

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.musicplayer_volDownInProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);
    }
    private void setVolUpInProgress(boolean state) {
        //Log.i(TAG, "setVolUpInProgress " + Boolean.toString(state));

        ProgressBar pb = (ProgressBar)getView().findViewById(R.id.musicplayer_volUpInProgress);
        pb.setVisibility(state ? View.VISIBLE: View.GONE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ALBUM_LIST_REFRESH_ACTION_DONE);
        filter.addAction(LOADALBUM_ACTION_DONE);
        filter.addAction(POWEROFF_ACTION_DONE);
        filter.addAction(POWERON_ACTION_DONE);
        filter.addAction(PLAY_ACTION_DONE);
        filter.addAction(PAUSE_ACTION_DONE);
        filter.addAction(NEXTSONG_ACTION_DONE);
        filter.addAction(PREVIOUSSONG_ACTION_DONE);
        filter.addAction(VOLUMEDOWN_ACTION_DONE);
        filter.addAction(VOLUMEUP_ACTION_DONE);
        filter.addAction(SONGPLAYING_EVENT);
        filter.addAction(GET_LMS_INFO_ACTION_DONE);
        filter.addAction(ALBUMS_LOADING_PROGRESS_EVENT);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        getActivity().registerReceiver(NetworkViewBroadcastReceiver, filter);
        ctx = getActivity();

        isPowerOn = false;

        ImageView albumView = (ImageView)getView().findViewById(R.id.musicplayer_albumcover);

        //  register a click event on the album cover to access album selection activity
        albumView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ctx.getApplicationContext(), SelectAlbumActivity.class);
                startActivity(intent);
            }
        });

        ImageView powerOnIcon = (ImageView)getView().findViewById(R.id.musicplayer_poweron);

        //  register a click event on the poweron button
        powerOnIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isPowerOn = true;
                setPowerOnInProgress(true);
                Intent intent = new Intent(ctx.getApplicationContext(), MusicPlayerService.class);
                intent.setAction(POWERON_ACTION);
                ctx.startService(intent);
            }
        });

        ImageView powerOffIcon = (ImageView)getView().findViewById(R.id.musicplayer_poweroff);

        //  register a click event on the poweroff button
        powerOffIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setPowerOffInProgress(true);
                enableControls(false);
                Intent intent = new Intent(ctx.getApplicationContext(), MusicPlayerService.class);
                intent.setAction(POWEROFF_ACTION);
                ctx.startService(intent);
            }
        });

        // Get fresh status from LMS music server
        Intent initIntent = new Intent(ctx.getApplicationContext(), MusicPlayerService.class);
        initIntent.setAction(GET_LMS_INFO_ACTION);
        ctx.startService(initIntent);

        // Initiate loading of info of all albums from LMS server
        setAlbumLoadingInProgress(true);
        Intent intent = new Intent(ctx.getApplicationContext(), MusicPlayerService.class);
        intent.setAction(ALBUM_LIST_REFRESH_ACTION);
        ctx.startService(intent);

        // Initialize list
        Log.i(TAG, "Initializing autoalbumload");
        handler.postDelayed(autoLoadAlbum, AUTOLOADALBUM_INITIALDELAY);
    }

    private void showPowerOnIcon(boolean show) {
        ImageView powerOnIcon = (ImageView)getView().findViewById(R.id.musicplayer_poweron);

        powerOnIcon.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void enableControls(boolean enable) {

        Log.i(TAG, "enableControls:" + Boolean.toString(enable));

        ImageView playIcon = (ImageView)getView().findViewById(R.id.musicplayer_play);

        if (!enable) {
            playIcon.setOnClickListener(null);
            playIcon.setImageResource(R.drawable.music_play_disabled);
        }
        else {
            playIcon.setImageResource(R.drawable.music_play);

            //  register a click event on the play button
            playIcon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setPlayInProgress(true);
                    Intent intent = new Intent(ctx.getApplicationContext(), MusicPlayerService.class);
                    intent.setAction(PLAY_ACTION);
                    ctx.startService(intent);
                }
            });
        }
        ImageView pauseIcon = (ImageView)getView().findViewById(R.id.musicplayer_pause);

        if (!enable) {
            pauseIcon.setOnClickListener(null);
            pauseIcon.setImageResource(R.drawable.music_pause_disabled);
        }
        else {
            pauseIcon.setImageResource(R.drawable.music_pause);
            //  register a click event on the pause button
            pauseIcon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setPauseInProgress(true);
                    Intent intent = new Intent(ctx.getApplicationContext(), MusicPlayerService.class);
                    intent.setAction(PAUSE_ACTION);
                    ctx.startService(intent);
                }
            });
        }
        ImageView nextSongIcon = (ImageView)getView().findViewById(R.id.musicplayer_next);

        if (!enable) {
            nextSongIcon.setOnClickListener(null);
            nextSongIcon.setImageResource(R.drawable.music_next_disabled);
        }
        else {
            nextSongIcon.setImageResource(R.drawable.music_next);

            //  register a click event on the next button
            nextSongIcon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setNextInProgress(true);
                    Intent intent = new Intent(ctx.getApplicationContext(), MusicPlayerService.class);
                    intent.setAction(NEXTSONG_ACTION);
                    ctx.startService(intent);
                }
            });
        }
        ImageView previousSongIcon = (ImageView)getView().findViewById(R.id.musicplayer_previous);

        if (!enable) {
            previousSongIcon.setOnClickListener(null);
            previousSongIcon.setImageResource(R.drawable.music_previous_disabled);
        }
        else {
            previousSongIcon.setImageResource(R.drawable.music_previous);

            //  register a click event on the play button
            previousSongIcon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setPreviousInProgress(true);
                    Intent intent = new Intent(ctx.getApplicationContext(), MusicPlayerService.class);
                    intent.setAction(PREVIOUSSONG_ACTION);
                    ctx.startService(intent);
                }
            });
        }

        ImageView volumeUpIcon = (ImageView)getView().findViewById(R.id.musicplayer_volumeup);

        if (!enable) {
            volumeUpIcon.setOnClickListener(null);
            volumeUpIcon.setImageResource(R.drawable.music_volup_disabled);
        }
        else {
            volumeUpIcon.setImageResource(R.drawable.music_volup);

            //  register a click event on the play button
            volumeUpIcon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setVolUpInProgress(true);
                    Intent intent = new Intent(ctx.getApplicationContext(), MusicPlayerService.class);
                    intent.setAction(VOLUMEUP_ACTION);
                    ctx.startService(intent);
                }
            });
        }

        ImageView volumeDownIcon = (ImageView)getView().findViewById(R.id.musicplayer_volumedown);

        if (!enable) {
            volumeDownIcon.setOnClickListener(null);
            volumeDownIcon.setImageResource(R.drawable.music_voldown_disabled);
        }
        else {
            volumeDownIcon.setImageResource(R.drawable.music_voldown);

            //  register a click event on the play button
            volumeDownIcon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setVolDownInProgress(true);
                    Intent intent = new Intent(ctx.getApplicationContext(), MusicPlayerService.class);
                    intent.setAction(VOLUMEDOWN_ACTION);
                    ctx.startService(intent);
                }
            });
        }
    }

    private void showPowerOffIcon(boolean show) {
        ImageView powerOffIcon = (ImageView)getView().findViewById(R.id.musicplayer_poweroff);

        powerOffIcon.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void showPlayIcon(boolean show) {
        ImageView playIcon = (ImageView)getView().findViewById(R.id.musicplayer_play);

        playIcon.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void showPauseIcon(boolean show) {
        ImageView pauseIcon = (ImageView)getView().findViewById(R.id.musicplayer_pause);

        pauseIcon.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void showVolume(String value) {
        TextView volumeFeedbackText = (TextView)getView().findViewById(R.id.musicplayer_controls_volumefeedback);
        volumeFeedbackText.setText(value+"%");
    }

    private void showNextIcon(boolean show) {
        ImageView nextIcon = (ImageView)getView().findViewById(R.id.musicplayer_next);

        nextIcon.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void showPreviousIcon(boolean show) {
        ImageView previousIcon = (ImageView)getView().findViewById(R.id.musicplayer_previous);

        previousIcon.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void showVolumeUpIcon(boolean show) {
        ImageView volumeUpIcon = (ImageView)getView().findViewById(R.id.musicplayer_volumeup);

        volumeUpIcon.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void showVolumeDownIcon(boolean show) {
        ImageView volumeDownIcon = (ImageView)getView().findViewById(R.id.musicplayer_volumedown);

        volumeDownIcon.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void loadRandomAlbum() {
        Random randomGenerator = new Random(System.currentTimeMillis());
        int index = randomGenerator.nextInt(Globals.musicAlbumItems.size());
        AlbumItem ai = Globals.musicAlbumItems.get(index);
        Globals.selectedAlbum = ai;
        Log.i(TAG, "Loading album randomly: " + ai.getAlbumTitle());

        // Trig service to load album
        Intent loadAlbumIntent = new Intent(ctx, MusicPlayerService.class);
        loadAlbumIntent.setAction(MusicPlayerMain.LOADALBUM_ACTION);
        loadAlbumIntent.putExtra(MusicPlayerMain.EXTRA_LOADALBUM_NAME, ai.getAlbumTitle());
        ctx.startService(loadAlbumIntent);
    }

    private final BroadcastReceiver NetworkViewBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            //Log.i(TAG, "onReceive " + action);
            if (action.equals(GET_LMS_INFO_ACTION_DONE)) {
                // Refresh current volume
                String volFeedback = intent.getStringExtra(EXTRA_VOLUME_FEEDBACK);
                showVolume(volFeedback);
                Log.i(TAG, "processed GET_LMS_INFO_ACTION_DONE, vol=["+volFeedback+"]");
            }
            else if (action.equals(ALBUM_LIST_REFRESH_ACTION_DONE)) {
                Log.i(TAG, "processing ALBUM_LIST_REFRESH_ACTION_DONE.");

                // All available albums were loaded, now pick one randomly
                loadRandomAlbum();
            }
            else if (action.equals(ALBUMS_LOADING_PROGRESS_EVENT)) {
                int progress = intent.getIntExtra(EXTRA_ALBUMLOAD_PROGRESS, 0);

                ProgressBar pb = (ProgressBar)getView().findViewById(R.id.musicplayer_albumloadingProgress);
                pb.setMax(100);
                pb.setProgress(progress);
                Log.i(TAG, "processed ALBUMS_LOADING_PROGRESS_EVENT: "+ Integer.toString(progress));
            }
            else if (action.equals(LOADALBUM_ACTION_DONE)) {

                setAlbumLoadingInProgress(false);

                ImageView albumCover = (ImageView)getView().findViewById(R.id.musicplayer_albumcover);
                albumCover.setImageBitmap(Globals.selectedAlbum.getAlbumCover());

                TextView albumTitle = (TextView)getView().findViewById(R.id.musicplayer_albumtitle);
                albumTitle.setText(Globals.selectedAlbum.getAlbumTitle());

                TextView albumArtist = (TextView)getView().findViewById(R.id.musicplayer_albumartist);
                albumArtist.setText(Globals.selectedAlbum.getAlbumArtist());

                Log.i(TAG, "processed LOADALBUM_ACTION_DONE");
            }
            else if (action.equals(POWERON_ACTION_DONE)) {

                // Sending poweron on the remote raspberry pi trigs loading of the confirmation song "audio_on.wav" in the playlist,
                // so reload the selected album
                setPowerOnInProgress(false);
                Log.i(TAG, "Reloading album at poweron: " + Globals.selectedAlbum.getAlbumTitle());

                // Trig service to load album
                Intent loadAlbumIntent = new Intent(ctx, MusicPlayerService.class);
                loadAlbumIntent.setAction(MusicPlayerMain.LOADALBUM_ACTION);
                loadAlbumIntent.putExtra(MusicPlayerMain.EXTRA_LOADALBUM_NAME, Globals.selectedAlbum.getAlbumTitle());
                ctx.startService(loadAlbumIntent);

                enableControls(true);
                showPowerOffIcon(true);
                showPowerOnIcon(false);
            }
            else if (action.equals(POWEROFF_ACTION_DONE)) {
                setPowerOffInProgress(false);
                showPowerOnIcon(true);
                showPowerOffIcon(false);
                isPowerOn = false;
            }
            else if (action.equals(PLAY_ACTION_DONE)) {
                setPlayInProgress(false);
                showPauseIcon(true);
                showPlayIcon(false);
            }
            else if (action.equals(PAUSE_ACTION_DONE)) {
                setPauseInProgress(false);
                showPlayIcon(true);
                showPauseIcon(false);
            }
            else if (action.equals(NEXTSONG_ACTION_DONE)) {
                setNextInProgress(false);
                // Next song command on LMS happens to play the song automatically,
                // so just in case we were not showing the player as playing yet, do it now
                showPlayIcon(false);
                showPauseIcon(true);

                Log.i(TAG, "processed NEXTSONG_ACTION_DONE");
            }
            else if (action.equals(PREVIOUSSONG_ACTION_DONE)) {
                setPreviousInProgress(false);
                // Next song command on LMS happens to play the song automatically,
                // so just in case we were not showing the player as playing yet, do it now
                showPlayIcon(false);
                showPauseIcon(true);

                Log.i(TAG, "processed PREVIOUSSONG_ACTION_DONE");
            }
            else if (action.equals(SONGPLAYING_EVENT)) {

                TextView albumArtist = (TextView)getView().findViewById(R.id.musicplayer_songtitle);
                String songtitle = intent.getStringExtra(EXTRA_SONGPLAYING_NAME);
                albumArtist.setText(songtitle);

                Log.i(TAG, "processed SONGPLAYING_EVENT");
            }
            else if (action.equals(VOLUMEDOWN_ACTION_DONE)) {
                setVolDownInProgress(false);
                String volFeedback = intent.getStringExtra(EXTRA_VOLUME_FEEDBACK);
                showVolume(volFeedback);
                Log.i(TAG, "processed VOLUMEDOWN_ACTION_DONE, volFeedback=[" + volFeedback + "]");
            }
            else if (action.equals(VOLUMEUP_ACTION_DONE)) {
                setVolUpInProgress(false);
                String volFeedback = intent.getStringExtra(EXTRA_VOLUME_FEEDBACK);
                showVolume(volFeedback);
                Log.i(TAG, "processed VOLUMEUP_ACTION_DONE, volFeedback=[" + volFeedback + "]");
            }
        }
    };

}
