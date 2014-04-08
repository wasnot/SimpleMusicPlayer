
package net.wasnot.music.simplemusicplayer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import net.wasnot.music.simplemusicplayer.R;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;


public class AlbumArtView extends View {
    private final String TAG = this.getClass().getSimpleName();
    // private final float START_ANGLE = 270F;
    private final float RADIUS_RATIO = 3.0f / 8.0f;
    private final float density = getResources().getDisplayMetrics().density;
    private final int STROKE_WIDTH = (int) (10F * density);

    private float centerX;
    private float centerY;
    private float radius;
    private String mAlbumArt;
    private Bitmap mBitmap = null;
    private boolean isRevers = false;

    public AlbumArtView(Context context) {
        super(context);
    }

    public AlbumArtView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumArtView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        centerX = width / 2.0F;
        centerY = height / 2.0F;
        // 幅のRADIUS_RATIOか高さのあまりの小さい方にあわせる
        // radius = Math.min((float) width * RADIUS_RATIO,
        // ((float) height - 121F * 2.0F * density - STROKE_WIDTH) / 2.0F);
        radius = Math.min((float) width * RADIUS_RATIO, ((float) height - STROKE_WIDTH) / 2.0F);

        if (isRevers)
            canvas.scale(-1f, 1f, centerX, centerY);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        // テキスト
        // setText(canvas, paint);
        // Album Art
        setAlbumArt(canvas, paint);
        // 線で円を描く
        // setArc(canvas, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        LogUtil.i(TAG, "onSizeChanged, w:" + w + ", h:" + h);
    }

    public void setAlbumArt(String albumArtPath) {
        mBitmap = null;
        mAlbumArt = albumArtPath;
        invalidate();
    }

    public void setAlbumArtBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        invalidate();
    }

    public void setRevers(boolean revers) {
        isRevers = revers;
        invalidate();
    }

    private void setText(Canvas canvas, Paint paint) {
        Typeface typeface = Typeface
                .createFromAsset(getContext().getAssets(), "GillSans-Light.ttf");
        paint.setTypeface(typeface);
        paint.setTextSize(100 * density);
        paint.setColor(Color.WHITE);
        canvas.drawText("test", 100 * density, 100 * density, paint);
    }

    private void setAlbumArt(Canvas canvas, Paint paint) {
        // String path = mItem != null ? mItem.getAlbumArt() : null;
        String path = mAlbumArt;
        // 曲からでコードできていたらそれ
        Bitmap image = mBitmap;
        // もしできていなければMediaStore
        if (image == null)
            image = BitmapFactory.decodeFile(path);
        // それもできなければなし。
        if (image == null)
            image = BitmapFactory.decodeResource(getResources(), R.drawable.music_empty_300);
        Bitmap bmp = clipCircle(image);
        Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        canvas.drawBitmap(bmp, src, getRect(), paint);
    }

    private Bitmap clipCircle(Bitmap image) {
        // Bitmap image = BitmapFactory.decodeResource(getResources(),
        // R.drawable.music_empty);
        int width = image.getWidth();
        int height = image.getHeight();

        Bitmap clipArea = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        RectF dst = new RectF(0, 0, width, height);
        // 角丸矩形を描写
        Canvas c = new Canvas(clipArea);
        c.drawRoundRect(dst, width / 2, height / 2, new Paint(Paint.ANTI_ALIAS_FLAG));
        // 角丸画像となるbitmap生成
        Bitmap newImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 切り取り領域を描写
        Canvas canvas = new Canvas(newImage);
        Paint paint = new Paint();
        canvas.drawBitmap(clipArea, 0, 0, paint);
        // 切り取り領域内にオリジナルの画像を描写
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Rect src = new Rect(0, 0, width, height);
        canvas.drawBitmap(image, src, dst, paint);
        return newImage;
    }

    private RectF getRect() {
        return new RectF((centerX - radius), (centerY - radius), (centerX + radius),
                (centerY + radius));
    }

}
