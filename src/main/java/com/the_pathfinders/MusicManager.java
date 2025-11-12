package com.the_pathfinders;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MusicManager {
    private static MediaPlayer mediaPlayer;
    private static volatile boolean isLoading = false;
    private static volatile boolean shouldPlayWhenReady = false;

    /**
     * Preload the background music asynchronously without blocking.
     * This completely avoids lag by loading on a background thread.
     */
    public static void preloadBackgroundMusic() {
        if (mediaPlayer == null && !isLoading) {
            isLoading = true;
            
            System.out.println("  - Starting async music load...");
            
            // Use a background thread to load the media
            Thread loadThread = new Thread(() -> {
                try {
                    System.out.println("  - Creating Media object (background thread)...");
                    String musicFile = MusicManager.class.getResource("/assets/audio/bg_music.mp3").toExternalForm();
                    Media sound = new Media(musicFile);
                    
                    System.out.println("  - Creating MediaPlayer on JavaFX thread...");
                    
                    // MediaPlayer must be created on JavaFX thread
                    Platform.runLater(() -> {
                        try {
                            mediaPlayer = new MediaPlayer(sound);
                            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                            mediaPlayer.setVolume(0.9);
                            
                            mediaPlayer.setOnReady(() -> {
                                System.out.println("  - Media is ready!");
                                isLoading = false;
                                
                                // If play was requested before ready, play now
                                if (shouldPlayWhenReady) {
                                    mediaPlayer.play();
                                    System.out.println("  - Started playing (deferred)");
                                    shouldPlayWhenReady = false;
                                }
                            });
                            
                            mediaPlayer.setOnError(() -> {
                                System.err.println("  - Media error: " + mediaPlayer.getError().getMessage());
                                isLoading = false;
                            });
                        } catch (Exception e) {
                            System.err.println("  - Error creating MediaPlayer: " + e.getMessage());
                            isLoading = false;
                        }
                    });
                    
                } catch (Exception e) {
                    System.err.println("  - Error loading music: " + e.getMessage());
                    isLoading = false;
                }
            });
            
            loadThread.setName("MusicLoader");
            loadThread.setDaemon(true);
            loadThread.start();
            
            // DO NOT wait - return immediately!
            System.out.println("  - Music loading started in background (non-blocking)");
        }
    }

    public static void playBackgroundMusic() {
        // If not preloaded, preload first
        if (mediaPlayer == null) {
            if (!isLoading) {
                System.out.println("  - Music not preloaded, starting load now...");
                shouldPlayWhenReady = true;
                preloadBackgroundMusic();
            } else {
                // Still loading, defer play
                System.out.println("  - Music still loading, will play when ready...");
                shouldPlayWhenReady = true;
            }
        } else {
            // Already loaded, play immediately
            if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                mediaPlayer.play();
                System.out.println("  - Started playing immediately (already loaded)");
            }
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
