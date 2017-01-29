package com.gbbtbb.homehub;

import android.graphics.Bitmap;

import com.gbbtbb.homehub.agendaviewer.AgendaItem;
import com.gbbtbb.homehub.agendaviewer.WeatherItem;
import com.gbbtbb.homehub.musicplayer.AlbumItem;
import com.gbbtbb.homehub.musicplayer.SongItem;
import com.gbbtbb.homehub.todolist.TodoListRowItem;

import java.util.ArrayList;
import java.util.HashMap;

public class Globals {
        public static Bitmap photoFrameBitmap;

        public static ArrayList<String> shoppingListItems;

        public static ArrayList<TodoListRowItem> todoListItems;

        public static ArrayList<AlbumItem> musicAlbumItems;

        public static AlbumItem selectedAlbum;

        public static ArrayList<SongItem> musicAlbumSongItems;

        public static ArrayList<WeatherItem> weatherItems;

        public static ArrayList<AgendaItem> agendaItems;

        public static Bitmap graphBitmap;

        public static HashMap<String, Float> graphLatestValues;
}
