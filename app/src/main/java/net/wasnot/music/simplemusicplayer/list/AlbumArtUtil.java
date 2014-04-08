
package net.wasnot.music.simplemusicplayer.list;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.MediaStore;

import net.wasnot.music.simplemusicplayer.utli.LogUtil;


public class AlbumArtUtil {
    private static final String TAG = AlbumArtUtil.class.getSimpleName();

    public static String getAlbumArt(Context con, long albumId) {
        ContentResolver cr = con.getContentResolver();
        return getAlbumArt(cr, albumId);
    }

    public static String getAlbumArt(ContentResolver cr, long albumId) {
        Cursor albumC = null;
        String filePath = null;
        try {
            // media.album_id = album._id;
            albumC = cr.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
                    MediaStore.Audio.Albums._ID + " = ?", new String[] {
                        String.valueOf(albumId)
                    }, null);
            if (albumC == null)
                return null;
            // LogUtil.d(TAG, "id:" + _id + ", album " + albumC.getCount());
            // artworkの一番最初をとりだす
            if (albumC.moveToFirst() && albumC.getCount() > 0) {
                filePath = albumC.getString(albumC
                        .getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART));
            }
        } catch (SQLiteException e) {
            LogUtil.e(TAG, e.toString());
        } finally {
            albumC.close();
        }
        return filePath;
    }

    /** album id と album countを返す */
    public static long[] getAlbumIdAndCount(Context con, Uri uri, String selection) {
        long[] result = new long[] {
                -1, 0
        };
        ContentResolver cr = con.getContentResolver();
        Cursor albumC = null;
        try {
            albumC = cr.query(uri, null, selection, null, null);
            if (albumC == null || !albumC.moveToFirst())
                return result;
            result[1] = albumC.getCount();
            // artworkの一番最初をとりだす
            if (result[1] > 0) {
                result[0] = albumC.getLong(albumC.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                return result;
            }
        } catch (SQLiteException e) {
            LogUtil.e(TAG, e.toString());
        } finally {
            albumC.close();
        }
        return result;
    }
}
