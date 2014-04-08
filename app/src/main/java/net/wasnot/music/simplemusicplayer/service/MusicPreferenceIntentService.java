
package net.wasnot.music.simplemusicplayer.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;

import net.wasnot.music.simplemusicplayer.PlayerActivity;
import net.wasnot.music.simplemusicplayer.R;
import net.wasnot.music.simplemusicplayer.list.AudioSelector;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;

public class MusicPreferenceIntentService extends IntentService {
    private final static String TAG = MusicPreferenceIntentService.class.getSimpleName();
    public final static String BUNDLE = "bundle";
    public final static String IS_NOTIFY = "is_notify";
    public final static String IS_PLAYING = "is_playing";
    public final static String KEY_CATEGORY = "key_category";
    public final static String KEY_CATEGORY_ID = "key_category_id";
    public final static String KEY_PLAYING_PATH = "key_playing_path";
    public final static String KEY_CURRENT_POSITION = "key_current_position";

    public MusicPreferenceIntentService() {
        super(TAG);
    }

    private Messenger mServiceMessenger;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "onCreate " + TAG);
        mServiceMessenger = new Messenger(new RequestHandler());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mServiceMessenger.getBinder();
    }

    private class RequestHandler extends Handler {
        private Messenger replyMsgr;

        @Override
        public void handleMessage(Message msg) {
            LogUtil.e(TAG, "handle request=" + msg);
            if (msg.replyTo != null) {
                replyMsgr = msg.replyTo;
                switch (msg.what) {
                    case 0:
                        LogUtil.d(TAG, "get preference request");
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    Message rp = Message.obtain(null, 0);
                                    // rp.obj = getPreference();
                                    rp.setData(getPreference());
                                    replyMsgr.send(rp);
                                    // send response.
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        break;
                    case 1:
                        LogUtil.d(TAG, "set preference request");
                        final Bundle params;
                        if (msg.obj instanceof Bundle) {
                            params = (Bundle) msg.obj;
                        } else {
                            params = null;
                        }
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    Message rp = Message.obtain(null, 0);
                                    rp.obj = setPreference(params);
                                    replyMsgr.send(rp);
                                    // send response.
                                    // msg.replyTo.send(Message.obtain(null, 1,
                                    // "file update"));
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean isNotify = intent.getBooleanExtra(IS_NOTIFY, false);
        boolean isPlaying = intent.getBooleanExtra(IS_PLAYING, false);
        Bundle bundle = intent.getBundleExtra(BUNDLE);
        if (isNotify) {
            setPreference(bundle);
            showNotification();
            if (isPlaying)
                startPlayingFromCloud();
        } else if (isPlaying) {
            // startPlayingFromCloud();
            startPlaying(bundle);
        }
    }

    private Bundle getPreference() {
        Bundle bundle = new Bundle();
        SharedPreferences settings = getSharedPreferences(MusicPreferenceUtil.PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        int category = settings.getInt(MusicPreferenceUtil.KEY_PLAY_QUE_CATEGORY, -1);
        int categoryId = settings.getInt(MusicPreferenceUtil.KEY_PLAY_QUE_CATEGORY_ID, -1);
        int playing = settings.getInt(MusicPreferenceUtil.KEY_PLAYING, 0);
        String path = settings.getString(MusicPreferenceUtil.KEY_PLAYING_PATH, null);
        long currentPosition = settings.getLong(MusicPreferenceUtil.KEY_CURRENT_POSITION, 0);
        boolean isShuffle = settings.getBoolean(MusicPreferenceUtil.KEY_SHUFFLE, false);
        String playQueStr = settings.getString(MusicPreferenceUtil.KEY_PLAY_QUE_ORDER, "");
        long lastPlayDate = settings.getLong(MusicPreferenceUtil.KEY_LAST_PLAY_DATE, 0);

        bundle.putInt(MusicPreferenceUtil.KEY_PLAY_QUE_CATEGORY, category);
        bundle.putInt(MusicPreferenceUtil.KEY_PLAY_QUE_CATEGORY_ID, categoryId);
        bundle.putInt(MusicPreferenceUtil.KEY_PLAYING, playing);
        bundle.putString(MusicPreferenceUtil.KEY_PLAYING_PATH, path);
        bundle.putLong(MusicPreferenceUtil.KEY_CURRENT_POSITION, currentPosition);
        bundle.putBoolean(MusicPreferenceUtil.KEY_SHUFFLE, isShuffle);
        bundle.putString(MusicPreferenceUtil.KEY_PLAY_QUE_ORDER, playQueStr);
        bundle.putLong(MusicPreferenceUtil.KEY_LAST_PLAY_DATE, lastPlayDate);
        return bundle;
    }

    private boolean setPreference(Bundle params) {
        if (params == null)
            return false;
        String categoryName = params.getString(KEY_CATEGORY);
        int categoryId = params.getInt(KEY_CATEGORY_ID, -1);
        String playingPath = params.getString(KEY_PLAYING_PATH);
        float currentPosition = params.getFloat(KEY_CURRENT_POSITION, 0);
        LogUtil.d(TAG, "params, category:" + categoryName + ", categoryId:" + categoryId
                + ", playing:" + playingPath + ", currentPosition:" + currentPosition);

        SharedPreferences settings = getSharedPreferences(MusicPreferenceUtil.PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        Editor edit = settings.edit();
        edit.putInt(MusicPreferenceUtil.KEY_WEB_PLAYING_CATEGORY,
                AudioSelector.getDetailCategoryInt(categoryName));
        edit.putInt(MusicPreferenceUtil.KEY_WEB_PLAYING_CATEGORY_ID, categoryId);
        edit.putString(MusicPreferenceUtil.KEY_WEB_PLAYING_PATH, playingPath);
        edit.putFloat(MusicPreferenceUtil.KEY_WEB_PLAYING_CURRENT_POSITION, currentPosition);
        return edit.commit();
    }

    /** Show a notification while this service is running. */
    private Notification showNotification() {
        Context con = getApplicationContext();
        SharedPreferences settings = getSharedPreferences(MusicPreferenceUtil.PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        int category = settings.getInt(MusicPreferenceUtil.KEY_WEB_PLAYING_CATEGORY, 0);
        int categoryId = settings.getInt(MusicPreferenceUtil.KEY_WEB_PLAYING_CATEGORY_ID, 0);
        Uri uri = AudioSelector.getSongUri(category, categoryId);
        float currentPosition = settings.getFloat(
                MusicPreferenceUtil.KEY_WEB_PLAYING_CURRENT_POSITION, 0);
        String filePath = settings.getString(MusicPreferenceUtil.KEY_WEB_PLAYING_PATH, "/none");
        String text = filePath.substring(filePath.lastIndexOf("/"));

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(con, MusicPreferenceIntentService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(AudioSelector.INTENT_PARAM_URI, uri);
        bundle.putInt(AudioSelector.INTENT_PARAM_CATEGORY_ID, categoryId);
        bundle.putInt(AudioSelector.INTENT_PARAM_SONG_INDEX,
                getIndex(uri, category, categoryId, filePath));
        bundle.putInt(AudioSelector.INTENT_PARAM_CATEGORY, category);
        bundle.putFloat(AudioSelector.INTENT_PARAM_SEEK, currentPosition);
        LogUtil.d(TAG, "showNotification:" + bundle);
        intent.putExtra(BUNDLE, bundle);
        intent.putExtra(IS_PLAYING, true);
        // PendingIntent contentIntent = PendingIntent.getService(con,
        // (int) System.currentTimeMillis(), intent,
        // PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent contentIntent = PendingIntent.getService(con, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(con);
        nb.setContentTitle(con.getText(R.string.player_cloud_notify_title));
        nb.setContentText(text);
        nb.setAutoCancel(true);
        nb.setSmallIcon(R.drawable.ic_notify_music);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            nb.setLargeIcon(BitmapFactory.decodeResource(con.getResources(),
                    R.drawable.ic_notify_large_music));
        }
        nb.setTicker(con.getText(R.string.player_cloud_notify_ticker));
        nb.setContentIntent(contentIntent);
        Notification notification = nb.getNotification();

        nm.notify(R.string.cloud_playing_notification_id, notification);
        return notification;
    }

    private void startPlaying(Bundle params) {
        LogUtil.d(TAG, "startPlaying:" + params);
        Context con = getApplicationContext();
        Intent serviceIntent = new Intent(PlayerService.ACTION_FORCE_RETRIEVER, null, con,
                PlayerService.class);
        serviceIntent.putExtra(AudioSelector.INTENT_PARAM_URI,
                params.getParcelable(AudioSelector.INTENT_PARAM_URI));
        serviceIntent.putExtra(AudioSelector.INTENT_PARAM_CATEGORY_ID,
                params.getInt(AudioSelector.INTENT_PARAM_CATEGORY_ID));
        serviceIntent.putExtra(AudioSelector.INTENT_PARAM_SONG_INDEX,
                params.getInt(AudioSelector.INTENT_PARAM_SONG_INDEX));
        serviceIntent.putExtra(AudioSelector.INTENT_PARAM_CATEGORY,
                params.getInt(AudioSelector.INTENT_PARAM_CATEGORY));
        serviceIntent.putExtra(AudioSelector.INTENT_PARAM_IS_SHUFFLE, false);
        serviceIntent.putExtra(AudioSelector.INTENT_PARAM_SEEK,
                params.getFloat(AudioSelector.INTENT_PARAM_SEEK, 0f));
        startService(serviceIntent);
        LogUtil.d(TAG, "startPlaying:" + serviceIntent.getExtras());
        Intent activityIntent = new Intent(con, PlayerActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activityIntent);
    }

    /** cloudからダイレクトに再生してしまう */
    private void startPlayingFromCloud() {
        // 設定から読み取り
        SharedPreferences settings = getSharedPreferences(MusicPreferenceUtil.PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        int category = settings.getInt(MusicPreferenceUtil.KEY_WEB_PLAYING_CATEGORY, 0);
        int categoryId = settings.getInt(MusicPreferenceUtil.KEY_WEB_PLAYING_CATEGORY_ID, 0);
        Uri uri = AudioSelector.getSongUri(category, categoryId);
        float currentPosition = settings.getFloat(
                MusicPreferenceUtil.KEY_WEB_PLAYING_CURRENT_POSITION, 0f);
        String filePath = settings.getString(MusicPreferenceUtil.KEY_WEB_PLAYING_PATH, "/none");
        // String text = filePath.substring(filePath.lastIndexOf("/"));

        Context con = getApplicationContext();
        Intent serviceIntent = new Intent(PlayerService.ACTION_FORCE_RETRIEVER, null, con,
                PlayerService.class);
        serviceIntent.putExtra(AudioSelector.INTENT_PARAM_URI, uri);
        serviceIntent.putExtra(AudioSelector.INTENT_PARAM_CATEGORY_ID, categoryId);
        serviceIntent.putExtra(AudioSelector.INTENT_PARAM_SONG_INDEX,
                getIndex(uri, category, categoryId, filePath));
        serviceIntent.putExtra(AudioSelector.INTENT_PARAM_CATEGORY, category);
        serviceIntent.putExtra(AudioSelector.INTENT_PARAM_SEEK, currentPosition);
        LogUtil.d(TAG, "" + serviceIntent.getExtras());
        startService(serviceIntent);
        Intent activityIntent = new Intent(con, PlayerActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activityIntent);
    }

    // TODO これ用のclassを用意すべきかも。。。
    /** MusicRetrieverやMusicListFragmentと同じ取り方 */
    private int getIndex(Uri uri, int category, int categoryId, String filePath) {
        int result = -1;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
        String categorySelection = AudioSelector.getSelection(category, categoryId);
        if (categorySelection != null) {
            selection += " AND " + categorySelection;
        }
        if (AudioSelector.isCategory(category))
            selection = null;

        String sortOrder = AudioSelector.getSortOrder(category);
        Cursor c = getContentResolver().query(uri, null, selection, null, sortOrder);
        LogUtil.i(TAG, "Query finished. " + (c == null ? "Returned NULL." : "Returned a cursor."));
        if (c == null) {
            // Query failed...
            LogUtil.e(TAG, "Failed to retrieve music: cursor is null :-(");
            return -1;
        }
        if (c.moveToFirst()) {
            int i = 0;
            do {
                if (c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA)).equals(filePath)) {
                    result = i;
                    break;
                }
                i++;
            } while (c.moveToNext());
        }
        c.close();
        LogUtil.d(TAG, "index is :" + result);
        return result;
    }
}
