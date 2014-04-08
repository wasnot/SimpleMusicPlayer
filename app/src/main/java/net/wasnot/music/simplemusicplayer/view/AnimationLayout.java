
package net.wasnot.music.simplemusicplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class AnimationLayout extends FrameLayout {

    // Scrollerの実装
    private Scroller mScroller;

    public AnimationLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScroller = new Scroller(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            animationStart();
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // Scrollerから移動位置を決定する
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    private void animationStart() {
        // 3000msで、座標を右下へ移動させる
        mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), -100, -100, 3000);
        invalidate();
    }

}
