
package net.wasnot.music.simplemusicplayer.service;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;

import com.example.android.musicplayer.MediaButtonHelper;
import com.example.android.musicplayer.RemoteControlClientCompat;
import com.example.android.musicplayer.RemoteControlHelper;

import net.wasnot.music.simplemusicplayer.utli.LogUtil;

/**
 * remote control、つまりロック画面でaudioのコントロールを可能にする設定 MediaPlayerのsetWakeModeが必要
 * 
 * @author akihiroaida
 */
public class RemoteControlClientManager {
    private final String TAG = this.getClass().getSimpleName();
    // The component name of MusicIntentReceiver, for use with media button and
    // remote control APIs
    private ComponentName mMediaButtonReceiverComponent;
    // Dummy album art we will pass to the remote control (if the APIs are
    // available).
    // private Bitmap mDummyAlbumArt;
    private RemoteControlClientCompat mRemoteControlClientCompat = null;
    private Context mCon;

    public RemoteControlClientManager(Context con) {
        // mDummyAlbumArt = BitmapFactory.decodeResource(con.getResources(),
        // R.drawable.music_empty);
        mMediaButtonReceiverComponent = new ComponentName(con, MusicIntentReceiver.class);
        mCon = con;
    }

    /** RemoteControlを作る。一応API<=14でも動くはず */
    public void setRemoteControlClient(MusicItem playingItem, AudioManager audioManager) {
        LogUtil.d(TAG, "setRemoteControlClient");
        // Use the media button APIs (if available) to register ourselves
        // for media button events
        MediaButtonHelper.registerMediaButtonEventReceiverCompat(audioManager,
                mMediaButtonReceiverComponent);
        // Use the remote control APIs (if available) to set the playback
        // state

        if (mRemoteControlClientCompat == null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.setComponent(mMediaButtonReceiverComponent);
            mRemoteControlClientCompat = new RemoteControlClientCompat(PendingIntent.getBroadcast(
                    mCon /* context */, 0 /*
                                           * requestCode , ignored
                                           */, intent /* intent */, 0 /* flags */));
            RemoteControlHelper.registerRemoteControlClient(audioManager,
                    mRemoteControlClientCompat);
        }

        mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);

        mRemoteControlClientCompat.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_NEXT | RemoteControlClient.FLAG_KEY_MEDIA_STOP
                | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);

        // Update the remote controls
        mRemoteControlClientCompat
                .editMetadata(true)
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, playingItem.getArtist())
                .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, playingItem.getAlbum())
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, playingItem.getTitle())
                .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, playingItem.getDuration())
                // TODO: fetch real item artwork
                .putBitmap(RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK,
                        BitmapFactory.decodeFile(playingItem.getAlbumArt())).apply();
        // .putBitmap(RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK,
        // mDummyAlbumArt).apply();

    }

    /** RemoteControlにplayの状況を伝える。 */
    public void setPlaybackState(int state) {
        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat.setPlaybackState(state);
        }
    }
}
