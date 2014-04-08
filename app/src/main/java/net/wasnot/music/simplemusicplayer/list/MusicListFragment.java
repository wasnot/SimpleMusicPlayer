package net.wasnot.music.simplemusicplayer.list;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.wasnot.music.simplemusicplayer.BaseListFragment;
import net.wasnot.music.simplemusicplayer.MusicActivity;
import net.wasnot.music.simplemusicplayer.PlayerActivity;
import net.wasnot.music.simplemusicplayer.R;
import net.wasnot.music.simplemusicplayer.image.AlbumArtTask;
import net.wasnot.music.simplemusicplayer.service.PlayerService;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;

public class MusicListFragment extends BaseListFragment implements OnItemLongClickListener,
        LoaderCallbacks<Cursor> {
    private static final String TAG = MusicListFragment.class.getSimpleName();
    public static final String ARG_TITLE = "title";
    public static final String ARG_URI = "uri";
    public static final String ARG_CATEGORY = "category";
    protected static final String ARG_CATEGORY_ID = "categoryId";
    protected static final String ARG_DETAIL_TITLE = "detail_title";
    protected static final String ARG_DETAIL_SUBTITLE = "detail_subtitle";
    protected static final String ARG_DETAIL_ALBUMID = "detail_albumid";
    final boolean isLoader = false;
    final boolean isRequery = true;

    private int size;
    private Uri mUri;
    private String mTitle;
    private String mDetailTitle = "";
    private int mCategory;
    private int mCategoryId = -1;
    private int mCount = 0;
    private MusicListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        if (arg != null && arg.containsKey(ARG_CATEGORY) && arg.containsKey(ARG_TITLE)
                && arg.containsKey(ARG_URI)) {
            // LogUtil.e(TAG, "item:" + mItem.title);
            mUri = Uri.parse(arg.getString(ARG_URI));
            mCategory = arg.getInt(ARG_CATEGORY);
            mTitle = arg.getString(ARG_TITLE);
            mDetailTitle = arg.getString(ARG_DETAIL_TITLE);
            LogUtil.e(TAG, "item:" + mTitle);
            if (arg.containsKey(ARG_CATEGORY_ID))
                mCategoryId = arg.getInt(ARG_CATEGORY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_default_list, container, false);
        super.setListView(v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogUtil.e(TAG, "onActivityCreated, " + this.getListAdapter());
        super.onActivityCreated(savedInstanceState);
//        MainActivity activity = (MainActivity) getActivity();
//        if (AudioSelector.isDetails(mCategory)) {
//            activity.setActionBarCustomTitleNoIcon(mDetailTitle);
//        } else {
//            activity.setActionBarCustomTitleNoIcon(getString(R.string.menu_music));
//        }
//        activity.setSlidingMenuAllTouchMode(false);

        // 既にsetAdapterされているとsetHeaderできないのでnullを入れる。
        if (!AudioSelector.isCategory(mCategory)) {
            setListAdapter(null);
            setDetailHeader();
        }
        try {
            View v = getActivity().getLayoutInflater().inflate(R.layout.list_footer_music_detail,
                    null, false);
            getListView().addFooterView(v, null, false);
        } catch (Exception e) {
            // TODO なんで頭ないけど戻ってきたときにエラーが起きる 2.3で。
            e.printStackTrace();
            LogUtil.e(TAG, "addFooterError " + AudioSelector.getCategoryTypeName(mCategory));
        }
        getListView().setOnItemLongClickListener(this);
        getListView().setFastScrollEnabled(true);

        size = (int) (getResources().getDimensionPixelSize(R.dimen.silkhat_list_album_size));
        if (!isLoader) {
            mAdapter = makeListAdapter(getActivity().getApplicationContext());
            setListAdapter(mAdapter);
            if (mCategory == AudioSelector.CATEGORY_SONGS) {
                getListView().setSelection(1);
                TextView text = (TextView) getListView().findViewById(R.id.row_title);
                if (text != null) {
                    if (mAdapter != null) {
                        text.setText(this.getString(R.string.player_list_shuffle_count,
                                mAdapter.getCount()));
                    } else {
                        text.setText(R.string.player_list_shuffle);
                    }
                }
            }
        } else {
            // setListAdapter(null);
//            getActivity().getSupportLoaderManager().initLoader(
//                    MainActivity.LOADER_ID_MUSIC + mCategory, null, this);
        }

        // getListView().scrollTo(0, 500);
        // getListView().setSelectionFromTop(1, 0);
        // getListView().setSelectionAfterHeaderView();
        // スクロール時の処理
        getListView().setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstItem, int visibleItem,
                                 int totalItemCount) {
                // LogUtil.d(TAG, "onScroll, total:" + totalItemCount +
                // ", first:" +
                // firstItem
                // + ", visible:" + visibleItem);
                // // 現在のリストアイテム数
                // int presentItemCount = ListAdapter.getCount();
                // // 前回よりアイテムが増えていたらスクロール処理
                // if (mPrevItemCount < presentItemCount) {
                // if ((firstItem + visibleItem) == (presentItemCount) &&
                // firstItem != 0) {
                // mLoadingText.setVisibility(View.VISIBLE);
                // mLoadingText.setText("読み込み中...");
                // }
                // }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // LogUtil.d(TAG, mTitle + ", onScrollStateChanged " +
                // scrollState);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
        if (getActivity() != null && isLoader && isRequery) {
//            getActivity().getSupportLoaderManager().restartLoader(
//                    MainActivity.LOADER_ID_MUSIC + mCategory, null, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.d(TAG, "onPause");
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            // LogUtil.e(TAG, "visible");
            boolean isDetail = AudioSelector.isDetails(mCategory);
            boolean isCategory = AudioSelector.isCategory(mCategory);
            String category = AudioSelector.getCategoryTypeName(mCategory);
            MusicActivity activity = (MusicActivity) getActivity();
//            if (activity != null)
//                activity.getSensing().trackAccessPage("music",
//                        (isCategory || isDetail) ? category : "list");
            if (getActivity() != null && isLoader && isRequery) {
//                getActivity().getSupportLoaderManager().restartLoader(
//                        MainActivity.LOADER_ID_MUSIC + mCategory, null, this);
            }
        }
    }

    @Override
    public void onDestroyView() {
        // list.setAdapter(null);
        super.onDestroyView();
        if (mAdapter != null) {
            Cursor cursor = mAdapter.getCursor();
            LogUtil.d(TAG, "onDestroy, cursor:" + cursor);
            if (cursor != null)
                cursor.close();
        }
        if (AudioSelector.isDetails(mCategory)) {
//            MainActivity activity = (MainActivity) getActivity();
//            activity.setActionBarCustomTitleNoIcon(getString(R.string.menu_music));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        LogUtil.i("onListItemClick", "positon:" + position + ", tag:" + v.getTag() + ", item:" + id);
        Cursor c = (Cursor) l.getItemAtPosition(position);
        LogUtil.d(TAG, "c:" + c);
        // LogUtil.d(TAG, "count" + c.getCount());
        // for (int i = 0; i < c.getColumnCount(); i++)
        // LogUtil.d(TAG, "cursor:" + i + ", " + c.getColumnName(i) + " : " +
        // c.getString(i));
        // l.setSmoothScrollbarEnabled(true);
        // l.smoothScrollToPosition(100);// 先頭に移動
        // l.scrollTo(0, position);
        // l.setSelection(position);
        // int last = l.getCount();//Listのアイテム数を取得
        // l.setSelection(last);//最後の行に移動

        if (!AudioSelector.isCategory(mCategory)) {
            if (isAdded() && getActivity() != null) {
                int index = c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                String name = "unknown";
                if (index != -1)
                    name = c.getString(index);
//                ((MainActivity) getActivity()).getSensing().trackFileOpen("music",
//                        AudioSelector.getCategoryTypeName(mCategory), name);
            }
            int song_num = position;
            // headerを足しているのでheader分引く
            song_num--;
            sendIntent(AudioSelector.getCategoryId(c, mCategory), song_num, false);
            Intent intent = new Intent(this.getActivity().getApplicationContext(),
                    PlayerActivity.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.player_fragment_open_enter,
                    R.anim.player_fragment_open_exit);
        } else {
            Activity a = getActivity();
            if (a instanceof MusicActivity) {
                MusicActivity activity = (MusicActivity) a;
            activity.switchContent(makeDetailList(AudioSelector.getCategoryId(c, mCategory),
                    v.getTag()));
            } else if (a instanceof MusicActivity) {
MusicActivity activity =(MusicActivity)a;
                activity.switchContent(makeDetailList(AudioSelector.getCategoryId(c, mCategory),
                        v.getTag()));
                this.getChildFragmentManager().beginTransaction()
                        .replace(R.id.content, makeDetailList(AudioSelector.getCategoryId(c, mCategory), v.getTag()))
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        LogUtil.d(TAG, "parent:" + parent.getItemAtPosition(position) + ", view:" + view + ", tag:"
                + view.getTag() + ", position:" + position + ", id:" + id);
        Cursor c = (Cursor) parent.getItemAtPosition(position);
        if (AudioSelector.isCategory(mCategory)) {
            MusicEditor editor = new MusicEditor(getActivity(), mCategory, mTitle);
            editor.editCategory(c);
            return true;
        } else if (!AudioSelector.isCategory(mCategory)) {
            MusicEditor editor = new MusicEditor(getActivity(), mCategory, mTitle);
            editor.editSong(c);
            return true;
        }
        return false;
    }

    @Override
    public void setEmptyText(CharSequence text) {
        super.setEmptyText(text);
        TextView tv = (TextView) getListView().getEmptyView();
        tv.setText(text);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if (getActivity() == null || !isAdded())
            return null;
        // setListShown(false);
        // ContentResolver cr = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
        String categorySelection = AudioSelector.getSelection(mCategory, mCategoryId);
        String sortOrder = AudioSelector.getSortOrder(mCategory);
        if (mUri != null) {
            uri = mUri;
            LogUtil.d(TAG, "URI:" + uri);
            if (categorySelection != null)
                selection += " AND " + categorySelection;

            // genreはis_musicのcountが0で名前がないものはのぞく
            // TODO countはちぇっくできてない。すると時間かかるからどうしよう。
            if (AudioSelector.CATEGORY_GENRE == mCategory) {
                selection = " LENGTH( " + MediaStore.Audio.GenresColumns.NAME + " ) > 0 ";
            }
            // ただのカテゴリーは全部表示
            else if (AudioSelector.isCategory(mCategory)) {
                selection = null;
            }
        }
        LogUtil.i(TAG, "uri: " + uri + ", selection: " + selection + ", sortorder:" + sortOrder);
        return new CursorLoader(getActivity().getApplicationContext(), uri, null, selection, null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        if (getActivity() == null || !isAdded())
            return;
        MusicViewManager.MusicHandler musicHandler = null;
        MusicViewManager mm = new MusicViewManager();
        musicHandler = mm.getMusicHandler(mCategory);
        Context con = getActivity().getApplicationContext();
        if (c != null && !c.isClosed()) {
            // genreはis_musicのcountが0で名前がないものはのぞく
            if (AudioSelector.CATEGORY_GENRE == mCategory) {
                // c = ((MusicGenreHandler) musicHandler).filteredCursor(c,
                // con);
            }
            mCount = c.getCount();
            LogUtil.d(TAG, "count:" + mCount);
            mAdapter = new MusicListAdapter(con, c, musicHandler,
                    CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        }

        // cursor.close();
        setListAdapter(mAdapter);
        setListShown(true);
        if (mCategory == AudioSelector.CATEGORY_SONGS)
            getListView().setSelection(1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        if (isAdded()) {
            // setEmptyText("");
        }
    }

    private void sendIntent(int id, int position, boolean isShuffle) {
        Uri uri = AudioSelector.getSongUri(mCategory, id);
        // String categorySelection = AudioSelector.getSelection(mCategory, id);
        // String columnName = null;
        // int categoryId = -1;
        int index = 0;
        if (!AudioSelector.isCategory(mCategory))
            index = position;
        Intent intent = new Intent(PlayerService.ACTION_FORCE_RETRIEVER, null, getActivity()
                .getApplicationContext(), PlayerService.class);
        intent.putExtra(AudioSelector.INTENT_PARAM_URI, uri);
        intent.putExtra(AudioSelector.INTENT_PARAM_CATEGORY_ID, id);
        intent.putExtra(AudioSelector.INTENT_PARAM_SONG_INDEX, index);
        intent.putExtra(AudioSelector.INTENT_PARAM_CATEGORY, mCategory);
        if (isShuffle)
            intent.putExtra(AudioSelector.INTENT_PARAM_IS_SHUFFLE, isShuffle);
        getActivity().startService(intent);
    }

    private MusicListAdapter makeListAdapter(Context con) {
        ContentResolver cr = con.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        MusicViewManager.MusicHandler musicHandler = null;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
        String categorySelection = AudioSelector.getSelection(mCategory, mCategoryId);
        String sortOrder = AudioSelector.getSortOrder(mCategory);
        if (mUri != null) {
            uri = mUri;
            LogUtil.d(TAG, "URI:" + uri);
            MusicViewManager mm = new MusicViewManager();
            musicHandler = mm.getMusicHandler(mCategory);
            if (categorySelection != null)
                selection += " AND " + categorySelection;

            // genreはis_musicのcountが0で名前がないものはのぞく
            // TODO countはちぇっくできてない。すると時間かかるからどうしよう。
            if (AudioSelector.CATEGORY_GENRE == mCategory) {
                selection = " LENGTH( " + MediaStore.Audio.GenresColumns.NAME + " ) > 0 ";
            }
            // ただのカテゴリーは全部表示
            else if (AudioSelector.isCategory(mCategory)) {
                selection = null;
            }
        }
        LogUtil.i(TAG, "uri: " + uri + ", selection: " + selection + ", sortorder:" + sortOrder);
        // mCursor = cr.query(uri, null, selection, null, sortOrder);
        // if (mCursor != null)
        // LogUtil.d(TAG, "count:" + mCursor.getCount());
        Cursor c = cr.query(uri, null, selection, null, sortOrder);
        if (c != null) {
            // genreはis_musicのcountが0で名前がないものはのぞく
            if (AudioSelector.CATEGORY_GENRE == mCategory) {
                // c = ((MusicGenreHandler) musicHandler).filteredCursor(c,
                // con);
            }
            mCount = c.getCount();
            LogUtil.d(TAG, "count:" + mCount);
        }

        MusicListAdapter listAdapter = new MusicListAdapter(con, c, musicHandler,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        // cursor.close();
        return listAdapter;
    }

    private MusicListFragment makeDetailList(int categoryId, Object tag) {
        String title = "";
        String subTitle = "";
        long albumId = -1;
        if (tag instanceof MusicViewManager.MusicView) {
            title = ((MusicViewManager.MusicView) tag).title.getText().toString();
            TextView subText = ((MusicViewManager.MusicView) tag).subTitle;
            if (subText != null)
                subTitle = ((MusicViewManager.MusicView) tag).subTitle.getText().toString();
            albumId = ((MusicViewManager.MusicView) tag).albumId;
        }
        MusicListFragment fragment = new MusicListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_CATEGORY, AudioSelector.getDetailCategory(mCategory));
        bundle.putString(ARG_TITLE, "detail");
        bundle.putString(ARG_URI, AudioSelector.getSongUri(mCategory, categoryId).toString());
        bundle.putInt(ARG_CATEGORY_ID, categoryId);
        bundle.putString(ARG_DETAIL_TITLE, title);
        bundle.putString(ARG_DETAIL_SUBTITLE, subTitle);
        bundle.putLong(ARG_DETAIL_ALBUMID, albumId);
        fragment.setArguments(bundle);
        return fragment;
    }

    private void setDetailHeader() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View header;
        // detailsの場合
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_DETAIL_TITLE)
                && args.containsKey(ARG_DETAIL_SUBTITLE) && args.containsKey(ARG_DETAIL_ALBUMID)) {
            header = inflater.inflate(R.layout.list_header_music_detail, null, false);
            final Context con = getActivity().getApplicationContext();
            ImageView categoryAlbumArt = (ImageView) header.findViewById(R.id.CategoryAlbumArt);
            categoryAlbumArt.setTag("categoryAlbumArt");
            AlbumArtTask task = new AlbumArtTask(con, categoryAlbumArt,
                    args.getLong(ARG_DETAIL_ALBUMID));
            task.execute();
            TextView categoryTitle = (TextView) header.findViewById(R.id.CategoryTitle);
            categoryTitle.setText(args.getString(ARG_DETAIL_TITLE));
            TextView categorySubtitle = (TextView) header.findViewById(R.id.CategorySubtitle);
            categorySubtitle.setText(args.getString(ARG_DETAIL_SUBTITLE));
            header.findViewById(R.id.shuffleStartButton).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    double startIndex = Math.floor(Math.random() * mCount);
                    LogUtil.d(TAG, "onClick shuffleBtn,  " + startIndex);
                    sendIntent(mCategoryId, (int) startIndex, true);
                    Intent intent = new Intent(con, PlayerActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.player_fragment_open_enter,
                            R.anim.player_fragment_open_exit);
                }
            });
        }
        // songsの場合
        else {
            header = inflater.inflate(R.layout.list_item_menu, null, false);
            ((ImageView) header.findViewById(R.id.row_icon))
                    .setImageResource(R.drawable.list_shuffle);
            TextView text = (TextView) header.findViewById(R.id.row_title);
            // if (mAdapter != null) {
            // text.setText(this.getString(R.string.player_list_shuffle_count,
            // mAdapter.getCount()));
            // } else {
            // text.setText(R.string.player_list_shuffle);
            // }
            text.setTextColor(getResources().getColor(R.color.player_list_header_text));
            header.setBackgroundResource(R.drawable.list_item_even);
            header.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    double startIndex = Math.floor(Math.random() * mCount);
                    LogUtil.d(TAG, "onClick shuffleBtn,  " + startIndex);
                    sendIntent(mCategoryId, (int) startIndex, true);
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            PlayerActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.player_fragment_open_enter,
                            R.anim.player_fragment_open_exit);
                }
            });
        }
        getListView().addHeaderView(header, null, false);
    }


    public class MusicListAdapter extends CursorAdapter {
        LayoutInflater mInflater;
        MusicViewManager.MusicHandler mHandler;

        // Resources mResource = MusicListFragment.this.getResources();

        public MusicListAdapter(Context context, Cursor c, MusicViewManager.MusicHandler handler, int flags) {
            super(context, c, flags);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mHandler = handler;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            // 背景の色を変える
            if (position % 2 == 0) {
                view.setBackgroundResource(R.drawable.list_item_even);
            } else {
                view.setBackgroundResource(R.drawable.list_item_odd);
            }
            // LogUtil.d(TAG, "tag:" + view.getTag() + "");
            return view;
        }

        @Override
        public void bindView(View view, Context con, Cursor cursor) {
            MusicViewManager.MusicView musicView = (MusicViewManager.MusicView) view.getTag();
            long albumId = mHandler.setView(musicView, con, cursor);
            if (musicView.albumArt != null) {
                // サムネイル画像の取得を非同期で実行
                AlbumArtTask task = new AlbumArtTask(con, getListView(), cursor.getPosition(),
                        musicView.albumArt, albumId, size);
                // if (Build.VERSION.SDK_INT >= 11) {
                // task.executeOnExecutor();
                // }
                task.execute();
                // mTask = new AlbumArtTask(musicView.albumArt, albumId, con);
                // mTask.execute();
            }
        }

        @Override
        public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
            MusicViewManager.MusicView musicView = new MusicViewManager.MusicView();
            return mHandler.makeView(musicView, mInflater);
        }

    }

}
