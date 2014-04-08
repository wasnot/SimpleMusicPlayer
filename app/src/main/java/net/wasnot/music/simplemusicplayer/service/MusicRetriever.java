/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wasnot.music.simplemusicplayer.service;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import net.wasnot.music.simplemusicplayer.list.AlbumArtUtil;
import net.wasnot.music.simplemusicplayer.list.AudioSelector;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Retrieves and organizes media to play. Before being used, you must call
 * {@link #prepare()}, which will retrieve all of the music on the user's device
 * (by performing a query on a content resolver). After that, it's ready to
 * retrieve a random song, with its title and URI, upon request.
 */
public class MusicRetriever {
    final String TAG = "MusicRetriever";

    private ContentResolver mContentResolver;
    private Cursor mCursor = null;
    private Uri mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    // private String mCategorySelection;
    private int mCategory = 0;
    private int mCategoryId = -1;
    // the items (songs) we have queried
    // private List<MusicItem> mItems = new ArrayList<MusicItem>();

    // 順番管理　song order
    private int mIndex = 0;
    private boolean isClosed = true;

    private boolean isShuffle = false;
    // 曲の行数 song track number (in cursor)
    private List<Integer> songNumberList;

    private int artistColumn;
    private int titleColumn;
    private int albumColumn;
    private int durationColumn;
    private int idColumn;
    private int albumIdColumn;
    private int pathColumn;

    public MusicRetriever(ContentResolver cr, boolean shuffle) {
        LogUtil.d(TAG, "retriever created");
        if (mCursor != null)
            close();
        mContentResolver = cr;
        isShuffle = shuffle;
    }

    public MusicRetriever(ContentResolver cr, Uri uri, int categoryId, int index, int category,
            boolean shuffle) {
        LogUtil.d(TAG, "retriever created, requested Uri");
        if (mCursor != null)
            close();
        mContentResolver = cr;
        mUri = uri;
        mIndex = index;
        mCategoryId = categoryId;
        mCategory = category;
        isShuffle = shuffle;
    }

    public void close() {
        isClosed = true;
        if (mCursor != null)
            mCursor.close();
        mCursor = null;
    }

    /**
     * Loads music data. This method may take long, so be sure to call it
     * asynchronously without blocking the main thread.
     */
    public int prepare() {
        LogUtil.i(TAG, "Querying media...");
        LogUtil.i(TAG, "URI: " + mUri.toString());

        String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
        String categorySelection = AudioSelector.getSelection(mCategory, mCategoryId);
        if (categorySelection != null) {
            selection += " AND " + categorySelection;
        }
        if (AudioSelector.isCategory(mCategory))
            selection = null;

        String sortOrder = AudioSelector.getSortOrder(mCategory);
        // if (limit > 0) {
        // sortOrder = MediaStore.Audio.AudioColumns._ID + " LIMIT " + limit;
        // if (offset >= 0)
        // sortOrder += " OFFSET " + offset;
        // }

        // Perform a query on the content resolver. The URI we're passing
        // specifies that we
        // want to query for all audio media on external storage (e.g. SD card)
        mCursor = mContentResolver.query(mUri, null, selection, null, sortOrder);
        LogUtil.i(TAG, "Query finished. "
                + (mCursor == null ? "Returned NULL." : "Returned a cursor."));

        if (mCursor == null) {
            // Query failed...
            LogUtil.e(TAG, "Failed to retrieve music: cursor is null :-(");
            return -1;
        }
        if (!mCursor.moveToFirst()) {
            // Nothing to query. There is no music on the device. How boring.
            LogUtil.e(TAG,
                    "Failed to move cursor to first row (no query results)." + mCursor.getCount());
            mCursor.close();
            mCursor = null;
            return -1;
        }
        // retrieve the indices of the columns where the ID, title, etc. of the
        // song are
        artistColumn = mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        titleColumn = mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        albumColumn = mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        durationColumn = mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        if (mCategory == AudioSelector.CATEGORY_PLAYLIST
                || mCategory == AudioSelector.CATEGORY_DETAILS_PLAYLIST)
            idColumn = mCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        else
            idColumn = mCursor.getColumnIndex(MediaStore.Audio.Media._ID);

        albumIdColumn = mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID);
        pathColumn = mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);

        isClosed = false;

        int size = mCursor.getCount();
        songNumberList = new ArrayList<Integer>(size);
        for (int i = 0; i < size; i++)
            songNumberList.add(i);
        setShuffle(isShuffle);

        return size;
    }

    private MusicItem makeItem(int index) {
        if (mCursor == null) {
            // Query failed...
            LogUtil.e(TAG, "Failed to retrieve music: cursor is null :-(");
            return null;
        }
        if (!mCursor.moveToPosition(index)) {
            // Nothing to query. There is no music on the device. How boring.
            LogUtil.e(TAG, "Failed to move cursor to first row (no query results).");
            return null;
        }
        LogUtil.i(TAG, "Listing...");

        LogUtil.i(TAG, "Title column index: " + String.valueOf(titleColumn));
        LogUtil.i(TAG, "ID column index: " + String.valueOf(titleColumn));

        // add each song to mItems
        LogUtil.i(TAG,
                "ID: " + mCursor.getString(idColumn) + " Title: " + mCursor.getString(titleColumn));
        MusicItem item = new MusicItem();
        item.setId(mCursor.getLong(idColumn));
        item.setTitle(mCursor.getString(titleColumn));
        item.setArtist(mCursor.getString(artistColumn));
        item.setAlbum(mCursor.getString(albumColumn));
        item.setAlbumArt(AlbumArtUtil.getAlbumArt(mContentResolver, mCursor.getLong(albumIdColumn)));
        // item.setAlbumArt(null);
        item.setPath(mCursor.getString(pathColumn));
        item.setDuration(mCursor.getLong(durationColumn));

        LogUtil.i(TAG, "Done querying media. MusicRetriever is ready.");
        return item;
    }

    // retriever中だとsizeが急にかわるので。。
    private boolean isSizeOk() {
        if (mCursor == null || mCursor.isClosed() || mCursor.getCount() <= 0)
            return false;
        if (mIndex >= mCursor.getCount() || songNumberList == null
                || mIndex >= songNumberList.size()
                || songNumberList.get(mIndex) >= mCursor.getCount())
            return false;
        return true;
    }

    public ContentResolver getContentResolver() {
        return mContentResolver;
    }

    public MusicItem getNowItem() {
        if (!isSizeOk())
            return null;
        return makeItem(songNumberList.get(mIndex));
    }

    /** Returns a random Item. If there are no items available, returns null. */
    public MusicItem getPrevItem() {
        if (!isSizeOk())
            return null;
        if (mIndex > 0)
            mIndex--;
        else
            mIndex = mCursor.getCount() - 1;
        return makeItem(songNumberList.get(mIndex));
    }

    public MusicItem getNextItem() {
        if (!isSizeOk())
            return null;
        if (mIndex < mCursor.getCount() - 1)
            mIndex++;
        else
            mIndex = 0;
        return makeItem(songNumberList.get(mIndex));
    }

    /** テスト用にしか使ってない。消したい */
    // public List<MusicItem> getItems() {
    // if (!isSizeOk())
    // return null;
    // return mItems;
    // }

    /** viewpager用 albumartlistを返す */
    public List<String> getAlbumArtList() {
        List<String> albumArtList = new ArrayList<String>();
        if (mCursor == null) {
            LogUtil.e(TAG, "Failed to get album art list: cursor is null :-(");
            return null;
        }
        if (mCursor.getCount() != songNumberList.size()) {
            LogUtil.e(TAG,
                    "Failed to get album art list: cursor count and song order count is not equal :-(");
            return null;
        }
        int count = songNumberList.size();
        for (int i = 0; i < count; i++) {
            if (!mCursor.moveToPosition(songNumberList.get(i))) {
                // Nothing to query. There is no music on the device. How
                // boring.
                LogUtil.e(TAG, "Failed to move cursor to first row (no query results).");
                return null;
            }
            albumArtList.add(AlbumArtUtil.getAlbumArt(mContentResolver,
                    mCursor.getLong(albumIdColumn)));
        }
        return albumArtList;
    }

    /** だめなindexでもかえす */
    public int getIndex() {
        return mIndex;
    }

    public boolean setIndex(int index) {
        if (mCursor != null && !mCursor.isClosed() && mCursor.getCount() > 0
                && index < mCursor.getCount()) {
            mIndex = index;
            return true;
        }
        return false;
    }

    public boolean isCursorEquals(Uri uri, int categoryId) {
        if (mUri.equals(uri)) {
            if (mCategoryId == categoryId) {
                return true;
            }
        }
        return false;
    }

    public boolean getShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean shuffle) {
        if (mCursor == null || mCursor.isClosed())
            return;
        isShuffle = shuffle;
        int nowSongNumber;
        // シャッフルする
        if (isShuffle) {
            nowSongNumber = songNumberList.remove(mIndex);
            Collections.shuffle(songNumberList);
            songNumberList.add(0, nowSongNumber);
            mIndex = 0;
        }
        // シャッフル戻す
        else {
            nowSongNumber = songNumberList.get(mIndex);
            int size = mCursor.getCount();
            songNumberList = new ArrayList<Integer>(size);
            for (int i = 0; i < size; i++)
                songNumberList.add(i);
            mIndex = nowSongNumber;
        }
    }

    public boolean isClosed() {
        return isClosed;
    }

    public boolean isEquals(MusicRetriever mr) {
        if (mr.mUri != null && mr.mUri.equals(this.mUri)) {
            if (this.songNumberList == null || mr.songNumberList == null
                    || this.songNumberList.size() != mr.songNumberList.size()) {
                return false;
            }
            int count = this.songNumberList.size();
            for (int i = 0; i < count; i++) {
                if (this.songNumberList.get(i) != mr.songNumberList.get(i))
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }

    protected List<Integer> getSongNumberList() {
        return songNumberList;
    }
}
