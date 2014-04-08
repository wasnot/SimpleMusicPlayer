
package net.wasnot.music.simplemusicplayer.image;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.AbsListView;
import android.widget.ImageView;

import net.wasnot.music.simplemusicplayer.R;
import net.wasnot.music.simplemusicplayer.list.AlbumArtUtil;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;

public class AlbumArtTask extends AsyncTask<Void, Void, Bitmap> {
    Context mCon;
    private final WeakReference<ImageView> mImageViewReference;
    private final WeakReference<AbsListView> mListViewReference;
    private final int mSize;
    private final int mPosition;
    String mTag;
    long mAlbumId;

    public AlbumArtTask(Context con, AbsListView listView, int position, ImageView imageView,
            long albumId, int size) {
        mCon = con;
        mImageViewReference = new WeakReference<ImageView>(imageView);
        mListViewReference = new WeakReference<AbsListView>(listView);
        mPosition = position;
        mSize = size;
        // 以前の画像が残っているため、ImageViewの表示内容を適当に初期化
        imageView.setImageResource(R.drawable.music_empty_96);
        mTag = (String) imageView.getTag();
        mAlbumId = albumId;
    }

    public AlbumArtTask(Context con, ImageView imageView, long albumId, int size) {
        mCon = con;
        mImageViewReference = new WeakReference<ImageView>(imageView);
        mListViewReference = null;
        mPosition = -1;
        mSize = size;
        // 以前の画像が残っているため、ImageViewの表示内容を適当に初期化
        imageView.setImageResource(R.drawable.music_empty_96);
        mTag = (String) imageView.getTag();
        mAlbumId = albumId;
    }

    public AlbumArtTask(Context con, ImageView imageView, long albumId) {
        mCon = con;
        mImageViewReference = new WeakReference<ImageView>(imageView);
        mListViewReference = null;
        mPosition = -1;
        mSize = (int) con.getResources().getDimension(R.dimen.silkhat_list_header_album_size);
        // 以前の画像が残っているため、ImageViewの表示内容を適当に初期化
        imageView.setImageResource(R.drawable.music_empty_300);
        mTag = (String) imageView.getTag();
        mAlbumId = albumId;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        if (this.isCancelled())
            return null;
        ImageView m = mImageViewReference.get();
        if (mListViewReference == null) {
            // listViewがないときは特にチェックしない
        } else if (m != null && m.getTag() != null && m.getTag().equals(mTag)) {
            AbsListView l = mListViewReference.get();
            // TODO　本当にこの数字でいいのか？？
            if (mPosition <= l.getFirstVisiblePosition() - 24
                    || l.getLastVisiblePosition() + 24 <= mPosition) {
                LogUtil.e(
                        "doInBackground",
                        "first:" + l.getFirstVisiblePosition() + ", last:"
                                + l.getLastVisiblePosition() + ", " + mPosition);
                this.cancel(false);
                return null;
            } else {
                // LogUtil.i("doInBackground", "imageview:" + m + ", tag:" +
                // m.getTag());
            }
        } else {
            LogUtil.e("doInBackground", "imageview:" + m + ", tag:"
                    + (m != null ? m.getTag() : null));
            this.cancel(false);
            return null;
        }

        String filePath = AlbumArtUtil.getAlbumArt(mCon, mAlbumId);
        Bitmap bmp = AlbumArtCache.getImage(filePath);
        if (bmp == null) {
            bmp = makeBitmap(filePath);
            AlbumArtCache.setImage(filePath, bmp);
        }
        return bmp;

    }

    @Override
    protected void onPostExecute(Bitmap result) {
        // キャンセルされていたらなにもしない
        if (isCancelled()) {
            result = null;
        }
        // 非同期処理中にListViewをスクロールさせていくと、
        // 設定先ImageViewの対象画像が別なものに設定される場合がある。
        // そのため、非同期処理したの画像 と 設定先ImageViewが現時点で対象としている画像の識別子を比較。
        // 一致しているならば、ImageViewに取得したサムネイル画像をSetする。
        if (mImageViewReference != null && result != null) {
            final ImageView imageView = mImageViewReference.get();
            if (imageView != null && mTag != null && mTag.equals(imageView.getTag())) {
                imageView.setImageBitmap(result);
            }
        }
    }

    private Bitmap makeBitmap(String filePath) {
        // 画像IDに対応するサムネイル画像のBitmapを取得
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opt);
        // px size calculate

        opt.inSampleSize = calculateInSampleSize(opt, mSize, mSize);
        opt.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, opt);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = (int) Math.floor((float) height / (float) reqHeight);
            } else {
                inSampleSize = (int) Math.floor((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

}
