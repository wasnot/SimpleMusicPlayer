
package net.wasnot.music.simplemusicplayer.service;

import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;


import net.wasnot.music.simplemusicplayer.utli.LogUtil;

import java.util.Arrays;

public class EqualizerUtil {
    private static final String TAG = EqualizerUtil.class.getSimpleName();

    public static void setEqualizer(MediaPlayer mediaPlayer) {
        try {
            // MediaPlayer mediaPlayer_ = MediaPlayer.create(this, R.raw.music);
            Equalizer equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
            equalizer.setEnabled(true);
            // 調整を行うことのできる中心周波数の数を確認
            short bands = equalizer.getNumberOfBands();
            LogUtil.d(TAG, "NumberOfBands: " + bands);

            // 設定出来るレベルの下限、上限を確認
            short minEQLevel = equalizer.getBandLevelRange()[0];
            short maxEQLevel = equalizer.getBandLevelRange()[1];
            LogUtil.d(TAG, "minEQLevel: " + String.valueOf(minEQLevel));
            LogUtil.d(TAG, "maxEQLevel: " + String.valueOf(maxEQLevel));
//            if (Globals.AUDIO_LEVEL_MAX) {
            if (false) {
                setMaxPreset(equalizer);
            } else {
                setPerfectPreset(equalizer);
            }
            boolean onlySet = true;
            if (onlySet)
                return;

            for (short i = 0; i < bands; i++) {
                // 中心周波数の表示
                LogUtil.d(TAG,
                        i + ", centerFreq:" + String.valueOf(equalizer.getCenterFreq(i) / 1000)
                                + "Hz");
                try {
                    LogUtil.d(TAG,
                            i + ", BandFreqRange:" + Arrays.toString(equalizer.getBandFreqRange(i)));
                } catch (Exception e) {
                }
                LogUtil.d(TAG, i + ", bandLevel:" + String.valueOf(equalizer.getBandLevel(i)));
                // レベルを真ん中に設定
                // equalizer.setBandLevel(i, (short) ((minEQLevel + maxEQLevel)
                // *
                // 0.6));
                // equalizer.setBandLevel(i, (short) 100);
            }

            short presets = equalizer.getNumberOfPresets();
            LogUtil.d(TAG, "NumberOfPresets: " + presets);
            try {
                LogUtil.d(TAG, " current:" + equalizer.getCurrentPreset());
            } catch (Exception e) {
            }
            for (short i = 0; i < presets; i++) {
                LogUtil.d(TAG, i + ", preset  " + equalizer.getPresetName(i));

            }
            // try {
            // equalizer.usePreset((short) 1);
            // LogUtil.d(TAG, " current:" + equalizer.getCurrentPreset());
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setMaxPreset(Equalizer equalizer) {
        short bands = equalizer.getNumberOfBands();
        short[] range = equalizer.getBandLevelRange();
        short max = (short) (range[1] - (range[1] + range[0]) / 2);
        // double maxDb = 12;
        for (short i = 0; i < bands; i++) {
            // int freq = equalizer.getCenterFreq(i);
            // short level = equalizer.getBandLevel(i);
            equalizer.setBandLevel(i, max);
        }
    }

    private static void setPerfectPreset(Equalizer equalizer) {
        short bands = equalizer.getNumberOfBands();
        short[] range = equalizer.getBandLevelRange();
        short max = (short) (range[1] - (range[1] + range[0]) / 2);
        double maxDb = 12;

        for (short i = 0; i < bands; i++) {
            int freq = equalizer.getCenterFreq(i);
            short level = equalizer.getBandLevel(i);
            if (freq <= 32000) {
                level = (short) (max * calcLevel(0, 0, 32000, 3, freq) / maxDb);
            } else if (freq <= 64000) {
                level = (short) (max * calcLevel(32000, 3, 64000, 6, freq) / maxDb);
            } else if (freq <= 125000) {
                level = (short) (max * calcLevel(64000, 6, 125000, 9, freq) / maxDb);
            } else if (freq <= 250000) {
                level = (short) (max * calcLevel(125000, 9, 250000, 7, freq) / maxDb);
            } else if (freq <= 500000) {
                level = (short) (max * calcLevel(250000, 7, 500000, 6, freq) / maxDb);
            } else if (freq <= 1000000) {
                level = (short) (max * calcLevel(500000, 6, 1000000, 5, freq) / maxDb);
            } else if (freq <= 2000000) {
                level = (short) (max * calcLevel(1000000, 5, 2000000, 7, freq) / maxDb);
            } else if (freq <= 4000000) {
                level = (short) (max * calcLevel(2000000, 7, 4000000, 9, freq) / maxDb);
            } else if (freq <= 8000000) {
                level = (short) (max * calcLevel(4000000, 9, 8000000, 11, freq) / maxDb);
            } else if (freq <= 16000000 || 16000000 < freq) {
                level = (short) (max * calcLevel(8000000, 11, 12000000, 8, freq) / maxDb);
            }
            equalizer.setBandLevel(i, level);
        }

        // 32:+3
        // 64:+6
        // 125:+9
        // 250:+7
        // 500:+6
        // 1k:+5
        // 2k:+7
        // 4k:+9
        // 8k:+11
        // 16k:+8
    }

    /** x値から2点間のグラフのy値を計算する */
    private static double calcLevel(int x0, int y0, int x1, int y1, int px) {
        // 傾き
        double k = (y1 - y0) / (x1 - x0);
        // 切片
        double s = y0 - k * x0;
        double resulty = k * px + s;
        return resulty;
    }
}
