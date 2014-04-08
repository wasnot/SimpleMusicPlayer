
package net.wasnot.music.simplemusicplayer.list;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wasnot.music.simplemusicplayer.PreferenceUtil;
import net.wasnot.music.simplemusicplayer.R;
import net.wasnot.music.simplemusicplayer.utli.LogUtil;

import java.util.ArrayList;

public class MusicPagerFragment extends Fragment {
    private final String TAG = MusicPagerFragment.class.getSimpleName();
    // private static final String STATE_SELECTED_NAVIGATION_ITEM =
    // "selected_navigation_item";
    // private Handler mHandler = new Handler();
    // private PlayerService mService = null;

    /**
     * The fragment argument representing the section number for this fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";
    private ViewPager mViewPager;
    private MusicPagerAdapter mPagerAdapter;

    // private int currentPosition;

    public MusicPagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.i(TAG, "ondestroyview");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.i(TAG, "oncreateview," + mViewPager + ", " + mPagerAdapter);

        View view = inflater.inflate(R.layout.fragment_default_pager, container, false);
        mPagerAdapter = new MusicPagerAdapter(this.getChildFragmentManager());
        mViewPager = (ViewPager) view.findViewById(R.id.fragmentPager);
        mViewPager.setPageMargin((int) getResources().getDimension(
                R.dimen.silkhat_view_pager_side_padding));

        // 各ページアイテム(songs)
        PageItem songs = new PageItem();
        songs.title = getString(R.string.music_page_title_songs);
        songs.uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();
        songs.category = AudioSelector.CATEGORY_SONGS;
        mPagerAdapter.addItem(songs);
        // 各ページアイテム(playlist)
        PageItem playlist = new PageItem();
        playlist.title = getString(R.string.music_page_title_playlist);
        playlist.uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI.toString();
        playlist.category = AudioSelector.CATEGORY_PLAYLIST;
        mPagerAdapter.addItem(playlist);
        // 各ページアイテム(album)
        PageItem album = new PageItem();
        album.title = getString(R.string.music_page_title_album);
        album.uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.toString();
        album.category = AudioSelector.CATEGORY_ALBUM;
        mPagerAdapter.addItem(album);
        // 各ページアイテム(artist)
        PageItem artist = new PageItem();
        artist.title = getString(R.string.music_page_title_artist);
        artist.uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI.toString();
        artist.category = AudioSelector.CATEGORY_ARTIST;
        mPagerAdapter.addItem(artist);
        // 各ページアイテム(genre)
        PageItem genre = new PageItem();
        genre.title = getString(R.string.music_page_title_genre);
        genre.uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.toString();
        genre.category = AudioSelector.CATEGORY_GENRE;
        mPagerAdapter.addItem(genre);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(inflater.getContext());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(settings.getInt(PreferenceUtil.SAVED_PAGE_MUSIC, 0));
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int value) {
                settings.edit().putInt(PreferenceUtil.SAVED_PAGE_MUSIC, value).commit();
            }
        });
        Fragment f = mPagerAdapter.getItem(0);
        // LogUtil.d(TAG, "fragment:" + f + ",isnull:");
        // mPagerAdapter.notifyDataSetChanged();

        // PagerTabStrip のカスタマイズ
        // float density = this.getResources().getDisplayMetrics().density;
        PagerTabStrip strip = (PagerTabStrip) view.findViewById(R.id.pagerTabStrip);
        // strip.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16 * density);
        strip.setTextSpacing(0);
        // int paddingTop = (int) (5 * density);
        // int paddingBottom = (int) (5 * density);
        // strip.setPadding(0, paddingTop, 0, paddingBottom);
        // strip.setTextColor(getResources().getColor(R.color.player_list_pager_title_text));
        strip.setNonPrimaryAlpha(1.0f);
        // strip.setGravity(Gravity.BOTTOM);
        strip.setTabIndicatorColorResource(R.color.player_list_pager_bg);
        strip.setDrawFullUnderline(false);

        // FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0,0 );
        // ImageView shadow = (ImageView) view.findViewById(R.id.shadowLeft);
        // shadow.setOnTouchListener(mListener);
        // ImageView shadowr = (ImageView) view.findViewById(R.id.shadowLeft);
        // shadowr.setOnTouchListener(mListener);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        MainActivity activity = (MainActivity) getActivity();
//        activity.setActionBarCustomTitleNoIcon(getString(R.string.menu_music));
//        activity.setSlidingMenuAllTouchMode(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
        // ((MainActivity)
        // getActivity()).setServiceBindListener(mServiceBindListener);
        // if (mService != null && mService.getItem() != null) {
        // // activityが先に死ぬとエラーになるので。。
        // if (getActivity().getSupportFragmentManager() == null)
        // return;
        // changePlayingLayout(mService.getItem(), mService.getState());
        // }
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.d(TAG, "onPause");
        // ((MainActivity)
        // getActivity()).removeServiceBindListener(mServiceBindListener);
    }

    public class MusicPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<PageItem> mList;

        public MusicPagerAdapter(FragmentManager fm) {
            super(fm);
            LogUtil.d(TAG, "adapter create");
            mList = new ArrayList<PageItem>();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            /*
             * このpositionに表示するViewの番号が来る 初期表示は0で右にスワイプするごとにインクリメントみたいな
             * 毎回newせずにコンストラクタでnewして渡すほうがいいはず
             */
            fragment = new MusicListFragment();
            PageItem item = mList.get(position);
            Bundle bundle = new Bundle();
            bundle.putInt(MusicListFragment.ARG_CATEGORY, item.category);
            bundle.putString(MusicListFragment.ARG_TITLE, item.title);
            bundle.putString(MusicListFragment.ARG_URI, item.uri);
            // bundle.putSerializable("item", mList.get(position));
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mList.get(position).title;
        }

        /*
         * こいつの返却数がページ数になる
         */
        @Override
        public int getCount() {
            return mList.size();
            // return NUM_ITEMS;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        /**
         * アイテムを追加する.
         *
         * @param item {@link net.wasnot.music.simplemusicplayer.list.MusicPagerFragment.PageItem}
         */
        public void addItem(PageItem item) {
            mList.add(item);
        }

    }

    private class PageItem {

        /**
         * ページ名.
         */
        public String title;
        /**
         * uri.
         */
        public String uri;
        public int category;
    }

    // private ServiceBindListener mServiceBindListener = new
    // ServiceBindListener() {
    // @Override
    // public void onSeekCompleted(boolean isSuccess) {
    // }
    //
    // @Override
    // public void onStateChanged(final MusicItem nowItem, final State state,
    // final int currentPosition) {
    // mHandler.post(new Runnable() {
    // public void run() {
    // changePlayingLayout(nowItem, state);
    // }
    // });
    // }
    //
    // @Override
    // public void onServiceConnected(PlayerService service) {
    // mService = service;
    // // mService.setServiceListener(mServiceListener);
    // LogUtil.d(TAG, "service:" + mService + ", player:" +
    // mService.getPlayer());
    // if (mService != null) {
    // mHandler.post(new Runnable() {
    // public void run() {
    // changePlayingLayout(mService.getItem(), mService.getState());
    // }
    // });
    // }
    // }
    // };
}
