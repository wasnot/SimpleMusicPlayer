
package net.wasnot.music.simplemusicplayer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import net.wasnot.music.simplemusicplayer.R;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;

public class CircleView extends View {
    private final String TAG = this.getClass().getSimpleName();
    private final float START_ANGLE = 270F;
    private final float RADIUS_RATIO = 3.0f / 8.0f;
    private final float density = getResources().getDisplayMetrics().density;
    private final int STROKE_WIDTH = (int) (10F * density);

    private float mProgress;
    private float centerX;
    private float centerY;
    private float radius;
    private boolean isRevers = false;
    private boolean isProgressing = false;
    private OnCircleViewChangeListener mOnCircleViewChangeListener;

    public interface OnCircleViewChangeListener {
        public void onProgressChanged(CircleView circleView, float progress, boolean fromUser);

        public void onStartTrackingTouch(CircleView circleView);

        public void onChangeProgress(float progress);

        public void onStopTrackingTouch(CircleView circleView);

        public void onInterrupted(CircleView circleView);
    }

    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mProgress = 0.3F;
        // setFocusable(true);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyle) {
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
        // テキスト
        // setText(canvas, paint);
        // Album Art
        // setAlbumArt(canvas, paint);
        // 線で円を描く
        setArc(canvas, paint);
        // トグルボタンを描く
        // setThumb(canvas, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        LogUtil.i(TAG, "onSizeChanged, w:" + w + ", h:" + h);
    }

    public void setProgress(float progress) {
        // LogUtil.d(TAG, "setProgress progress:" + progress);
        mProgress = progress;
        if (mOnCircleViewChangeListener != null)
            mOnCircleViewChangeListener.onChangeProgress(progress);
        invalidate();
    }

    public void setOnCircleViewChangeListener(OnCircleViewChangeListener l) {
        mOnCircleViewChangeListener = l;
    }

    public void setRevers(boolean revers) {
        isRevers = revers;
        invalidate();
    }

    private void setArc(Canvas canvas, Paint paint) {
        // 囲い線
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setColor(getResources().getColor(R.color.player_highlight_blue));
        canvas.drawArc(getRect(), START_ANGLE, mProgress * 360, false, paint);
    }

    private RectF getRect() {
        return new RectF((centerX - radius), (centerY - radius), (centerX + radius),
                (centerY + radius));
    }

    // トグル用の突起
    private void setThumb(Canvas canvas, Paint paint) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.toggle_large);
        Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        int[] xy = getXY();
        int size = (int) (17F * density);
        Rect dist = new Rect(xy[0] - size, xy[1] - size, xy[0] + size, xy[1] + size);
        canvas.drawBitmap(bmp, src, dist, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float gap = 30F * density;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // LogUtil.d(TAG, "motion:down, (x,y):(" + x + "," + y + ")");
                gap = 30F * density;
                if (checkArc(x, y, gap)) {
                    // progress barを操作開始
                    isProgressing = true;
                    float progress = getAngle(x, y, radius + gap);
                    setProgress(progress);
                    if (mOnCircleViewChangeListener != null) {
                        mOnCircleViewChangeListener.onStartTrackingTouch(this);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // LogUtil.d(TAG, "motion:move, (x,y):(" + x + "," + y + ")");
                gap = 50F * density;
                if (checkArc(x, y, gap)) {
                    float progress = getAngle(x, y, radius + gap);
                    if (Math.abs(progress - mProgress) > 0.8)
                        return false;
                    setProgress(progress);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                // LogUtil.d(TAG, "motion:up, (x,y):(" + x + "," + y + ")");
                gap = 50F * density;
                if (checkArc(x, y, gap)) {
                    // progress barを操作終了
                    isProgressing = false;
                    float progress = getAngle(x, y, radius + gap);
                    // もし１２時のところを超えようとしてたら超えさせない。
                    if (Math.abs(progress - mProgress) > 0.8) {
                        if (mOnCircleViewChangeListener != null)
                            mOnCircleViewChangeListener.onInterrupted(this);
                        return false;
                    }
                    setProgress(progress);
                    if (mOnCircleViewChangeListener != null) {
                        mOnCircleViewChangeListener.onProgressChanged(this, progress, true);
                        mOnCircleViewChangeListener.onStopTrackingTouch(this);
                    }
                    return true;
                } else if (isProgressing) {
                    // progress barを操作終了
                    isProgressing = false;
                    if (mOnCircleViewChangeListener != null)
                        mOnCircleViewChangeListener.onInterrupted(this);
                }
                break;
        }
        return false;
    }

    private boolean checkArc(float x, float y, float gap) {
        float distance = (float) (Math.pow((double) centerX - x, 2) + Math.pow(
                (double) centerY - y, 2));
        // LogUtil.d(TAG, "distance:" + distance + ", radius:" + radius);
        if (distance < Math.pow(radius + gap, 2)) {
            if (distance > Math.pow(radius - gap, 2)) {
                // LogUtil.e(TAG, "around Arc");
                return true;
            }
        }
        return false;
    }

    private float getAngle(float x, float y, float maxRadius) {
        // 12時の方向が0度になるようにsin, cosを定義する
        float cos = (centerY - y) / maxRadius;
        float sin = (x - centerX) / maxRadius;
        double angle = Math.atan2(sin, cos);
        if (angle < 0)
            angle += Math.PI * 2;
        return (float) (angle / (Math.PI * 2));
    }

    // x,yを求める
    private int[] getXY() {
        int[] xy = new int[2];
        double angle = mProgress * 2 * Math.PI;
        xy[0] = (int) (centerX + radius * Math.sin(angle));
        xy[1] = (int) (centerY - radius * Math.cos(angle));
        // LogUtil.d(TAG, "xy," + Arrays.toString(xy) + ", Angle:" + angle +
        // ", sincon:" + Math.sin(angle)
        // + "," + Math.cos(angle));
        return xy;
    }

}
