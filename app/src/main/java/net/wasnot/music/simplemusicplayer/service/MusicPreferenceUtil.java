
package net.wasnot.music.simplemusicplayer.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

import java.util.List;

public class MusicPreferenceUtil {
    private static final String TAG = MusicPreferenceUtil.class.getSimpleName();
    protected static final String PREFERENCES_FILE = "com.freebit.musicplayer_preferences";
    public static final String KEY_LOOPING = "isLooping";
    public static final String KEY_SHUFFLE = "isShuffle";
    // 再生中の曲のindex
    public static final String KEY_PLAYING = "nowPlayingSong";
    public static final String KEY_PLAYING_PATH = "nowPlayingSongPath";
    // 曲リストの再生順番
    public static final String KEY_PLAY_QUE_ORDER = "playQueOrder";
    // 曲リスト
    public static final String KEY_PLAY_QUE_URI = "playQueUri";
    public static final String KEY_PLAY_QUE_CATEGORY = "playQueCategory";
    public static final String KEY_PLAY_QUE_CATEGORY_ID = "playQueCategoryId";
    // 再生中の曲の時間
    public static final String KEY_CURRENT_POSITION = "currentPosition";
    // 最後に情報更新した時間
    public static final String KEY_LAST_PLAY_DATE = "lastPlayDate";
    // Webから送られてきたレジューム情報
    public static final String KEY_WEB_PLAYING_PATH = "webPlayingPath";
    public static final String KEY_WEB_PLAYING_CATEGORY = "webPlayingCategory";
    public static final String KEY_WEB_PLAYING_CATEGORY_ID = "webPlayingCategoryId";
    public static final String KEY_WEB_PLAYING_CURRENT_POSITION = "webPlayingCurrentPosition";
    // Equalizer情報の保存
    protected static final String KEY_EQUALIZE_SET = "equalize_set";

    protected static void setPrepared(Context con, Uri uri, int category, int categoryId, int index) {
        SharedPreferences settings = con.getSharedPreferences(PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        Editor edit = settings.edit();
        edit.putInt(KEY_PLAYING, index);
        // edit.putString(KEY_PLAYING_PATH, path);
        edit.putString(KEY_PLAY_QUE_URI, uri.toString());
        edit.putInt(KEY_PLAY_QUE_CATEGORY, category);
        edit.putInt(KEY_PLAY_QUE_CATEGORY_ID, categoryId);
        edit.putLong(KEY_LAST_PLAY_DATE, System.currentTimeMillis());
        edit.commit();
    }

    protected static void setSongOrder(Context con, List<Integer> songNumberList) {
        SharedPreferences settings = con.getSharedPreferences(PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        if (songNumberList == null) {
            Editor edit = settings.edit();
            edit.remove(KEY_PLAY_QUE_ORDER);
            edit.commit();
            return;
        }
        String songs = "";
        for (Integer num : songNumberList) {
            songs += num + ",";
        }
        songs = songs.substring(0, songs.length() - 1);
        Editor edit = settings.edit();
        edit.putString(KEY_PLAY_QUE_ORDER, songs);
        edit.commit();
    }

    protected static void setPlaying(Context con, int index, String path, long currentPosition) {
        SharedPreferences settings = con.getSharedPreferences(PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        Editor edit = settings.edit();
        edit.putInt(KEY_PLAYING, index);
        edit.putString(KEY_PLAYING_PATH, path);
        edit.putLong(KEY_CURRENT_POSITION, currentPosition);
        edit.putLong(KEY_LAST_PLAY_DATE, System.currentTimeMillis());
        edit.commit();
    }

    protected static void setLooping(Context con, boolean isLooping) {
        SharedPreferences settings = con.getSharedPreferences(PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        Editor edit = settings.edit();
        edit.putBoolean(KEY_LOOPING, isLooping);
        edit.commit();
    }

    protected static void setShuffle(Context con, boolean isShuffle) {
        SharedPreferences settings = con.getSharedPreferences(PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        Editor edit = settings.edit();
        edit.putBoolean(KEY_SHUFFLE, isShuffle);
        edit.commit();
    }

}
