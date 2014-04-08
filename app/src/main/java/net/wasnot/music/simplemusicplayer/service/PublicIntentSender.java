
package net.wasnot.music.simplemusicplayer.service;

import android.content.Context;
import android.content.Intent;

import net.wasnot.music.simplemusicplayer.utli.LogUtil;

/**
 * 再生状態のintentを外部に投げる 三沢さんリクエスト 標準音楽プレーヤーの場合： com.android.music.metachanged
 * は、再生曲が変わった瞬間。 (例えば、次ボタンや前ボタンの押下時。) com.android.music.playstatechanged
 * は、再生状態が変わった瞬間。 (例えば、再生ボタンや停止ボタン押下時。) com.android.music.playbackcomplete
 * は、その曲が最後まで再生された瞬間。
 * 
 * @author akihiroaida
 */
public class PublicIntentSender {
    private static final String TAG = PublicIntentSender.class.getSimpleName();
    // 外部用intent
    private final static String METACHANGED = ".metachanged";
    private final static String PLAYSTATECHANGED = ".playstatechanged";
    private final static String PLAYBACKCOMPLETE = ".playbackcomplete";
    /** 再生曲が変わった瞬間(next of prevボタンで曲が移った等) */
    private final String ACTION_PUBLIC_METACHANGED;
    /** 再生状態が変わった瞬間。(例えば、再生ボタンや停止ボタン押下時。) */
    private final String ACTION_PUBLIC_PLAYSTATECHANGED;
    /** 曲が最後まで再生された瞬間 */
    private final String ACTION_PUBLIC_PLAYBACKCOMPLETE;

    private static final String INTENT_ARTIST = "artist";
    private static final String INTENT_ALBUM = "album";
    private static final String INTENT_TRACK = "track";

    protected enum PublicState {
        /** 変化のない状態 */
        None,
        /** 曲がかわった */
        MetaChange,
        /** 再生状態が変わった */
        StateChange,
        /** 最後まで再生された */
        PlayComplete
    }

    private Context mContext;

    protected PublicIntentSender(Context con) {
        mContext = con;
        ACTION_PUBLIC_METACHANGED = con.getPackageName() + METACHANGED;
        ACTION_PUBLIC_PLAYSTATECHANGED = con.getPackageName() + PLAYSTATECHANGED;
        ACTION_PUBLIC_PLAYBACKCOMPLETE = con.getPackageName() + PLAYBACKCOMPLETE;
    }

    protected void sendIntent(MusicRetriever retriever, PublicState state) {
        if (retriever == null || retriever.getNowItem() == null) {
            return;
        }
        MusicItem playingItem = retriever.getNowItem();
        LogUtil.e(TAG, "sendIntent!" + playingItem);
        if (state == PublicState.None) {
            // State=Noneなら何もしない
            return;
        }

        Intent intent;
        switch (state) {
            case MetaChange:
                intent = new Intent(ACTION_PUBLIC_METACHANGED);
                break;
            case StateChange:
                intent = new Intent(ACTION_PUBLIC_PLAYSTATECHANGED);
                break;
            case PlayComplete:
                intent = new Intent(ACTION_PUBLIC_PLAYBACKCOMPLETE);
                break;
            default:
                intent = null;
                break;
        }
        if (intent != null) {
            intent.putExtra(INTENT_ARTIST, playingItem.getArtist());
            intent.putExtra(INTENT_ALBUM, playingItem.getAlbum());
            intent.putExtra(INTENT_TRACK, playingItem.getTitle());
            mContext.sendBroadcast(intent);
            LogUtil.e(TAG, "sendIntent!" + intent);
        }
    }
}
