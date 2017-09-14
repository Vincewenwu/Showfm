package co.bankoo.zuweie.showfm.model;


import android.media.MediaPlayer;

import java.io.IOException;

public class PlaybackAdapter {

    public PlaybackAdapter (MediaPlayer mediaPlayer) {
        this.m_Mediaplayer = mediaPlayer;
    }

    public void setResourceAsync (String url) throws IOException {
        m_Mediaplayer.setDataSource(url);
    }

    public void setOnErrorListener () {

    }

    MediaPlayer m_Mediaplayer;


}