
package net.wasnot.music.simplemusicplayer.list;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.wasnot.music.simplemusicplayer.R;

public class MusicViewManager {
    private static String TAG = MusicViewManager.class.getSimpleName();

    public interface MusicHandler {
        public View makeView(MusicView musicViews, LayoutInflater inflater);

        public long setView(MusicView musicViews, Context con, Cursor cursor);
    }

    public static class MusicView {
        public ImageView albumArt;
        TextView title;
        TextView subTitle;
        TextView time;
        long albumId;
    }

    public MusicHandler getMusicHandler(int item) {
        switch (item) {
            case AudioSelector.CATEGORY_PLAYLIST:
                return new MusicPlaylistHandler();
            case AudioSelector.CATEGORY_ALBUM:
                return new MusicAlbumHandler();
            case AudioSelector.CATEGORY_ARTIST:
                return new MusicArtistHandler();
            case AudioSelector.CATEGORY_SONGS:
                return new MusicSongHandler();
            case AudioSelector.CATEGORY_GENRE:
                return new MusicGenreHandler();
            default:
                return new MusicSongHandler();
        }
    }

    public class MusicSongHandler implements MusicHandler {
        public View makeView(MusicView item, LayoutInflater inflater) {
            View v = inflater.inflate(R.layout.list_item_music_song, null);
            item.albumArt = (ImageView) v.findViewById(R.id.SongImage);
            item.title = (TextView) v.findViewById(R.id.SongName);
            item.subTitle = (TextView) v.findViewById(R.id.SongArtist);
            item.time = (TextView) v.findViewById(R.id.MusicTime);
            v.setTag(item);
            return v;
        }

        public long setView(MusicView musicViews, Context con, Cursor cursor) {
            final int indexAlbumId = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            final int indexTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            final int indexArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            final int indexDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            final int indexData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            // 対象画像のDB情報から、IDを取得
            String title = cursor.getString(indexTitle);
            String artist = cursor.getString(indexArtist);
            long duration = cursor.getLong(indexDuration);
            long albumId = cursor.getLong(indexAlbumId);
            // 対象画像のDB情報から、DATAを取得
            String tag = cursor.getString(indexData);

            musicViews.title.setText(title);
            musicViews.subTitle.setText(artist);
            musicViews.time.setText(timeFormat(duration));
            // album artを識別するのに必要
            musicViews.albumArt.setTag(tag);
            musicViews.albumId = albumId;

            return albumId;
        }

        /** 時間formatをつくる */
        private String timeFormat(long time) {
            long min = time / 60000;
            long sec = (time % 60000) / 1000;
            return min + ":" + (sec < 10 ? "0" + sec : sec + "");
        }
    }

    public class MusicPlaylistHandler implements MusicHandler {
        @Override
        public View makeView(MusicView musicViews, LayoutInflater inflater) {
            View v = inflater.inflate(R.layout.list_item_music_playlist, null);
            musicViews.albumArt = (ImageView) v.findViewById(R.id.SongImage);
            musicViews.title = (TextView) v.findViewById(R.id.SongName);
            musicViews.time = (TextView) v.findViewById(R.id.MusicTime);
            // musicViewsを記録
            v.setTag(musicViews);
            return v;
        }

        @Override
        public long setView(MusicView musicViews, Context con, Cursor cursor) {
            final int indexId = cursor.getColumnIndex(MediaStore.Audio.Playlists._ID);
            final int indexTitle = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
            final int indexData = cursor.getColumnIndex(MediaStore.Audio.Playlists.DATA);
            String title = cursor.getString(indexTitle);
            long id = cursor.getLong(indexId);
            String tag = cursor.getString(indexData);
            long[] albumCount = AlbumArtUtil.getAlbumIdAndCount(con,
                    MediaStore.Audio.Playlists.Members.getContentUri("external", id),
                    MediaStore.Audio.Media.IS_MUSIC + " = 1");
            long albumId = albumCount[0];
            long count = albumCount[1];

            musicViews.title.setText(title);
            musicViews.time.setText(String.valueOf(count));
            // album artを識別するのに必要
            musicViews.albumArt.setTag(tag);
            musicViews.albumId = albumId;
            return albumId;
        }

    }

    public class MusicAlbumHandler implements MusicHandler {
        @Override
        public View makeView(MusicView musicViews, LayoutInflater inflater) {
            View v = inflater.inflate(R.layout.list_item_music_song, null);
            musicViews.albumArt = (ImageView) v.findViewById(R.id.SongImage);
            musicViews.title = (TextView) v.findViewById(R.id.SongName);
            musicViews.subTitle = (TextView) v.findViewById(R.id.SongArtist);
            musicViews.time = (TextView) v.findViewById(R.id.MusicTime);
            v.setTag(musicViews);
            return v;
        }

        @Override
        public long setView(MusicView musicViews, Context con, Cursor cursor) {
            final int indexId = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
            final int indexTitle = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            final int indexArtist = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
            final int indexCount = cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            // 対象画像のDB情報から、IDを取得
            String title = cursor.getString(indexTitle);
            String artist = cursor.getString(indexArtist);
            long count = cursor.getLong(indexCount);
            long albumId = cursor.getLong(indexId);

            musicViews.title.setText(title);
            musicViews.subTitle.setText(artist);
            musicViews.time.setText(String.valueOf(count));
            // album artを識別するのに必要
            musicViews.albumArt.setTag(String.valueOf(albumId));
            musicViews.albumId = albumId;

            return albumId;
        }
    }

    public class MusicArtistHandler implements MusicHandler {
        @Override
        public View makeView(MusicView musicViews, LayoutInflater inflater) {
            View v = inflater.inflate(R.layout.list_item_music_simple, null);
            musicViews.title = (TextView) v.findViewById(R.id.SongName);
            musicViews.time = (TextView) v.findViewById(R.id.MusicTime);
            musicViews.albumArt = null;
            v.setTag(musicViews);
            return v;
        }

        @Override
        public long setView(MusicView musicViews, Context con, Cursor cursor) {
            final int indexArtistId = cursor.getColumnIndex(MediaStore.Audio.Artists._ID);
            final int indexArtist = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
            final int indexCount = cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
            // 対象画像のDB情報から、IDを取得
            String artist = cursor.getString(indexArtist);
            long count = cursor.getLong(indexCount);
            long artistId = cursor.getLong(indexArtistId);

            // long[] albumCount = AlbumArtUtil.getAlbumIdAndCount(con,
            // MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            // MediaStore.Audio.AlbumColumns.ALBUM_ID + " = " + artistId);
            // long albumId = albumCount[0];
            // long count = albumCount[1];

            musicViews.title.setText(artist);
            musicViews.time.setText(String.valueOf(count));
            musicViews.albumId = -1;

            return -1;
            // album artを識別するのに必要
            // musicViews.albumArt.setTag(tag);
            // return albumId;
        }
    }

    public class MusicGenreHandler implements MusicHandler {
        @Override
        public View makeView(MusicView musicViews, LayoutInflater inflater) {
            View v = inflater.inflate(R.layout.list_item_music_simple, null);
            musicViews.title = (TextView) v.findViewById(R.id.SongName);
            musicViews.time = (TextView) v.findViewById(R.id.MusicTime);
            musicViews.albumArt = null;
            v.setTag(musicViews);
            return v;
        }

        @Override
        public long setView(MusicView musicViews, Context con, Cursor cursor) {
            final int indexGenreId = cursor.getColumnIndex(MediaStore.Audio.Genres._ID);
            final int indexGenre = cursor.getColumnIndex(MediaStore.Audio.Genres.NAME);
            // 対象画像のDB情報から、IDを取得
            String genre = cursor.getString(indexGenre);
            long genreId = cursor.getLong(indexGenreId);

            long[] albumCount = getAlbumIdAndCount(con, genreId);
            long albumId = albumCount[0];
            long count = albumCount[1];

            musicViews.title.setText(genre);
            musicViews.time.setText(String.valueOf(count));
            // musicViews.time.setText(0 + "");
            musicViews.albumId = albumId;
            return -1;
            // album artを識別するのに必要
            // musicViews.albumArt.setTag(tag);
            // return albumCount[0];
        }

        /** cursorにfilterをかける */
        private Cursor filteredCursor(Cursor c, Context con) {
            String cn_id = MediaStore.Audio.Genres._ID;
            String cn_name = MediaStore.Audio.Genres.NAME;
            MatrixCursor mc = new MatrixCursor(new String[] {
                    cn_id, cn_name
            });
            if (c.moveToFirst()) {
                String name;
                long id;
                long count;
                do {
                    id = c.getLong(c.getColumnIndex(cn_id));
                    name = c.getString(c.getColumnIndex(cn_name));
                    count = getAlbumIdAndCount(con, id)[1];
                    if (count > 0 || (name != null && name.length() > 0)) {
                        mc.addRow(new Object[] {
                                id, name
                        });
                    }
                } while (c.moveToNext());
            }
            c.close();
            return mc;
        }

        private long[] getAlbumIdAndCount(Context con, long genreId) {
            return AlbumArtUtil.getAlbumIdAndCount(con,
                    MediaStore.Audio.Genres.Members.getContentUri("external", genreId),
                    MediaStore.Audio.Media.IS_MUSIC + " = 1");
        }
    }
}
