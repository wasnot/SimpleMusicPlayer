
package net.wasnot.music.simplemusicplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.widget.RemoteViews;

import net.wasnot.music.simplemusicplayer.PlayerActivity;
import net.wasnot.music.simplemusicplayer.R;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;

public class NotificationRemoteManager {
    private final String TAG = this.getClass().getSimpleName();

    // The ID we use for the notification (the onscreen alert that appears at
    // the notification
    // area at the top of the screen as an icon -- and as text as well if the
    // user expands the
    // notification area).
    private final int NOTIFICATION_ID = 1;
    private Context mCon;
    private NotificationManager mNotificationManager;
    private Notification mNotification = null;

    public NotificationRemoteManager(Context con) {
        mCon = con;
        mNotificationManager = (NotificationManager) mCon
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // mNotification = makeRemoteView();

    }

    /** CustomViewのNotificationをつくる,API>ICSじゃなきゃだめ */
    private Notification makeRemoteView() {
        LogUtil.d(TAG, "makeRemoteView!");
        Intent intent;

        intent = new Intent(mCon, PlayerActivity.class);
        PendingIntent pi = PendingIntent.getActivity(mCon, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews views = new RemoteViews(mCon.getPackageName(),
                R.layout.notification_remote_control);
        mNotification = new Notification();
        mNotification.icon = R.drawable.play_btn;
        mNotification.contentView = views;
        mNotification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        mNotification.contentIntent = pi;

        intent = new Intent(PlayerService.ACTION_REWIND, null, mCon, PlayerService.class);
        PendingIntent piRewind = PendingIntent.getService(mCon, R.id.rewind, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.rewind, piRewind);

        intent = new Intent(PlayerService.ACTION_TOGGLE_PLAYBACK, null, mCon, PlayerService.class);
        PendingIntent piPlayPause = PendingIntent.getService(mCon, R.id.playpause, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.playpause, piPlayPause);

        intent = new Intent(PlayerService.ACTION_SKIP, null, mCon, PlayerService.class);
        PendingIntent piSkip = PendingIntent.getService(mCon, R.id.skip, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.skip, piSkip);

//        intent = new Intent(PlayerService.ACTION_STOP, null, mCon, PlayerService.class);
//        PendingIntent piStop = PendingIntent.getService(mCon, R.id.stop, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        views.setOnClickPendingIntent(R.id.stop, piStop);
        return mNotification;
    }

    /**
     * Configures service as a foreground service. A foreground service is a
     * service that's doing something the user is actively aware of (such as
     * playing music), and must appear to the user as a notification. That's why
     * we create the notification here.
     */
    public void setUpAsForeground(String text, Service service) {
        LogUtil.d(TAG, "makeRemoteView!");
        PendingIntent pi = PendingIntent.getActivity(mCon, 0,
                new Intent(mCon, PlayerActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification = new Notification();
        mNotification.tickerText = text;
        mNotification.icon = R.drawable.ic_notify_music;
        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        mNotification.setLatestEventInfo(mCon, mCon.getText(R.string.player_local_notify_ticker),
                text, pi);
        service.startForeground(NOTIFICATION_ID, mNotification);
    }

    // TODO 引数減らしたい
    /** Updates the notification. notification controlerを使っていたとき API>ICSじゃなきゃだめ */
    public void updateNotification(MediaPlayer player, PlayerService.State state, MusicRetriever retriever) {
        LogUtil.d(TAG, "update Custom Notification");
        if (true)
            return;

        boolean playing = (state == PlayerService.State.Playing);

        mNotification.icon = playing ? R.drawable.play_btn : R.drawable.pause_btn;
        int playPauseRes = playing ? R.drawable.pause_btn : R.drawable.play_btn;
        mNotification.contentView.setImageViewResource(R.id.playpause, playPauseRes);

        MusicItem playingItem = retriever.getNowItem();
        mNotification.contentView.setTextViewText(R.id.artist, playingItem.getArtist());
        mNotification.contentView.setTextViewText(R.id.album, playingItem.getAlbum());
        mNotification.contentView.setTextViewText(R.id.title, playingItem.getTitle());
        long current;
        if (state == PlayerService.State.Stopped) {
            current = 0;
        } else {
            current = player.getCurrentPosition();
        }
        mNotification.contentView.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime()
                - current, null, playing);

        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /** Updates the notification. 普通のnotification */
    public void updateNotification(String text) {
        LogUtil.d(TAG, "update standard Notification");
        PendingIntent pi = PendingIntent.getActivity(mCon, 0,
                new Intent(mCon, PlayerActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification.setLatestEventInfo(mCon, mCon.getText(R.string.player_local_notify_ticker),
                text, pi);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /** キャンセル */
    public void cancel() {
        LogUtil.d(TAG, "notify cancel!");
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

}
