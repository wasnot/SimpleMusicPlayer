
package net.wasnot.music.simplemusicplayer.image;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;

public class AlbumArtCache {
    private static HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();

    public static Bitmap getImage(String key) {
        SoftReference<Bitmap> ref = cache.get(key);
        if (ref != null) {
            // Log.d("cache", "cache hit");
            return ref.get();
        }
        return null;
    }

    public static void setImage(String key, Bitmap image) {
        cache.put(key, new SoftReference<Bitmap>(image));
    }

}
