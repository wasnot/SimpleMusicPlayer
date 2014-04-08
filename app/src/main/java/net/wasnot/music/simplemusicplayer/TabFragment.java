package net.wasnot.music.simplemusicplayer;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import net.wasnot.music.simplemusicplayer.list.AudioSelector;
import net.wasnot.music.simplemusicplayer.list.MusicListFragment;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TabFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class TabFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public TabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Context con = getActivity();
        FragmentTabHost host = (FragmentTabHost) getActivity().findViewById(android.R.id.tabhost);
        host.setup(getActivity(), getFragmentManager(), R.id.content);

//        TabHost.TabSpec tabSpec1 = host.newTabSpec("tab1");
//        Button button1 = new Button(this);
//        button1.setBackgroundResource(R.drawable.abc_ic_go);
//        tabSpec1.setIndicator(button1);
//        Bundle bundle1 = new Bundle();
//        bundle1.putString("name", "Tab1");
//        host.addTab(tabSpec1, SampleFragment.class, bundle1);

        TabHost.TabSpec tabSpec1 = host.newTabSpec("tab1");
//        Button button1 = new Button(con);
//        button1.setBackgroundResource(R.drawable.abc_ic_go);
//        tabSpec1.setIndicator(button1);
        tabSpec1.setIndicator("playlist");
        host.addTab(tabSpec1, MusicListFragment.class, makeArguments(AudioSelector.CATEGORY_PLAYLIST));

        TabHost.TabSpec tabSpec2 = host.newTabSpec("tab2");
//        Button button2 = new Button(con);
////        button2.setBackgroundResource(R.drawable.abc_ic_search);
//        tabSpec2.setIndicator(button2);
        tabSpec2.setIndicator("artist");
        host.addTab(tabSpec2, MusicListFragment.class, makeArguments(AudioSelector.CATEGORY_ARTIST));

        TabHost.TabSpec tabSpec3 = host.newTabSpec("tab3");
//        Button button3 = new Button(con);
////        button3.setBackgroundResource(R.drawable.ic_drawer);
//        tabSpec3.setIndicator(button3);
        tabSpec3.setIndicator("songs");
        host.addTab(tabSpec3, MusicListFragment.class, makeArguments(AudioSelector.CATEGORY_SONGS));

        TabHost.TabSpec tabSpec4 = host.newTabSpec("tab4");
//        Button button4 = new Button(con);
//        button4.setBackgroundResource(R.drawable.abc_ic_search);
//        tabSpec4.setIndicator(button4);
        tabSpec4.setIndicator("album");
        host.addTab(tabSpec4, MusicListFragment.class, makeArguments(AudioSelector.CATEGORY_ALBUM));

        TabHost.TabSpec tabSpec5 = host.newTabSpec("tab5");
//        Button button5 = new Button(con);
//        button5.setBackgroundResource(R.drawable.ic_drawer);
//        tabSpec5.setIndicator(button5);
        tabSpec5.setIndicator("genre");
        host.addTab(tabSpec5, MusicListFragment.class, makeArguments(AudioSelector.CATEGORY_GENRE));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private Bundle makeArguments(int category) {
//        int category = AudioSelector.CATEGORY_SONGS;
        String title = getString(R.string.music_page_title_songs);
        String uri;
        switch (category) {
            case AudioSelector.CATEGORY_SONGS:
            default:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();
                break;
            case AudioSelector.CATEGORY_ALBUM:
                uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.toString();
                break;
            case AudioSelector.CATEGORY_ARTIST:
                uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI.toString();
                break;
            case AudioSelector.CATEGORY_PLAYLIST:
                uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI.toString();
                break;
            case AudioSelector.CATEGORY_GENRE:
                uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.toString();
                break;
        }
        Bundle args = new Bundle();
        args.putInt(MusicListFragment.ARG_CATEGORY, category);
        args.putString(MusicListFragment.ARG_TITLE, title);
        args.putString(MusicListFragment.ARG_URI, uri);
        return args;
    }

}
