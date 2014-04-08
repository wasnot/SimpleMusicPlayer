
package net.wasnot.music.simplemusicplayer.list;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class AudioSelector {

    public static final int CATEGORY_SONGS = 0;
    public static final int CATEGORY_ALBUM = 1;
    public static final int CATEGORY_GENRE = 2;
    public static final int CATEGORY_PLAYLIST = 3;
    public static final int CATEGORY_ARTIST = 4;
    public static final int CATEGORY_DETAILS_ALBUM = 5;
    public static final int CATEGORY_DETAILS_GENRE = 6;
    public static final int CATEGORY_DETAILS_PLAYLIST = 7;
    public static final int CATEGORY_DETAILS_ARTIST = 8;

    public static final String INTENT_PARAM_CATEGORY = "category";
    public static final String INTENT_PARAM_CATEGORY_ID = "categoryId";
    // public static final String INTENT_PARAM_CATEGORY_COLUMN_NAME =
    // "categoryColumneName";
    // public static final String INTENT_PARAM_CATEGORY_SELECTION =
    // "categorySelection";
    public static final String INTENT_PARAM_URI = "uri";
    public static final String INTENT_PARAM_SONG_INDEX = "index";
    public static final String INTENT_PARAM_IS_SHUFFLE = "isShuffle";
    public static final String INTENT_PARAM_SEEK = "seek";

    public static String getSortOrder(int category) {
        String sortOrder = null;
        switch (category) {
            case CATEGORY_SONGS:
                sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE NOCASE ASC";
                break;
            case CATEGORY_PLAYLIST:
                sortOrder = MediaStore.Audio.PlaylistsColumns.NAME + " COLLATE NOCASE ASC";
                break;
            case CATEGORY_GENRE:
                sortOrder = MediaStore.Audio.GenresColumns.NAME + " COLLATE NOCASE ASC";
                break;
            case CATEGORY_ALBUM:
                sortOrder = MediaStore.Audio.AlbumColumns.ALBUM + " COLLATE NOCASE ASC";
                break;
            case CATEGORY_ARTIST:
                sortOrder = MediaStore.Audio.ArtistColumns.ARTIST + " COLLATE NOCASE ASC";
                break;
            case CATEGORY_DETAILS_PLAYLIST:
                sortOrder = MediaStore.Audio.Playlists.Members.PLAY_ORDER + " ASC";
                break;
            case CATEGORY_DETAILS_GENRE:
                sortOrder = MediaStore.Audio.Genres.Members.TITLE + " COLLATE NOCASE ASC";
                break;
            case CATEGORY_DETAILS_ALBUM:
                sortOrder = MediaStore.Audio.Media.TRACK + " ASC, " + MediaStore.Audio.Media.TITLE
                        + " COLLATE NOCASE ASC";
                break;
            case CATEGORY_DETAILS_ARTIST:
                sortOrder = MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC, "
                        + MediaStore.Audio.Media.TRACK + " ASC, " + MediaStore.Audio.Media.TITLE
                        + " COLLATE NOCASE ASC";
                break;
        }
        return sortOrder;
    }

    public static String getSort(int category) {
        String sort = null;
        switch (category) {
            case CATEGORY_SONGS:
                sort = "title";// MediaStore.Audio.AudioColumns.TITLE;
                break;
            case CATEGORY_PLAYLIST:
                sort = "title";// MediaStore.Audio.PlaylistsColumns.NAME;
                break;
            case CATEGORY_GENRE:
                sort = "title";// MediaStore.Audio.GenresColumns.NAME;
                break;
            case CATEGORY_ALBUM:
                sort = "title";// MediaStore.Audio.AlbumColumns.ALBUM;
                break;
            case CATEGORY_ARTIST:
                sort = "title";// MediaStore.Audio.ArtistColumns.ARTIST;
                break;
            case CATEGORY_DETAILS_PLAYLIST:
                sort = "playorder";// MediaStore.Audio.Playlists.Members.PLAY_ORDER;
                break;
            case CATEGORY_DETAILS_GENRE:
                sort = "title";// MediaStore.Audio.Genres.Members.TITLE;
                break;
            case CATEGORY_DETAILS_ALBUM:
                sort = "track";// MediaStore.Audio.Media.TRACK;
                break;
            case CATEGORY_DETAILS_ARTIST:
                sort = "album";// MediaStore.Audio.Media.ALBUM;
                break;
        }
        return sort;
    }

    public static String getOrder(int category) {
        String order = null;
        switch (category) {
            case CATEGORY_SONGS:
            case CATEGORY_PLAYLIST:
            case CATEGORY_GENRE:
            case CATEGORY_ALBUM:
            case CATEGORY_ARTIST:
            case CATEGORY_DETAILS_PLAYLIST:
            case CATEGORY_DETAILS_GENRE:
            case CATEGORY_DETAILS_ALBUM:
            case CATEGORY_DETAILS_ARTIST:
                order = "asc";
                break;
        }
        return order;
    }

    /** songリスト用のuriを得る 主にdetail list用 */
    public static Uri getSongUri(int category, int categoryId) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        switch (category) {
            case CATEGORY_PLAYLIST:
            case CATEGORY_DETAILS_PLAYLIST:
                uri = MediaStore.Audio.Playlists.Members.getContentUri("external", categoryId);
                break;
            case CATEGORY_GENRE:
            case CATEGORY_DETAILS_GENRE:
                uri = MediaStore.Audio.Genres.Members.getContentUri("external", categoryId);
                break;
            case CATEGORY_ALBUM:
            case CATEGORY_DETAILS_ALBUM:
            case CATEGORY_ARTIST:
            case CATEGORY_DETAILS_ARTIST:
            case CATEGORY_SONGS:
            default:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
        }
        return uri;
    }

    /** categoryリスト用のuriを得る 今は編集用 */
    protected static Uri getCategoryUri(int category) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        switch (category) {
            case CATEGORY_PLAYLIST:
                uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
                break;
            case CATEGORY_GENRE:
                uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
                break;
            case CATEGORY_DETAILS_GENRE:
            case CATEGORY_DETAILS_PLAYLIST:
            case CATEGORY_ALBUM:
            case CATEGORY_DETAILS_ALBUM:
            case CATEGORY_ARTIST:
            case CATEGORY_DETAILS_ARTIST:
            case CATEGORY_SONGS:
            default:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
        }
        return uri;
    }

    /** categoryIdのcolumn名を得る 今は編集用 */
    protected static String getCategoryIdColumn(int category) {
        String columnName = MediaStore.Audio.Media._ID;
        switch (category) {
            case CATEGORY_PLAYLIST:
            case CATEGORY_DETAILS_PLAYLIST:
                columnName = MediaStore.Audio.Playlists._ID;
                break;
            case CATEGORY_GENRE:
            case CATEGORY_DETAILS_GENRE:
                columnName = MediaStore.Audio.Genres._ID;
                break;
            case CATEGORY_ALBUM:
                columnName = MediaStore.Audio.AudioColumns.ALBUM_ID;
                break;
            case CATEGORY_ARTIST:
                columnName = MediaStore.Audio.AudioColumns.ARTIST_ID;
                break;
            case CATEGORY_SONGS:
            case CATEGORY_DETAILS_ALBUM:
            case CATEGORY_DETAILS_ARTIST:
            default:
                columnName = MediaStore.Audio.Media._ID;
                break;
        }
        return columnName;
    }

    /** songIdのcolumn名を得る 今は編集用 */
    protected static String getSongIdColumn(int category) {
        String columnName = MediaStore.Audio.Media._ID;
        switch (category) {
            case CATEGORY_PLAYLIST:
            case CATEGORY_DETAILS_PLAYLIST:
                columnName = MediaStore.Audio.Playlists.Members.AUDIO_ID;
                break;
            case CATEGORY_GENRE:
            case CATEGORY_DETAILS_GENRE:
                columnName = MediaStore.Audio.Genres.Members.AUDIO_ID;
                break;
            case CATEGORY_ARTIST:
            case CATEGORY_ALBUM:
            case CATEGORY_SONGS:
            case CATEGORY_DETAILS_ALBUM:
            case CATEGORY_DETAILS_ARTIST:
            default:
                columnName = MediaStore.Audio.AudioColumns._ID;
                break;
        }
        return columnName;
    }

    /** categoryNameのcolumn名を得る 今は編集用 */
    protected static String getCategoryNameColumn(int category) {
        String columnName = MediaStore.Audio.Media.DISPLAY_NAME;
        switch (category) {
            case CATEGORY_PLAYLIST:
                columnName = MediaStore.Audio.PlaylistsColumns.NAME;
                break;
            case CATEGORY_ALBUM:
                columnName = MediaStore.Audio.AlbumColumns.ALBUM;
                break;
            case CATEGORY_ARTIST:
                columnName = MediaStore.Audio.ArtistColumns.ARTIST;
                break;
            case CATEGORY_GENRE:
                columnName = MediaStore.Audio.GenresColumns.NAME;
                break;
            case CATEGORY_DETAILS_PLAYLIST:
            case CATEGORY_DETAILS_ALBUM:
            case CATEGORY_DETAILS_ARTIST:
            case CATEGORY_DETAILS_GENRE:
            case CATEGORY_SONGS:
            default:
                columnName = MediaStore.Audio.Media.DISPLAY_NAME;
                break;
        }
        return columnName;
    }

    /** updateのcolumn名を得る 今は編集用 */
    protected static String getUpdateColumn(int category) {
        String columnName = MediaStore.Audio.Media.DATE_MODIFIED;
        switch (category) {
            case CATEGORY_PLAYLIST:
                columnName = MediaStore.Audio.PlaylistsColumns.DATE_MODIFIED;
                break;
            case CATEGORY_GENRE:
                columnName = null;
                break;
            case CATEGORY_DETAILS_PLAYLIST:
            case CATEGORY_ALBUM:
            case CATEGORY_DETAILS_ALBUM:
            case CATEGORY_ARTIST:
            case CATEGORY_DETAILS_ARTIST:
            case CATEGORY_DETAILS_GENRE:
            case CATEGORY_SONGS:
            default:
                columnName = MediaStore.Audio.AudioColumns.DATE_MODIFIED;
                break;
        }
        return columnName;
    }

    /** categoryから表示すべき詳細カテゴリを得る 今はdetail list用 */
    protected static int getDetailCategory(int currentCategory) {
        int result = currentCategory;
        switch (currentCategory) {
            case CATEGORY_PLAYLIST:
                result = CATEGORY_DETAILS_PLAYLIST;
                break;
            case CATEGORY_ALBUM:
                result = CATEGORY_DETAILS_ALBUM;
                break;
            case CATEGORY_ARTIST:
                result = CATEGORY_DETAILS_ARTIST;
                break;
            case CATEGORY_GENRE:
                result = CATEGORY_DETAILS_GENRE;
                break;
            case CATEGORY_SONGS:
            case CATEGORY_DETAILS_ALBUM:
            case CATEGORY_DETAILS_ARTIST:
            case CATEGORY_DETAILS_GENRE:
            case CATEGORY_DETAILS_PLAYLIST:
            default:
                result = CATEGORY_SONGS;
                break;
        }
        return result;
    }

    /** categoryかどうか */
    public static boolean isCategory(int category) {
        boolean result = false;
        switch (category) {
            case CATEGORY_PLAYLIST:
            case CATEGORY_ALBUM:
            case CATEGORY_ARTIST:
            case CATEGORY_GENRE:
                result = true;
                break;
            case CATEGORY_SONGS:
            case CATEGORY_DETAILS_ALBUM:
            case CATEGORY_DETAILS_ARTIST:
            case CATEGORY_DETAILS_GENRE:
            case CATEGORY_DETAILS_PLAYLIST:
            default:
                result = false;
                break;
        }
        return result;
    }

    /** 詳細かどうか */
    protected static boolean isDetails(int category) {
        boolean result = false;
        switch (category) {
            case CATEGORY_DETAILS_ALBUM:
            case CATEGORY_DETAILS_ARTIST:
            case CATEGORY_DETAILS_GENRE:
            case CATEGORY_DETAILS_PLAYLIST:
                result = true;
                break;
            case CATEGORY_PLAYLIST:
            case CATEGORY_ALBUM:
            case CATEGORY_ARTIST:
            case CATEGORY_GENRE:
            case CATEGORY_SONGS:
            default:
                result = false;
                break;
        }
        return result;
    }

    /** categoryIdでしぼる場合のselectionを作る detail list用 */
    public static String getSelection(int category, int categoryId) {
        String selection = null;
        switch (category) {
            case CATEGORY_ALBUM:
            case CATEGORY_DETAILS_ALBUM:
                selection = MediaStore.Audio.AudioColumns.ALBUM_ID + " = " + categoryId;
                // columnName = MediaStore.Audio.AudioColumns.ALBUM_ID;
                // categoryId = id;
                break;
            case CATEGORY_ARTIST:
            case CATEGORY_DETAILS_ARTIST:
                selection = MediaStore.Audio.AudioColumns.ARTIST_ID + " = " + categoryId;
                // columnName = MediaStore.Audio.AudioColumns.ARTIST_ID;
                // categoryId = id;
                break;
            case CATEGORY_SONGS:
            case CATEGORY_PLAYLIST:
            case CATEGORY_GENRE:
            case CATEGORY_DETAILS_GENRE:
            case CATEGORY_DETAILS_PLAYLIST:
            default:
                selection = null;
                break;
        }
        return selection;
    }

    /** cursorを渡してcategoryIdを得る */
    protected static int getCategoryId(Cursor c, int category) {
        int categoryId = -1;
        switch (category) {
            case CATEGORY_ALBUM:
                categoryId = c.getInt(c.getColumnIndex(MediaStore.Audio.Albums._ID));
                break;
            case CATEGORY_ARTIST:
                categoryId = c.getInt(c.getColumnIndex(MediaStore.Audio.Artists._ID));
                break;
            case CATEGORY_PLAYLIST:
                categoryId = c.getInt(c.getColumnIndex(MediaStore.Audio.Playlists._ID));
                break;
            case CATEGORY_GENRE:
                categoryId = c.getInt(c.getColumnIndex(MediaStore.Audio.Genres._ID));
                break;
            case CATEGORY_SONGS:
                categoryId = c.getInt(c.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
                break;
            case CATEGORY_DETAILS_ALBUM:
                categoryId = c.getInt(c.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
                break;
            case CATEGORY_DETAILS_ARTIST:
                categoryId = c.getInt(c.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST_ID));
                break;
            case CATEGORY_DETAILS_GENRE:
                categoryId = c.getInt(c.getColumnIndex(MediaStore.Audio.Genres.Members.GENRE_ID));
                break;
            case CATEGORY_DETAILS_PLAYLIST:
                categoryId = c.getInt(c
                        .getColumnIndex(MediaStore.Audio.Playlists.Members.PLAYLIST_ID));
                break;
            default:
                categoryId = c.getInt(c.getColumnIndex(MediaStore.MediaColumns._ID));
                break;
        }
        return categoryId;
    }

    /** categoryのtype名を得る 表示／API/出し分け用 */
    public static String getCategoryTypeName(int category) {
        String categoryName = "unknown";
        switch (category) {
            case CATEGORY_ALBUM:
            case CATEGORY_DETAILS_ALBUM:
                categoryName = "album";
                break;
            case CATEGORY_ARTIST:
            case CATEGORY_DETAILS_ARTIST:
                categoryName = "artist";
                break;
            case CATEGORY_PLAYLIST:
            case CATEGORY_DETAILS_PLAYLIST:
                categoryName = "playlist";
                break;
            case CATEGORY_GENRE:
            case CATEGORY_DETAILS_GENRE:
                categoryName = "genre";
                break;
            case CATEGORY_SONGS:
                categoryName = "list";
                break;
            default:
                categoryName = "unknown";
                break;
        }
        return categoryName;
    }

    /** categoryのtype名からcategoryIntを得る */
    public static int getDetailCategoryInt(String categoryName) {
        int categoryNum;
        if (categoryName == null) {
            categoryNum = CATEGORY_SONGS;
        } else if (categoryName.equals("album")) {
            categoryNum = CATEGORY_DETAILS_ALBUM;
        } else if (categoryName.equals("artist")) {
            categoryNum = CATEGORY_DETAILS_ARTIST;
        } else if (categoryName.equals("playlist")) {
            categoryNum = CATEGORY_DETAILS_PLAYLIST;
        } else if (categoryName.equals("genre")) {
            categoryNum = CATEGORY_DETAILS_GENRE;
        } else if (categoryName.equals("list")) {
            categoryNum = CATEGORY_SONGS;
        } else {
            categoryNum = CATEGORY_SONGS;
        }
        return categoryNum;
    }

}
