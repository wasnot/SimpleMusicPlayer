package net.wasnot.music.simplemusicplayer.service;

//import com.freebit.silkhat.musicplayer.service.PlayerService.State;

oneway interface IPlayerServiceCallback {
     void onSeekCompleted(boolean isSuccess);

     void onStateChanged(String title, long id,
         String albumArt, String artist, String album, long duration, String path, int state, int currentPosition);
}
