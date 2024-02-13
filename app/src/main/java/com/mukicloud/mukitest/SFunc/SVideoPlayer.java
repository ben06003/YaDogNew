package com.mukicloud.mukitest.SFunc;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.mukicloud.mukitest.Activity.ActivityWeb;
import com.mukicloud.mukitest.R;

public class SVideoPlayer {
    ExoPlayer exoPlayer;
    public PlayerView PV_Player;
    private boolean isPlaying = false;

    // 初始化 ExoPlayer
    public SVideoPlayer(ActivityWeb act) {
        exoPlayer = new ExoPlayer.Builder(act).build();
        PV_Player = act.findViewById(R.id.exoplayer_view);
        PV_Player.setPlayer(exoPlayer);
    }

    public void startVideo(String url) {
        if (!isPlaying) {
            isPlaying = true;
            exoPlayer.setMediaItem(com.google.android.exoplayer2.MediaItem.fromUri(url));
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }

    public void stopVideo() {
        isPlaying = false;
        exoPlayer.stop();
        exoPlayer.clearVideoSurface();
    }

    public void destroy() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
