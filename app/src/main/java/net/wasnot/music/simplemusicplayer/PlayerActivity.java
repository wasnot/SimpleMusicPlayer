package net.wasnot.music.simplemusicplayer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import net.wasnot.music.simplemusicplayer.list.AudioSelector;
import net.wasnot.music.simplemusicplayer.service.MusicItem;
import net.wasnot.music.simplemusicplayer.service.PlayerService;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;
import net.wasnot.music.simplemusicplayer.view.AlbumArtView;
import net.wasnot.music.simplemusicplayer.view.CircleView;
import net.wasnot.music.simplemusicplayer.view.RotateHolder;

import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends ActionBarActivity implements OnClickListener {
    private final String PLAY = "play";
    private final String PAUSE = "pause";
    private final static String TAG = PlayerActivity.class.getSimpleName();
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    private long mCurrentPosition;
    private long mDuration;

    private boolean isSeeking = false;
    private boolean isShuffle = false;
    private boolean isLooping = false;
    private int updatetime = 500;
    private PlayerService mService = null;
    // private boolean didCreated = false;

    private ImageButton playBtn;
    private ImageButton prevBtn;
    private ImageButton nextBtn;
    private TextView titleText;
    private TextView artistText;
    private TextView albumText;
    private TextView timeText;
    private SeekBar volumeBar;
    private CircleView circleBar;
    private AlbumArtView albumArt;
    private RotateHolder rotateHolder;
    private ImageView shuffleBtn;
    private ImageView repeatBtn;

    // private Chronometer mChronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_play);

        playBtn = (ImageButton) findViewById(R.id.playBtn);
        playBtn.setOnClickListener(this);
        playBtn.setContentDescription(PLAY);
        nextBtn = (ImageButton) findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(this);
        prevBtn = (ImageButton) findViewById(R.id.prevBtn);
        prevBtn.setOnClickListener(this);
        shuffleBtn = (ImageView) findViewById(R.id.shuffleBtn);
        shuffleBtn.setOnClickListener(this);
        repeatBtn = (ImageView) findViewById(R.id.repeatBtn);
        repeatBtn.setOnClickListener(this);

        titleText = (TextView) findViewById(R.id.titleText);
        titleText.requestFocus();
        artistText = (TextView) findViewById(R.id.artistText);
        albumText = (TextView) findViewById(R.id.albumText);
        timeText = (TextView) findViewById(R.id.timeText);
        // mChronometer = (Chronometer) findViewById(R.id.playerChronometer);
        // try {
        // Typeface face = Typeface.createFromAsset(getAssets(),
        // "GillSans-Light.ttf");
        // titleText.setTypeface(face);
        // artistText.setTypeface(face);
        // albumText.setTypeface(face);
        // timeText.setTypeface(face);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        // ストリームタイプの設定
        // 　　　　　　player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 最大音量値を取得
        int vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int nowVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        LogUtil.d(TAG, "max:" + vol + ", now:" + nowVol);
        // 音量を設定
        // audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (vol /
        // 2), 0);

        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        volumeBar.setMax(vol);
        volumeBar.setProgress(nowVol);
        volumeBar.setOnSeekBarChangeListener(mSeekBarListener);
        circleBar = (CircleView) findViewById(R.id.circleView);
        circleBar.setOnCircleViewChangeListener(mCircleViewListener);
        albumArt = (AlbumArtView) findViewById(R.id.albumArtView);
        rotateHolder = (RotateHolder) findViewById(R.id.rotateHolder);
        rotateHolder.setOnGestureChangeListener(mGestureChangeLister);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
        titleText.requestFocus();
        startService(new Intent(PlayerService.ACTION_REQUEST_STATE, null, getApplicationContext(),
                PlayerService.class));
        this.bindService(new Intent(PlayerService.ACTION_REQUEST_STATE, null,
                getApplicationContext(), PlayerService.class), mConnection, Service.BIND_IMPORTANT);
        // didCreated = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d(TAG, "onPause");
        // didCreated = false;
        unbindService(mConnection);
        // mChronometer.stop();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        playBtn.setImageResource(R.drawable.play_btn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtil.d(TAG, "key:" + keyCode);
        AudioManager audioManager;
        int nowVol;
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                // 再生／停止のリクエスト
                startService(new Intent(PlayerService.ACTION_TOGGLE_PLAYBACK, null,
                        getApplicationContext(), PlayerService.class));
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                // ここでボリュームダウンの処理
                LogUtil.d(TAG, "push key volume down");
                audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                nowVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (nowVol > 0) {
                    nowVol--;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nowVol, 0);
                    volumeBar.setProgress(nowVol);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                // ここでボリュームアップの処理
                LogUtil.d(TAG, "push key volume up");
                audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                nowVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (nowVol < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                    nowVol++;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nowVol, 0);
                    volumeBar.setProgress(nowVol);
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Intent intent = new Intent();
                    intent.setClass(PlayerActivity.this, MusicActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
//                    overridePendingTransition(R.anim.player_fragment_close_enter,
//                            R.anim.player_fragment_close_exit);
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playBtn:
                if (view.getContentDescription().equals(PLAY)) {
                    startService(new Intent(PlayerService.ACTION_PLAY, null,
                            getApplicationContext(), PlayerService.class));
                    playing();
                } else {
                    startService(new Intent(PlayerService.ACTION_PAUSE, null,
                            getApplicationContext(), PlayerService.class));
                    paused();
                }
                break;
            case R.id.prevBtn:
                startService(new Intent(PlayerService.ACTION_REWIND, null, getApplicationContext(),
                        PlayerService.class));
                // mChronometer.setBase(SystemClock.elapsedRealtime());
                circleBar.setProgress(0);
                break;
            case R.id.nextBtn:
                startService(new Intent(PlayerService.ACTION_SKIP, null, getApplicationContext(),
                        PlayerService.class));
                // mChronometer.stop();
                // mChronometer.setBase(SystemClock.elapsedRealtime());
                circleBar.setProgress(0);
                break;
            case R.id.shuffleBtn:
                if (mService != null) {
                    if (mService.setShuffle(!isShuffle))
                        isShuffle = !isShuffle;
                    if (isShuffle) {
                        shuffleBtn.setImageResource(R.drawable.shuffle_on);
                    } else {
                        shuffleBtn.setImageResource(R.drawable.shuffle_off);
                    }
                }
                break;
            case R.id.repeatBtn:
                if (mService != null) {
                    isLooping = !isLooping;
                    mService.setLooping(isLooping);
                    if (isLooping) {
                        repeatBtn.setImageResource(R.drawable.repeat_on);
                    } else {
                        repeatBtn.setImageResource(R.drawable.repeat_off);
                    }
                }
                break;
        }
    }

    /**
     * pause intentを受け取った,btnをおした
     */
    private void paused() {
        // mCurrentPosition = SystemClock.elapsedRealtime() -
        // mChronometer.getBase();
        // mChronometer.stop();
        cancelTimer();
        playBtn.setImageResource(R.drawable.play_btn);
        playBtn.setContentDescription(PLAY);
    }

    /**
     * play intentを受け取った,btnをおした
     */
    private void playing() {
        // mChronometer.setBase(SystemClock.elapsedRealtime() -
        // mCurrentPosition);
        // mChronometer.start();
        cancelTimer();
        mTimer = new Timer(false);
        mTimer.schedule(new MyTimerTask(), 100, updatetime);
        playBtn.setImageResource(R.drawable.pause_btn);
        playBtn.setContentDescription(PAUSE);
    }

    /**
     * stop intentを受け取った,btnをおした
     */
    private void stopped() {
        // mChronometer.stop();
        // mChronometer.setBase(SystemClock.elapsedRealtime());
        cancelTimer();
        playBtn.setImageResource(R.drawable.play_btn);
        playBtn.setContentDescription(PLAY);
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 時間formatをつくる
     */
    private String timeFormat(long time) {
        long min = time / 60000;
        long sec = (time % 60000) / 1000;
        return min + ":" + (sec < 10 ? "0" + sec : sec + "");
    }

    private void updateState(MusicItem nowItem, PlayerService.State state, int currentPosition) {
        // if (!didCreated) {
        // LogUtil.d(TAG, "----onReceive, did not created view!");
        // return;
        // }
        long duration = 0;
        if (nowItem != null) {
            artistText.setText(nowItem.getArtist());
            albumText.setText(nowItem.getAlbum());
            titleText.setText(nowItem.getTitle());
            titleText.requestFocus();
            albumArt.setAlbumArt(nowItem.getAlbumArt());
            // DecodeAlbumArtTask task = new
            // DecodeAlbumArtTask(nowItem.getPath(), mDecodeListener);
            // task.execute();
            duration = nowItem.getDuration();
            // ID3Test test = new ID3Test(nowplay.getPath());
            // test.getByEntagged();
        } else {
            LogUtil.e(TAG, "----onReceive, item is null!");
        }
        if (mService != null) {
            isLooping = mService.getLooping();
            if (isLooping) {
                repeatBtn.setImageResource(R.drawable.repeat_on);
            } else {
                repeatBtn.setImageResource(R.drawable.repeat_off);
            }
            isShuffle = mService.getShuffle();
            if (isShuffle) {
                shuffleBtn.setImageResource(R.drawable.shuffle_on);
            } else {
                shuffleBtn.setImageResource(R.drawable.shuffle_off);
            }
        }

        mCurrentPosition = currentPosition;
        mDuration = duration;
        // mChronometer.setBase(SystemClock.elapsedRealtime() -
        // mCurrentPosition);
        if (state == PlayerService.State.Playing) {
            LogUtil.d(TAG, "----onReceive! playing");
            playing();
        } else if (state == PlayerService.State.Paused) {
            LogUtil.d(TAG, "----onReceive! paused");
            timeText.setText(timeFormat(mCurrentPosition) + " / " + timeFormat(mDuration));
            // circleBar.setProgress(mCurrentPosition);
            paused();
        } else if (state == PlayerService.State.Stopped) {
            LogUtil.d(TAG, "----onReceive! stopped");
            timeText.setText(timeFormat(0) + " / " + timeFormat(mDuration));
            circleBar.setProgress(0);
            stopped();
        } else {
            LogUtil.d(TAG, "----onReceive! other");
        }
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            LogUtil.d(TAG, "onProgressChanged, seekbar" + seekBar);
            LogUtil.d(TAG, "Ring Volume:" + progress);
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            LogUtil.d(TAG,
                    "change volume," + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // LogUtil.d(TAG, "onStartTrackingTouch, seekbar" + seekBar);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // LogUtil.d(TAG, "onStopTrackingTouch, seekbar" + seekBar);
        }
    };
    private CircleView.OnCircleViewChangeListener mCircleViewListener = new CircleView.OnCircleViewChangeListener() {
        @Override
        public void onProgressChanged(CircleView view, float progress, boolean fromUser) {
            LogUtil.d(TAG, "onProgressChanged, circleBar" + view);
            LogUtil.d(TAG, "Music Seek:" + progress); // TextViewに設定値を表示
            Intent intent = new Intent(PlayerService.ACTION_SEEK, null, getApplicationContext(),
                    PlayerService.class);
            intent.putExtra(AudioSelector.INTENT_PARAM_SEEK, progress);
            startService(intent);
        }

        @Override
        public void onStartTrackingTouch(CircleView view) {
            LogUtil.e(TAG, "onStartTrackingTouch, view" + view);
            isSeeking = true;
        }

        @Override
        public void onChangeProgress(float progress) {
            if (isSeeking) {
                timeText.setText(timeFormat((long) (progress * (float) mDuration)) + " / "
                        + timeFormat(mDuration));
            }
        }

        @Override
        public void onStopTrackingTouch(CircleView view) {
            LogUtil.e(TAG, "onStopTrackingTouch, view" + view);
            // isSeeking = false;
        }

        @Override
        public void onInterrupted(CircleView circleView) {
            LogUtil.e(TAG, "onInterrupted");
            isSeeking = false;
        }
    };
    private RotateHolder.OnRoteteChangeListener mGestureChangeLister = new RotateHolder.OnRoteteChangeListener() {
        @Override
        public void onFling(boolean isNext) {
            LogUtil.d(TAG, "onFling " + (isNext ? "next" : "prev"));
        }

        @Override
        public void onStartRotate(boolean isNext) {
            LogUtil.d(TAG, "onStartRotate " + (isNext ? "next" : "prev"));
            Intent intent;
            if (isNext)
                intent = new Intent(PlayerService.ACTION_SKIP, null, getApplicationContext(),
                        PlayerService.class);
            else
                intent = new Intent(PlayerService.ACTION_PREVIOUS, null, getApplicationContext(),
                        PlayerService.class);
            startService(intent);
        }

        @Override
        public void onRotatingCenter(boolean isNext) {
            LogUtil.d(TAG, "onRotatingCenter " + (isNext ? "next" : "prev"));
        }

        @Override
        public void onFinishRotate(boolean isNext) {
            LogUtil.d(TAG, "onFinishRotate " + (isNext ? "next" : "prev"));
        }
    };

    /**
     * 画面を更新するためのTimerTask
     */
    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                public void run() {
                    if (circleBar != null && mService != null && !isSeeking
                            && mService.getPlayer() != null) {
                        int max = mService.getPlayer().getDuration();
                        int nowSeek = mService.getPlayer().getCurrentPosition();
                        mCurrentPosition = nowSeek;
                        mDuration = max;
                        // 再描画を指示
                        circleBar.setProgress(((float) nowSeek / (float) max));
                        timeText.setText(timeFormat(nowSeek) + " / " + timeFormat(max));
                    }
                }
            });
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            mService = ((PlayerService.MyServiceLocalBinder) binder).getService();
            mService.setServiceListener(mServiceListener);
            LogUtil.d(TAG, "service:" + mService + ", player:" + mService.getPlayer());
            if (mService != null) {
                mHandler.post(new Runnable() {
                    public void run() {
                        int currentPosition = 0;
                        if (mService.getPlayer() != null)
                            currentPosition = mService.getPlayer().getCurrentPosition();
                        updateState(mService.getItem(), mService.getState(), currentPosition);
                    }
                });
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            LogUtil.d(TAG, "onServiceDisconnected");
            mService.setServiceListener(null);
            mService = null;
        }
    };

    private PlayerService.ServiceListener mServiceListener = new PlayerService.ServiceListener() {
        @Override
        public void onSeekCompleted(boolean isSuccess) {
            isSeeking = false;
        }

        @Override
        public void onStateChanged(final MusicItem nowItem, final PlayerService.State state,
                                   final int currentPosition) {
            mHandler.post(new Runnable() {
                public void run() {
                    updateState(nowItem, state, currentPosition);
                }
            });
        }

    };
    // private OnDecodeAlbumArtListener mDecodeListener = new
    // OnDecodeAlbumArtListener() {
    // @Override
    // public void onDecoded(final Bitmap result) {
    // LogUtil.i(TAG, "decoded! :" + (result != null ? result.getHeight() :
    // "null"));
    // mHandler.post(new Runnable() {
    // public void run() {
    // albumArt.setAlbumArtBitmap(result);
    // }
    // });
    // }
    // };
}
