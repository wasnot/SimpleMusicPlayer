
package net.wasnot.music.simplemusicplayer.service;

import android.content.ContentUris;
import android.net.Uri;

public class MusicItem {
    private String title;
    private long id;
    private String albumArt;
    private String artist;
    private String album;
    private long duration;
    private String path;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setId(long id) {
        this.id = id;

    }

    public long getId() {
        return this.id;
    }

    /**
     * これかしこい！
     * 
     * @return
     */
    public Uri getURI() {
        return ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public String getAlbumArt() {
        return this.albumArt;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtist() {
        return this.artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setDuration(long duration) {
        this.duration = duration;

    }

    public long getDuration() {
        return this.duration;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }
}
