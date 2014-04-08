
package net.wasnot.music.simplemusicplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;

import com.example.android.apis.animation.Rotate3dAnimation;

import net.wasnot.music.simplemusicplayer.R;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;

public class RotateHolder extends FrameLayout {
    private final String TAG = this.getClass().getSimpleName();
    private final float RADIUS_RATIO = 3.0f / 8.0f;
    private final float density = getResources().getDisplayMetrics().density;
    private final int STROKE_WIDTH = (int) (10F * density);
    private float centerX;
    private float centerY;
    private float radius;
    private CircleView circleView;
    private AlbumArtView albumArtView;

    private int DURATION = 300;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector mGestureDetector;
    private OnRoteteChangeListener mOnRotateChangeListener;

    public interface OnRoteteChangeListener {
        public void onFling(boolean isNext);

        public void onStartRotate(boolean isNext);

        public void onRotatingCenter(boolean isNext);

        public void onFinishRotate(boolean isNext);
    }

    public RotateHolder(Context context) {
        super(context);
    }

    public RotateHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RotateHolder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = getWidth();
        int height = getHeight();
        centerX = width / 2.0F;
        centerY = height / 2.0F;
        // 幅のRADIUS_RATIOか高さのあまりの小さい方にあわせる
        // radius = Math.min((float) width * RADIUS_RATIO,
        // ((float) height - 121F * 2.0F * density - STROKE_WIDTH) / 2.0F);
        radius = Math.min((float) width * RADIUS_RATIO, ((float) height - STROKE_WIDTH) / 2.0F);
        LogUtil.i(TAG, "onLayout changed:" + changed + ", l:" + left + ",t:" + top + ",r:" + right
                + ",b:" + bottom + ",w:" + getWidth() + ",h:" + getHeight());
        circleView = (CircleView) this.findViewById(R.id.circleView);
        albumArtView = (AlbumArtView) this.findViewById(R.id.albumArtView);
        mGestureDetector = new GestureDetector(this.getContext(), mOnGestureListener);
    }

    public void setOnGestureChangeListener(OnRoteteChangeListener l) {
        mOnRotateChangeListener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private final SimpleOnGestureListener mOnGestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent event) {
            // TODO Auto-generated method stub
            LogUtil.v("Gesture", "onDown");
            float x = event.getX();
            float y = event.getY();
            float gap = 30F * density;
            if (checkInCircle(x, y, -1 * (int) gap)) {
                return true;
                // rotate();
                // switchFrontBackView(true);
            }
            return super.onDown(event);
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX,
                float velocityY) {
            LogUtil.v("Gesture", "onFling");
            try {
                if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
                    // 縦の移動距離が大きすぎる場合は無視
                    return false;
                }
                if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    // 開始位置から終了位置の移動距離が指定値より大きい
                    // X軸の移動速度が指定値より大きい
                    LogUtil.d(TAG, "right -> left");
                    rotate(true);
                    if (mOnRotateChangeListener != null)
                        mOnRotateChangeListener.onFling(true);
                } else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    // 終了位置から開始位置の移動距離が指定値より大きい
                    // X軸の移動速度が指定値より大きい
                    LogUtil.d(TAG, "left -> right");
                    rotate(false);
                    if (mOnRotateChangeListener != null)
                        mOnRotateChangeListener.onFling(false);
                }
            } catch (Exception e) {
                // nothing
            }
            return super.onFling(event1, event2, velocityX, velocityY);
        }
    };

    private boolean checkInCircle(float x, float y, int gap) {
        float distance = (float) (Math.pow((double) centerX - x, 2) + Math.pow(
                (double) centerY - y, 2));
        // LogUtil.d(TAG, "distance:" + distance + ", radius:" + radius);
        if (distance < Math.pow(radius + gap, 2)) {
            // performClick();
            return true;
        }
        return false;
    }

    private void setChildrenRevers(boolean revers) {
        this.circleView.setRevers(revers);
        this.albumArtView.setRevers(revers);
    }

    private void rotate(boolean isNext) {
        LogUtil.i(TAG, "rotate!!");
        if (!isNext) {
            applyRotation(0f, 90f, 180f, 0f, isNext);
        } else {
            applyRotation(0f, -90f, -180f, 0f, isNext);
        }
    }

    /** startからendまでY軸回転する */
    private void applyRotation(float start, final float mid, final float end, final float depth,
            final boolean isNext) {
        Rotate3dAnimation rot = new Rotate3dAnimation(start, mid, centerX, centerY, depth, true);
        rot.setDuration(DURATION);
        // rot.setInterpolator(new AccelerateInterpolator());
        // rot.setAnimationListener(new DisplayNextView(mid, end, depth));
        rot.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mOnRotateChangeListener != null)
                    mOnRotateChangeListener.onRotatingCenter(isNext);
                // 90度回転したらviewを反転させる
                // RotateHolder.this.setAnimation(null);
                RotateHolder.this.setChildrenRevers(true);

                Rotate3dAnimation rot = new Rotate3dAnimation(mid, end, centerX, centerY, depth,
                        false);
                rot.setDuration(DURATION);
                rot.setInterpolator(new AccelerateInterpolator());
                rot.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // 180度回転させたらanimationを消して反転を元に戻す
                        // animationをnullにしないとちらつく
                        RotateHolder.this.setAnimation(null);
                        RotateHolder.this.setChildrenRevers(false);

                        if (mOnRotateChangeListener != null)
                            mOnRotateChangeListener.onFinishRotate(isNext);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                });
                RotateHolder.this.startAnimation(rot);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                if (mOnRotateChangeListener != null)
                    mOnRotateChangeListener.onStartRotate(isNext);
            }
        });
        this.startAnimation(rot);
    }

}
