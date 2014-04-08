
package net.wasnot.music.simplemusicplayer.list;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import net.wasnot.music.simplemusicplayer.R;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;
import net.wasnot.music.simplemusicplayer.utli.MimeTypes;

import java.io.File;
import java.util.List;

import entagged.audioformats.AudioFile;
import entagged.audioformats.AudioFileIO;
import entagged.audioformats.Tag;
import entagged.audioformats.exceptions.CannotReadException;
import entagged.audioformats.exceptions.CannotWriteException;

public class MusicEditor {
    private final String TAG = this.getClass().getSimpleName();
    private Activity mActivity;
    private int mCategory;
    private String mTitle;

    public MusicEditor(Activity activity, int category, String title) {
        this.mActivity = activity;
        mCategory = category;
        mTitle = title;
    }

    public void editCategory(final Cursor cursor) {
        // cursorからcategoryIdを取り出す
        final long categoryId = AudioSelector.getCategoryId(cursor, mCategory);
        final String currentName = cursor.getString(cursor.getColumnIndex(AudioSelector
                .getCategoryNameColumn(mCategory)));

        // テキスト入力を受け付けるビューを作成します。
        final EditText editView = new EditText(mActivity);
        editView.setText(currentName);
        editView.setSingleLine(true);
        new AlertDialog.Builder(mActivity)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(
                        mActivity.getString(R.string.music_list_dialog_category_meta_edit_title,
                                mTitle))
                // setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = editView.getText().toString();
                        if (currentName.equals(newName)) {
                            Toast.makeText(mActivity, R.string.music_list_toast_meta_no_edited,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        ContentResolver contentResolver = mActivity.getContentResolver();
                        int result = -1;
                        // 登録データの作成
                        ContentValues contentvalues = new ContentValues();
                        contentvalues.put(AudioSelector.getCategoryNameColumn(mCategory), newName);
                        if (mCategory != AudioSelector.CATEGORY_GENRE) {
                            contentvalues.put(AudioSelector.getUpdateColumn(mCategory),
                                    String.valueOf((int) (System.currentTimeMillis() / 1000L)));
                        }
                        String as[] = new String[1];
                        as[0] = String.valueOf(categoryId);
                        String selection = AudioSelector.getCategoryIdColumn(mCategory) + " = ?";
                        // 変更
                        result = contentResolver.update(AudioSelector.getCategoryUri(mCategory),
                                contentvalues, selection, as);
                        LogUtil.d(TAG, selection + ":" + result);
                        int msgRes = R.string.music_list_toast_meta_edit_failed;
                        if (result > 0) {
                            msgRes = R.string.music_list_toast_meta_edited;
                            cursor.requery();
                        }
                        // 入力した文字をトースト出力する
                        Toast.makeText(mActivity, msgRes, Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }

    public void editSong(final Cursor cursor) {
        // テキスト入力を受け付けるビューを作成します。
        final View view = mActivity.getLayoutInflater().inflate(R.layout.dialog_music_edit, null,
                false);
        final long id = cursor.getLong(cursor.getColumnIndex(AudioSelector
                .getSongIdColumn(mCategory)));
        final String currentTitle = cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
        final String currentArtist = cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
        final String currentAlbum = cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
        final String currentTrack = cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.AudioColumns.TRACK));
        final String filePath = cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.AudioColumns.DATA));

        final EditText title = (EditText) view.findViewById(R.id.editTitle);
        title.setText(currentTitle);
        final EditText artist = (EditText) view.findViewById(R.id.editArtist);
        artist.setText(currentArtist);
        final EditText album = (EditText) view.findViewById(R.id.editAlbum);
        album.setText(currentAlbum);
        final EditText track = (EditText) view.findViewById(R.id.editTrack);
        track.setText(currentTrack);
        new AlertDialog.Builder(mActivity).setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.music_list_dialog_song_meta_edit_title)
                // setViewにてビューを設定します。
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newTitle = title.getText().toString();
                        String newArtist = artist.getText().toString();
                        String newAlbum = album.getText().toString();
                        String newTrack = track.getText().toString();
                        ContentResolver contentResolver = mActivity.getContentResolver();
                        ContentValues contentvalues = null;
                        String as[] = new String[1];
                        as[0] = String.valueOf(id);
                        int result = -1;
                        // 登録データの作成
                        contentvalues = new ContentValues();
                        if (!currentTitle.equals(newTitle))
                            contentvalues.put(MediaStore.Audio.AudioColumns.TITLE, newTitle);
                        if (!currentArtist.equals(newArtist))
                            contentvalues.put(MediaStore.Audio.AudioColumns.ARTIST, newArtist);
                        if (!currentAlbum.equals(newAlbum))
                            contentvalues.put(MediaStore.Audio.AudioColumns.ALBUM, newAlbum);
                        if (!currentTrack.equals(newTrack))
                            contentvalues.put(MediaStore.Audio.AudioColumns.TRACK, newTrack);
                        // 変更
                        if (contentvalues.size() <= 0) {
                            Toast.makeText(mActivity, R.string.music_list_toast_meta_no_edited,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        contentvalues.put(MediaStore.Audio.AudioColumns.DATE_MODIFIED,
                                String.valueOf((int) (System.currentTimeMillis() / 1000L)));
                        editRealFile(filePath, newTitle, newArtist, newAlbum, newTrack);
                        result = contentResolver.update(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentvalues,
                                MediaStore.Audio.AudioColumns._ID + " = ?", as);
                        int msgRes = R.string.music_list_toast_meta_edit_failed;
                        if (result > 0) {
                            msgRes = R.string.music_list_toast_meta_edited;
                            cursor.requery();
                        }
                        // 入力した文字をトースト出力する
                        Toast.makeText(mActivity, msgRes, Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void editRealFile(String filePath, String newTitle, String newArtist, String newAlbum,
            String newTrack) {
        String type = MimeTypes.GetMIMEType(filePath);
        if (type.contains("mp3") || type.contains("mpeg") || type.contains("mpg")) {
            // Reads the given file.
            AudioFile audioFile;
            try {
                audioFile = AudioFileIO.read(new File(filePath));
                Tag tag = audioFile.getTag();

                List<String> titles = tag.getTitle();
                if (newTitle != null && !titles.contains(newTitle))
                    tag.setTitle(newTitle);

                List<String> artists = tag.getArtist();
                if (newArtist != null && !artists.contains(newArtist))
                    tag.setArtist(newArtist);

                List<String> albums = tag.getAlbum();
                if (newAlbum != null && !albums.contains(newAlbum))
                    tag.setAlbum(newAlbum);

                List<String> tracks = tag.getTrack();
                if (newTrack != null && !tracks.contains(newTrack))
                    tag.setTrack(newTrack);
                // Sets the genre to Prog. Rock, note the file on disk is still
                // unmodified.
                // tag.setGenre("Progressive Rock");
                // Write the modifications in the file on disk.
                AudioFileIO.write(audioFile);
            } catch (CannotReadException e) {
                e.printStackTrace();
            } catch (CannotWriteException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.e(TAG, "cannot edit!!");
        }

    }

}
