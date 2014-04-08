package net.wasnot.music.simplemusicplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import net.wasnot.music.simplemusicplayer.utli.LogUtil;

/**
 * Created by aidaakihiro on 2014/02/20.
 */
public class MusicActivity extends ActionBarActivity implements FragmentManager.OnBackStackChangedListener {
    private final static String TAG = MusicActivity.class.getSimpleName();

    private Fragment mContent;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_music);

        if (savedInstance != null)
            mContent = getSupportFragmentManager().getFragment(savedInstance, "mContent");
        if (mContent == null)
            mContent = new TabFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, mContent
        ).commit();

        // customize the ActionBar
        ActionBar actionBar = getSupportActionBar();
        // actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME );
//        actionBar.setIcon(R.drawable.actionbar_menu);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.player_actionbar_bg));
        // actionBar.setDisplayShowTitleEnabled(false);
        // actionBar.setDisplayHomeAsUpEnabled(true);
        // actionBar.setDisplayUseLogoEnabled(true);


        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO savedinstancestateにfragmentを入れちゃだめだよね、、よく死ぬし。
        try {
            LogUtil.d(TAG, "onSaveInstanceState " + mContent);
            getSupportFragmentManager().putFragment(outState, "mContent", mContent);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        super.onSaveInstanceState(outState);
    }

    public void switchContent(Fragment fragment) {
        int waitTime = 100;
        mContent = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in,
                        R.anim.fade_out).replace(R.id.container, fragment).addToBackStack(null)
                .commit();
        // LogUtil.d("switchContent","icon:"+icon+" title:"+title);
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fm = getSupportFragmentManager();
        LogUtil.d(TAG, "onBackStackChanged " + fm.getBackStackEntryCount());
        String fragments = "";
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fragments += "[" + fm.getBackStackEntryAt(i).getName() + "],";
        }
        if (fragments.length() > 0)
            LogUtil.d(TAG, "onBackStackChanged " + fragments);
        mContent = fm.findFragmentById(R.id.container);
    }
}
