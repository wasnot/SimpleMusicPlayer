
package net.wasnot.music.simplemusicplayer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.android.musicplayer.AudioFocusHelper;
import com.example.android.musicplayer.MusicFocusable;

import net.wasnot.music.simplemusicplayer.PlayerActivity;
import net.wasnot.music.simplemusicplayer.R;
import net.wasnot.music.simplemusicplayer.list.AudioSelector;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;

import java.io.IOException;
import java.util.Arrays;


/**
 * ほとんど com.example.android.musicplayer.MusicServiceそのまま 　＠IT記事を参考にした
 * 
 * @author aidaakihiro
 */
public class PlayerService extends Service implements OnCompletionListener, OnPreparedListener,
        OnSeekCompleteListener, OnErrorListener, MusicFocusable,
        PrepareMusicRetrieverTask.MusicRetrieverPreparedListener {

    // The tag we put on debug messages
    private final static String TAG = PlayerService.class.getSimpleName();

    // These are the Intent actions that we are prepared to handle. Notice that
    // the fact these
    // constants exist in our class is a mere convenience: what really defines
    // the actions our
    // service can handle are the <action> tags in the <intent-filters> tag for
    // our service in
    // AndroidManifest.xml.
    // private final static String PACKAGE =
    // PlayerService.class.getPackage().getName();
    public static final String ACTION_TOGGLE_PLAYBACK = "com.freebit.everysync.musicplayer.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.freebit.everysync.musicplayer.action.PLAY";
    public static final String ACTION_PAUSE = "com.freebit.everysync.musicplayer.action.PAUSE";
    public static final String ACTION_STOP = "com.freebit.everysync.musicplayer.action.STOP";
    public static final String ACTION_SKIP = "com.freebit.everysync.musicplayer.action.SKIP";
    public static final String ACTION_REWIND = "com.freebit.everysync.musicplayer.action.REWIND";
    public static final String ACTION_URL = "com.freebit.everysync.musicplayer.action.URL";
    public static final String ACTION_SEEK = "com.freebit.everysync.musicplayer.action.SEEK";
    public static final String ACTION_PREVIOUS = "com.freebit.everysync.musicplayer.action.PREVIOUS";

    // Activityとの連携用
    public static final String ACTION_STATE_CHANGED = "com.freebit.everysync.musicplayer.action.ACTION_STATE_CHANGED";
    public static final String ACTION_REQUEST_STATE = "com.freebit.everysync.musicplayer.action.ACTION_REQUEST_STATE";
    public static final String ACTION_REQUEST_STATE_FROM_OTHER_PROCESS = "com.freebit.everysync.musicplayer.action.ACTION_REQUEST_STATE_FROM_OTHER_PROCESS";

    // ListからQueをつくる
    public static final String ACTION_FORCE_RETRIEVER = "com.freebit.everysync.musicplayer.action.ACTION_FORCE_RETRIEVER";

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;

    // our media player
    private MediaPlayer mPlayer = null;

    // our AudioFocusHelper object, if it's available (it's available on SDK
    // level >= 8)
    // If not available, this will be null. Always check for null before using!
    private AudioFocusHelper mAudioFocusHelper = null;

    // indicates the state our service:
    public enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped, // media player is stopped and not prepared to play
        Preparing, // media player is preparing...
        Playing, // playback active (media player ready!). (but the media player
                 // may actually be
                 // paused in this state if we don't have audio focus. But we
                 // stay in this state
                 // so that we know we have to resume playback once we get focus
                 // back)
        Paused, // playback paused (media player ready!)
        Seeking
    };

    private State mState = State.Retrieving;

    // if in Retrieving mode, this flag indicates whether we should start
    // playing immediately
    // when we are ready or not.
    private boolean mStartPlayingAfterRetrieve = false;

    enum PauseReason {
        UserRequest, // paused by user request
        FocusLoss, // paused because of audio focus loss
    };

    // why did we pause? (only relevant if mState == State.Paused)
    private PauseReason mPauseReason = PauseReason.UserRequest;

    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck, // we don't have audio focus, and can't duck
        NoFocusCanDuck, // we don't have focus, but can play at a low volume
                        // ("ducking")
        Focused // we have full audio focus
    }

    private AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    // title of the song we are currently playing
    private String mSongTitle = "";

    // Our instance of our MusicRetriever, which handles scanning for media and
    // providing titles and URIs as we need.
    private MusicRetriever mRetriever;
    private boolean isLooping = false;
    private boolean isShuffle = false;
    private PublicIntentSender mPublicIntentSender;

    // our RemoteControlClient object, which will use remote control APIs
    // available in
    // SDK level >= 14, if they're available.
    private RemoteControlClientManager mRemoteControlClientManager;
    private NotificationRemoteManager mNotificationRemoteManager;

    private AudioManager mAudioManager;

    private Boolean mIsOnlyPrepare;
    private float mSeekAfterPrepare = 0f;
    private long mRelaxTime = System.currentTimeMillis();

//    private AppSensingUtil mSensing;

    private Thread mSelfStopThread = new Thread() {
        public void run() {
            while (true) {
                // 停止後 10 分再生がなかったらサービスを止める
                boolean needSleep = false;
                if (mState == PlayerService.State.Preparing
                        || mState == PlayerService.State.Playing
                        || mState == PlayerService.State.Paused) {
                    needSleep = true;
                } else if (mRelaxTime + 10 * 1000 * 60 > System.currentTimeMillis()) {
                    needSleep = true;
                }
                if (!needSleep) {
                    break;
                }
                try {
                    Thread.sleep(1 * 1000 * 60);
                    // 停止中でない、または 10 分経過してない場合は 1 分休む
                } catch (InterruptedException e) {
                }
            }
            PlayerService.this.stopSelf();
        }
    };

    /**
     * Makes sure the media player exists and has been reset. This will create
     * the media player if needed, or reset the existing media player if one
     * already exists.
     */
    private void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            LogUtil.d(TAG, "renew");
            mPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while
            // playing. If we don't do
            // that, the CPU might go to sleep while the song is playing,
            // causing playback to stop.
            //
            // Remember that to use this, we have to declare the
            // android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing,
            // and when it's done
            // playing:
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.setOnSeekCompleteListener(this);
            EqualizerUtil.setEqualizer(mPlayer);
        } else {
            LogUtil.d(TAG, "reset");
            mPlayer.reset();
        }
    }

    @Override
    public void onCreate() {
        LogUtil.i(TAG, "debug: Creating service");

        mNotificationRemoteManager = new NotificationRemoteManager(this.getApplicationContext());
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mRemoteControlClientManager = new RemoteControlClientManager(this.getApplicationContext());
//        mSensing = new AppSensingUtil(this.getApplicationContext(), AppUtil.getMailAddr(this
//                .getApplicationContext()));
        mPublicIntentSender = new PublicIntentSender(getApplicationContext());

        // create the Audio Focus Helper, if the Audio Focus feature is
        // available (SDK 8 or above)
        if (android.os.Build.VERSION.SDK_INT >= 8)
            mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        else
            mAudioFocus = AudioFocus.Focused; // no focus feature, so we always
                                              // "have" audio focus

        // 設定ファイルからstatusをひろってくる。
        SharedPreferences settings = this.getSharedPreferences(
                MusicPreferenceUtil.PREFERENCES_FILE, Context.MODE_PRIVATE);
        isLooping = settings.getBoolean(MusicPreferenceUtil.KEY_LOOPING, false);
        isShuffle = settings.getBoolean(MusicPreferenceUtil.KEY_SHUFFLE, false);

        mSelfStopThread.start();
    }

    /**
     * Called when we receive an Intent. When we receive an intent sent to us
     * via startService(), this is the method that gets called. So here we react
     * appropriately depending on the Intent's action, which specifies what is
     * being requested of us.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(TAG, "onStartCommand");
        String action = intent.getAction();

        // リストをリクエスト
        if (action.equals(ACTION_FORCE_RETRIEVER)) {
            Uri uri = intent.getParcelableExtra(AudioSelector.INTENT_PARAM_URI);
            int categoryId = intent.getIntExtra(AudioSelector.INTENT_PARAM_CATEGORY_ID, -1);
            int index = intent.getIntExtra(AudioSelector.INTENT_PARAM_SONG_INDEX, 0);
            int category = intent.getIntExtra(AudioSelector.INTENT_PARAM_CATEGORY, 0);
            isShuffle = intent.getBooleanExtra(AudioSelector.INTENT_PARAM_IS_SHUFFLE, isShuffle);
            mSeekAfterPrepare = intent.getFloatExtra(AudioSelector.INTENT_PARAM_SEEK, 0f);
            MusicPreferenceUtil.setPrepared(getApplicationContext(), uri, category, categoryId,
                    index);
//            if (mSensing != null)
//                mSensing.trackMusicNext("request_song");
            // if (mState == State.Playing)
            mStartPlayingAfterRetrieve = true;
            // 初期ならリストをとってくる、同じリストじゃなければ作り直す
            // TODO とりあえず今は毎回更新します
            if (mRetriever == null || !mRetriever.isCursorEquals(uri, categoryId) || true) {
                mRetriever = new MusicRetriever(getContentResolver(), uri, categoryId, index,
                        category, isShuffle);
                (new PrepareMusicRetrieverTask(mRetriever, this)).execute();
            }
            // 同じリストならインデックスをかえて準備完了
            else {
                // もしshuffleなら一回シャッフルし直す。そうじゃないとshuffleした後のindexの操作になっちゃう。
                // というか、曲選択し直したらシャッフルし直さないといけないよね。
                if (isShuffle)
                    mRetriever.setShuffle(false);
                boolean didSetIndex = mRetriever.setIndex(index);
                LogUtil.e(TAG, "action retriever request," + mState + ", " + didSetIndex);
                mRetriever.setShuffle(isShuffle);
                if (mState == State.Playing) {
                    processStopRequest();
                    processPlayRequest();
                } else {
                    this.onMusicRetrieverPrepared();
                }
            }
        }

        // Create the retriever and start an asynchronous task that will prepare
        // it.
        if (mRetriever == null) {
            // mRetriever = new MusicRetriever(getContentResolver(), isShuffle);
            // ただ初期化するときも設定に保存しているものを確認する。
            SharedPreferences settings = getSharedPreferences(MusicPreferenceUtil.PREFERENCES_FILE,
                    Context.MODE_PRIVATE);
            Uri uri = Uri.parse(settings.getString(MusicPreferenceUtil.KEY_PLAY_QUE_URI,
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()));
            int categoryId = settings.getInt(MusicPreferenceUtil.KEY_PLAY_QUE_CATEGORY_ID, -1);
            int index = settings.getInt(MusicPreferenceUtil.KEY_PLAYING, 0);
            int category = settings.getInt(MusicPreferenceUtil.KEY_PLAY_QUE_CATEGORY, 0);
            mRetriever = new MusicRetriever(getContentResolver(), uri, categoryId, index, category,
                    isShuffle);
            (new PrepareMusicRetrieverTask(mRetriever, this)).execute();
        }

        if (action.equals(ACTION_TOGGLE_PLAYBACK)) {
            processTogglePlaybackRequest();
        } else if (action.equals(ACTION_PLAY)) {
//            if (mSensing != null)
//                mSensing.trackMusicNext("new_song");
            processPlayRequest();
        } else if (action.equals(ACTION_PAUSE)) {
            processPauseRequest();
        } else if (action.equals(ACTION_SKIP)) {
            processSkipRequest();
        } else if (action.equals(ACTION_STOP)) {
            processStopRequest();
            if (intent.getBooleanExtra("cancel", false)) {
                mNotificationRemoteManager.cancel();
            }
        } else if (action.equals(ACTION_REWIND)) {
            processRewindRequest();
        } else if (action.equals(ACTION_PREVIOUS)) {
            processPreviousRequest();
        } else if (action.equals(ACTION_URL)) {
            processAddRequest(intent);
        } else if (action.equals(ACTION_REQUEST_STATE)) {
            sendPlayerState();
        } else if (action.equals(ACTION_SEEK)) {
            float seek = intent.getFloatExtra(AudioSelector.INTENT_PARAM_SEEK, 0F);
            LogUtil.d(TAG, "seek:" + seek);
            if (seek <= 1F) {
                processSeekRequest(seek);
            } else {
                if (mListener != null)
                    mListener.onSeekCompleted(false);
                if (mStub != null)
                    mStub.onSeekCompleted(false);
            }
        }

        return START_NOT_STICKY;
        // Means we started the service, but don't want it to restart in case
        // it's killed.
    }

    /** ヘッドホンなどのplaybtnでのtoggle */
    private void processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest();
        } else {
            processPauseRequest();
        }
    }

    /** playbtnを押した */
    private void processPlayRequest() {
        if (mState == State.Retrieving) {
            // If we are still retrieving media, just set the flag to start
            // playing when we're ready
            mStartPlayingAfterRetrieve = true;
            return;
        }
        tryToGetAudioFocus();
        // actually play the song

        if (mState == State.Stopped) {
            // If we're stopped, just go ahead to the next song and start
            // playing
            playNextSong(false, 0);
        } else if (mState == State.Paused) {
            // If we're paused, just continue playback and restore the
            // 'foreground service' state.
            mState = State.Playing;
            mNotificationRemoteManager.setUpAsForeground(mSongTitle + " (playing)", this);
            configAndStartMediaPlayer();
        }
        // Tell any remote controls that our playback state is 'playing'.
        mRemoteControlClientManager.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
    }

    /** pausebtnを押した */
    private void processPauseRequest() {
        LogUtil.d(TAG, "processPauseRequest:" + mState);
        if (mState == State.Retrieving) {
            // If we are still retrieving media, clear the flag that indicates
            // we should start
            // playing when we're ready
            mStartPlayingAfterRetrieve = false;
            return;
        }
        if (mState == State.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.Paused;
            mPlayer.pause();
            relaxResources(false);
            // while paused, we always retain the MediaPlayer do not give up
            // audio focus
            mNotificationRemoteManager.updateNotification(mPlayer, mState, mRetriever);
            mPublicIntentSender.sendIntent(mRetriever, PublicIntentSender.PublicState.StateChange);
            sendPlayerState();
        }
        // Tell any remote controls that our playback state is 'paused'.
        mRemoteControlClientManager.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
    }

    /** 戻るbtnを押した */
    private void processRewindRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            mPublicIntentSender.sendIntent(mRetriever, PublicIntentSender.PublicState.MetaChange);
            // 開始して1秒以内ならprevious,それ以外はrewind
            if (mPlayer.getCurrentPosition() < 1000) {
                if (mState == State.Playing)
                    playNextSong(false, -1);
                else
                    playNextSong(true, -1);
            } else
                mPlayer.seekTo(0);
            mNotificationRemoteManager.updateNotification(mPlayer, mState, mRetriever);
            sendPlayerState();
        }
        if (mState == State.Stopped) {
            playNextSong(true, -1);
        }
    }

    /** 前の曲に戻る */
    private void processPreviousRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            mPublicIntentSender.sendIntent(mRetriever, PublicIntentSender.PublicState.MetaChange);
            if (mState == State.Playing)
                playNextSong(false, -1);
            else
                playNextSong(true, -1);
            mNotificationRemoteManager.updateNotification(mPlayer, mState, mRetriever);
            sendPlayerState();
        }
        if (mState == State.Stopped) {
            playNextSong(true, -1);
        }
    }

    /** 次へbtnを押した */
    private void processSkipRequest() {
        if (mState == State.Playing) {
            LogUtil.d(TAG, "next song play");
            mPublicIntentSender.sendIntent(mRetriever, PublicIntentSender.PublicState.MetaChange);
            tryToGetAudioFocus();
            // 再生中なら続けて再生
            playNextSong(false, 1);
        } else if (mState == State.Stopped || mState == State.Paused) {
            LogUtil.d(TAG, "next song stop");
            // 　中止中ならめくるだけ
            playNextSong(true, 1);
        }
    }

    /** stopbtnを押した */
    private void processStopRequest() {
        processStopRequest(false);
    }

    /** 強制停止リクエスト */
    private void processStopRequest(boolean force) {
        if (mState == State.Playing || mState == State.Paused || force) {
            mState = State.Stopped;

            // let go of all resources...
            relaxResources(true);
            giveUpAudioFocus();

            // Tell any remote controls that our playback state is 'paused'.
            mRemoteControlClientManager.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
            mPublicIntentSender.sendIntent(mRetriever, PublicIntentSender.PublicState.StateChange);
            mNotificationRemoteManager.updateNotification(mPlayer, mState, mRetriever);
            sendPlayerState();
            // service is no longer necessary. Will be started again if needed.
            // stopSelf();
        }
    }

    /** Seekした seekは曲の割合(0~1) */
    private void processSeekRequest(float seek) {
        LogUtil.d(TAG, "seeking:" + seek);
        if (mState == State.Retrieving) {
            // If we are still retrieving media, just set the flag to start
            // playing when we're ready
            mStartPlayingAfterRetrieve = true;
            if (mListener != null)
                mListener.onSeekCompleted(false);
            if (mStub != null)
                mStub.onSeekCompleted(false);
            return;
        }
        // stateに関わらずシークしてみる。
        // if (mState == State.Playing || mState == State.Paused) {}
        if (mPlayer != null) {
            State nowState = mState;
            try {
                mState = State.Seeking;
                LogUtil.e(TAG, "seeking:" + (seek * (float) mPlayer.getDuration()));
                mPlayer.seekTo((int) (seek * (float) mPlayer.getDuration()));
                mNotificationRemoteManager.updateNotification(mPlayer, mState, mRetriever);
                sendPlayerState();
            } catch (IllegalStateException e) {
                mState = nowState;
                LogUtil.e(TAG, "cannot seek:" + seek);
                if (mListener != null)
                    mListener.onSeekCompleted(false);
                if (mStub != null)
                    mStub.onSeekCompleted(false);
            }
        }
    }

    /**
     * Releases resources used by the service for playback. This includes the
     * "foreground service" status and notification, the wake locks and possibly
     * the MediaPlayer.
     * 
     * @param releaseMediaPlayer Indicates whether the Media Player should also
     *            be released or not
     */
    private void relaxResources(boolean releaseMediaPlayer) {
        // stop being a foreground service
        stopForeground(true);

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
        mRelaxTime = System.currentTimeMillis();
    }

    private void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and
     * starts/restarts it. This method starts/restarts the MediaPlayer
     * respecting the current audio focus state. So if we have focus, it will
     * play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is
     * allowed by the current focus settings. This method assumes mPlayer !=
     * null, so if you are calling it, you have to do so from a context where
     * you are sure this is the case.
     */
    private void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            // If we don't have audio focus and can't duck, we have to pause,
            // even if mState
            // is State.Playing. But we stay in the Playing state so that we
            // know we have to resume
            // playback once we get the focus back.
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                mState = State.Paused;
                mNotificationRemoteManager.updateNotification(mPlayer, mState, mRetriever);
                mPublicIntentSender.sendIntent(mRetriever, PublicIntentSender.PublicState.StateChange);
                mRemoteControlClientManager.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                sendPlayerState();
            }
            return;
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
        // we'll be relatively quiet
        else
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud

        if (!mPlayer.isPlaying()) {
            mPlayer.start();
            mNotificationRemoteManager.updateNotification(mPlayer, mState, mRetriever);
            mPublicIntentSender.sendIntent(mRetriever, PublicIntentSender.PublicState.StateChange);
            mRemoteControlClientManager.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            sendPlayerState();
        }
    }

    /** 再整リスt追加リクエスト、使う？ */
    private void processAddRequest(Intent intent) {
        // user wants to play a song directly by URL or path. The URL or path
        // comes in the "data"
        // part of the Intent. This Intent is sent by {@link MainActivity} after
        // the user
        // specifies the URL/path via an alert box.
        if (mState == State.Retrieving) {
            mStartPlayingAfterRetrieve = true;
        } else if (mState == State.Playing || mState == State.Paused || mState == State.Stopped) {
            LogUtil.i(TAG, "Playing from URL/path: " + intent.getData().toString());
            tryToGetAudioFocus();
            // playNextSong(intent.getData().toString());
        }
    }

    private void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }

    /**
     * Starts playing the next song. If manualUrl is null, the next song will be
     * randomly selected from our Media Retriever (that is, it will be a random
     * song in the user's device). If manualUrl is non-null, then it specifies
     * the URL or path to the song that will be played next.
     */
    private void playNextSong(boolean isOnlyPrepare, int next) {
        mIsOnlyPrepare = isOnlyPrepare;
        mState = State.Paused;// Stopped
        relaxResources(false);
        // release everything except MediaPlayer
        try {
            // 次のItemをとってくる
            MusicItem playingItem;
            if (next > 0) {
                playingItem = mRetriever.getNextItem();
                // 最初に戻ったらstop?pause?準備だけする？
                if (mRetriever.getIndex() == 0 && !isLooping) {
                    // processStopRequest(true);
                    // processPauseRequest();
                    // return;
                    mIsOnlyPrepare = true;
                }
            } else if (next < 0) {
                playingItem = mRetriever.getPrevItem();
            } else {
                playingItem = mRetriever.getNowItem();
            }
            if (playingItem == null) {
                // String msg;
                // msg =
                // "No available music to play. Place some music on your external storage "
                // + "device (e.g. your SD card) and try again.";
                // msg = "再生できるファイルがありません";
                Toast.makeText(this, R.string.player_service_toast_error_no_music,
                        Toast.LENGTH_LONG).show();
                processStopRequest(true); // stop everything!
                return;
            }
            LogUtil.d(TAG, "path:" + playingItem.getURI());
            // set the source of the media player a a content URI
            createMediaPlayerIfNeeded();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(getApplicationContext(), playingItem.getURI());

            mSongTitle = playingItem.getTitle();
            mState = State.Preparing;

            // notificationを作るor更新
            mNotificationRemoteManager.setUpAsForeground(mSongTitle + " (loading)", this);
            // remote control を作るor更新
            mRemoteControlClientManager.setRemoteControlClient(playingItem, mAudioManager);

            // starts preparing the media player in the background. When it's
            // done, it will call
            // our OnPreparedListener (that is, the onPrepared() method on this
            // class, since we set
            // the listener to 'this').

            // Until the media player is prepared, we *cannot* call start() on
            // it!
            mPlayer.prepareAsync();
        } catch (IOException ex) {
            LogUtil.e("MusicService", "IOException playing next song: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** Called when media player is done playing current song. */
    @Override
    public void onCompletion(MediaPlayer player) {
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        mPublicIntentSender.sendIntent(mRetriever, PublicIntentSender.PublicState.PlayComplete);
        playNextSong(false, 1);
    }

    /** Called when media player is done preparing. */
    @Override
    public void onPrepared(MediaPlayer player) {
        // The media player is done preparing. That means we can start playing!
        if (mIsOnlyPrepare) {
            mState = State.Paused;// Stopped
            mRemoteControlClientManager.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        } else {
            mState = State.Playing;
            mRemoteControlClientManager.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
        mNotificationRemoteManager.updateNotification(mSongTitle + " (playing)");
        sendPlayerState();
        if (!mIsOnlyPrepare) {
            // クラウドからseekされたときの操作。
            if (mSeekAfterPrepare > 0f && player != null) {
                State nowState = mState;
                try {
                    mState = State.Seeking;
                    LogUtil.e(TAG, "seeking:" + mSeekAfterPrepare);
                    mPlayer.seekTo((int) mSeekAfterPrepare);
                    mNotificationRemoteManager.updateNotification(mPlayer, mState, mRetriever);
                    sendPlayerState();
                } catch (IllegalStateException e) {
                    mState = nowState;
                    LogUtil.e(TAG, "cannot seek:" + mSeekAfterPrepare);
                    if (mListener != null)
                        mListener.onSeekCompleted(false);
                    if (mStub != null)
                        mStub.onSeekCompleted(false);
                }
                mSeekAfterPrepare = 0f;
            }
//            if (mSensing != null)
//                mSensing.trackMusicNext(mSongTitle);
            configAndStartMediaPlayer();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer player) {
        LogUtil.e(TAG, "onSeekComplete, " + (player.isPlaying() ? "playing" : "paused"));
        if (player.isPlaying()) {
            mState = State.Playing;
        } else {
            mState = State.Paused;
        }
        if (mListener != null)
            mListener.onSeekCompleted(true);
        if (mStub != null)
            mStub.onSeekCompleted(true);
    }

    /**
     * Called when there's an error playing media. When this happens, the media
     * player goes to the Error state. We warn the user about the error and
     * reset the media player.
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // String msg = "再生エラーが発生しました";// "Media player error! Resetting.";
        Toast.makeText(getApplicationContext(), R.string.player_service_toast_error_player_error,
                Toast.LENGTH_SHORT).show();
        LogUtil.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

        // 壊れててもとりあえず再生し続ける
        // TODO 二つ続けて壊れてるのがあるとうまく動かない。。
        if (true) {
            mState = State.Playing;
            processSkipRequest();
            return true;
        } else {
            mState = State.Stopped;
            relaxResources(true);
            giveUpAudioFocus();
            sendPlayerState();
            return true; // true indicates we handled the error
        }
    }

    @Override
    public void onGainedAudioFocus() {
        // Toast.makeText(getApplicationContext(), "gained audio focus.",
        // Toast.LENGTH_SHORT).show();
        mAudioFocus = AudioFocus.Focused;

        // restart media player with new focus settings
        if (mState == State.Playing)
            configAndStartMediaPlayer();
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        // Toast.makeText(getApplicationContext(),
        // "lost audio focus." + (canDuck ? "can duck" : "no duck"),
        // Toast.LENGTH_SHORT)
        // .show();
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;

        // start/restart/pause media player with new focus settings
        if (mPlayer != null && mPlayer.isPlaying())
            configAndStartMediaPlayer();
    }

    /** Done retrieving! */
    @Override
    public void onMusicRetrieverPrepared() {
        LogUtil.e(TAG, "onMusicRetrieverPrepared");
        if (mRetriever != null)
            MusicPreferenceUtil.setSongOrder(getApplicationContext(),
                    mRetriever.getSongNumberList());
        mState = State.Stopped;
        sendPlayerState();
        // If the flag indicates we should start playing after retrieving, let's
        // do that now.
        if (mStartPlayingAfterRetrieve) {
            tryToGetAudioFocus();
            playNextSong(false, 0);
            // playNextSong(mWhatToPlayAfterRetrieve == null ? null :
            // mWhatToPlayAfterRetrieve
            // .toString());
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        // Service is being killed, so make sure we release our resources
        if (mRetriever != null) {
            mRetriever.close();
            mRetriever = null;
        }
        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
//        if (mSensing != null)
//            mSensing.flush(getApplicationContext());
    }

    /** サービスに接続するためのBinder */
    public class MyServiceLocalBinder extends Binder {
        // サービスの取得
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    // Binderの生成
    private final IBinder mBinder = new MyServiceLocalBinder();
    // AIDLのBinder
    private IPlayerServiceStub mStub = new IPlayerServiceStub();

    private class IPlayerServiceStub extends IPlayerService.Stub {
        private RemoteCallbackList<IPlayerServiceCallback> mCallbacks = new RemoteCallbackList<IPlayerServiceCallback>();

        @Override
        public void registerCallback(IPlayerServiceCallback callback) throws RemoteException {
            LogUtil.d(TAG, "registerCallback");
            mCallbacks.register(callback);
        }

        @Override
        public void unregisterCallback(IPlayerServiceCallback callback) throws RemoteException {
            LogUtil.d(TAG, "unregisterCallback");
            mCallbacks.unregister(callback);
        }

        @Override
        public void request() throws RemoteException {
            onStateChanged(getItem(), getState(), 0);
        }

        private void onSeekCompleted(boolean isSuccess) {

        }

        private void onStateChanged(MusicItem item, State state, int currentPosition) {
            try {
                int n = mCallbacks.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        mCallbacks.getBroadcastItem(i).onStateChanged(item.getTitle(),
                                item.getId(), item.getAlbumArt(), item.getArtist(),
                                item.getAlbum(), item.getDuration(), item.getPath(),
                                Arrays.binarySearch(State.values(), state), currentPosition);
                        Log.d(TAG, "called callback");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Log.d(TAG, e.getMessage(), e);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mCallbacks.finishBroadcast();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private IPlayerServiceCallback mCallback;

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.i(TAG, "onBind" + ": " + intent);
        if (ACTION_REQUEST_STATE_FROM_OTHER_PROCESS.equals(intent.getAction())) {
            // IServiceMethodのインスタンスを返す
            return mStub;
        }
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.i(TAG, "onUnbind" + ": " + intent);
        return super.onUnbind(intent);
    }

    /** bindした人にplayerをあげる。タダではない */
    public MediaPlayer getPlayer() {
        if (mState == State.Preparing || mState == State.Retrieving)
            return null;
        else
            return mPlayer;
    }

    /** retrieverを渡す */
    public MusicRetriever getRetriever() {
        if (mState == State.Preparing || mState == State.Retrieving)
            return null;
        else
            return mRetriever;
    }

    public MusicItem getItem() {
        if (mRetriever == null || mRetriever.isClosed())
            return null;
        return mRetriever.getNowItem();
    }

    public State getState() {
        return mState;
    }

    public boolean getLooping() {
        return isLooping;
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
        MusicPreferenceUtil.setLooping(getApplicationContext(), looping);
    }

    public boolean getShuffle() {
        isShuffle = mRetriever.getShuffle();
        return mRetriever.getShuffle();
    }

    public boolean setShuffle(boolean shuffle) {
        if (mRetriever == null || mRetriever.isClosed()) {
            return false;
        } else {
            mRetriever.setShuffle(shuffle);
            isShuffle = mRetriever.getShuffle();
            MusicPreferenceUtil.setShuffle(getApplicationContext(), shuffle);
            MusicPreferenceUtil.setSongOrder(getApplicationContext(),
                    mRetriever.getSongNumberList());
            return true;
        }
    }

    /** seekがlistenできない時にセットするとおいしいlistener */
    public void setServiceListener(ServiceListener l) {
        mListener = l;
    }

    private ServiceListener mListener;

    /** seekがlistenできない時にセットするとおいしいlistener */
    public interface ServiceListener {
        void onSeekCompleted(boolean isSuccess);

        void onStateChanged(MusicItem nowItem, State state, int currentPosition);
    }

    /** Activityにintentを送る,stateがかわるごとに */
    private void sendPlayerState() {
        // 再生位置を保存しておく
        if (mRetriever != null && mPlayer != null && mRetriever.getNowItem() != null) {
            MusicPreferenceUtil.setPlaying(getApplicationContext(), mRetriever.getIndex(),
                    mRetriever.getNowItem().getPath(), mPlayer.getCurrentPosition());
        }

        if (mRetriever == null) {
            if (mListener != null)
                mListener.onStateChanged(null, mState, 0);
            if (mStub != null)
                mStub.onStateChanged(null, mState, 0);
            return;
        }
        MusicItem playingItem = mRetriever.getNowItem();
        if (playingItem != null) {
            int currentPosition = 0;
            if (mPlayer != null) {
                currentPosition = mPlayer.getCurrentPosition();
            }
            if (mListener != null)
                mListener.onStateChanged(playingItem, mState, currentPosition);
            if (mStub != null)
                mStub.onStateChanged(playingItem, mState, currentPosition);
            return;
        }

        if (playingItem != null) {
            Intent intent = new Intent(ACTION_STATE_CHANGED, null, getApplicationContext(),
                    PlayerActivity.class);
            intent.putExtra("artist", playingItem.getArtist());
            intent.putExtra("album", playingItem.getAlbum());
            intent.putExtra("title", playingItem.getTitle());
            intent.putExtra("albumArt", playingItem.getAlbumArt());
            intent.putExtra("duration", playingItem.getDuration());
            intent.putExtra("state", mState.toString());
            if (mPlayer != null) {
                intent.putExtra("currentPosition", mPlayer.getCurrentPosition());
            }
            sendBroadcast(intent);
        }
    }

}
