package com.the_pathfinders;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MusicManager {
    private static MediaPlayer mediaPlayer;

    public static void playBackgroundMusic() {
        // play only if not already playing
        if (mediaPlayer == null) {
            String musicFile = MusicManager.class.getResource("/assets/audio/bg_music.mp3").toExternalForm();
            Media sound = new Media(musicFile);
            mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // infinite loop
            mediaPlayer.setVolume(0.3); // adjust volume as needed (0.0 - 1.0)
            mediaPlayer.play();
        } else if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            mediaPlayer.play();
        }
    }

    public static void stopBackgroundMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    public static void pauseBackgroundMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public static void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }
}
