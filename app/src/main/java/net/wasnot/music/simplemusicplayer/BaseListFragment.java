
package net.wasnot.music.simplemusicplayer;

import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * <a href="http://nkzn.hatenablog.jp/entry/2012/06/14/160706">ここ</a>
 * を参考にして作ったloadingつきlistfragment
 */
public abstract class BaseListFragment extends ListFragment {
    /** おまじないの材料１ */
    private static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;
    /** おまじないの材料２ */
    private static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;

    public void setListView(View view) {
        // ----------おまじないここから----------
        ProgressBar pBar = (ProgressBar) view.findViewById(android.R.id.progress);
        LinearLayout pframe = (LinearLayout) pBar.getParent();
        pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setItemsCanFocus(false);
        FrameLayout lFrame = (FrameLayout) listView.getParent();
        lFrame.setId(INTERNAL_LIST_CONTAINER_ID);
        // ----------おまじないここまで----------
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // Log.i("onListItemClick", "positon:" + position + ", tag:" +
        // v.getTag() + ", item:" + id);
    }

    @Override
    public void setEmptyText(CharSequence text) {
        // super.setEmptyText(text);
        TextView tv = (TextView) getListView().getEmptyView();
        tv.setText(text);
    }
}
